package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.ChatMessageDTO;
import com.scutelnic.rutex.dto.ConversationDTO;
import com.scutelnic.rutex.dto.MessageReadRequest;
import com.scutelnic.rutex.dto.ReactionRequest;
import com.scutelnic.rutex.dto.SendMessageRequest;
import com.scutelnic.rutex.entity.Conversation;
import com.scutelnic.rutex.entity.Message;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.ChatService;
import com.scutelnic.rutex.service.ChatSseService;
import com.scutelnic.rutex.service.CloudinaryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatSseService chatSseService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }
        SseEmitter emitter = chatSseService.createEmitter(currentUser.getId());
        List<Message> delivered = chatService.markDeliveredForUser(currentUser.getId());
        for (Message message : delivered) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("messageId", message.getId());
            payload.put("conversationId", message.getConversation().getId());
            payload.put("deliveredAt", message.getDeliveredAt());
            payload.put("readerId", currentUser.getId());
            chatSseService.sendEvent(message.getSender().getId(), "delivered", payload);
        }
        chatSseService.sendEvent(currentUser.getId(), "ready", Map.of("timestamp", LocalDateTime.now().toString()));
        return emitter;
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Trebuie să fiți logat."));
        }
        List<ConversationDTO> conversations = chatService.getConversations(currentUser.getId());
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversation")
    public ResponseEntity<?> getOrCreateConversation(@RequestParam("userId") Long otherUserId, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Trebuie să fiți logat."));
        }
        try {
            ConversationDTO conversation = chatService.getConversationWithUser(currentUser.getId(), otherUserId);
            return ResponseEntity.ok(conversation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam("conversationId") Long conversationId,
                                        @RequestParam(value = "beforeId", required = false) Long beforeId,
                                        @RequestParam(value = "limit", defaultValue = "30") int limit,
                                        HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Trebuie să fiți logat."));
        }
        try {
            List<ChatMessageDTO> messages = chatService.getMessages(conversationId, currentUser.getId(), beforeId, limit);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendMessage(@RequestParam("recipientId") Long recipientId,
                                         @RequestParam(value = "contentText", required = false) String contentText,
                                         @RequestParam(value = "image", required = false) MultipartFile image,
                                         HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Trebuie să fiți logat."));
        }

        if ((contentText == null || contentText.trim().isEmpty()) && (image == null || image.isEmpty())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mesajul nu poate fi gol."));
        }

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadChatImage(image);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
            }
        }

        boolean recipientOnline = chatSseService.hasActiveEmitters(recipientId);
        ChatMessageDTO message = chatService.sendMessage(currentUser.getId(), recipientId, contentText, imageUrl, recipientOnline);

        chatSseService.sendEvent(recipientId, "message", message);
        chatSseService.sendEvent(currentUser.getId(), "message", message);

        return ResponseEntity.ok(message);
    }

    @PostMapping(value = "/send-text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> sendTextMessage(@RequestBody SendMessageRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Trebuie să fiți logat."));
        }
        if (request == null || request.getRecipientId() == null || request.getContentText() == null || request.getContentText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mesajul nu poate fi gol."));
        }

        boolean recipientOnline = chatSseService.hasActiveEmitters(request.getRecipientId());
        ChatMessageDTO message = chatService.sendMessage(currentUser.getId(), request.getRecipientId(), request.getContentText(), null, recipientOnline);

        chatSseService.sendEvent(request.getRecipientId(), "message", message);
        chatSseService.sendEvent(currentUser.getId(), "message", message);

        return ResponseEntity.ok(message);
    }

    @PostMapping("/read")
    public ResponseEntity<?> markRead(@RequestBody MessageReadRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Trebuie să fiți logat."));
        }
        if (request.getConversationId() == null || request.getLastMessageId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Parametri invalizi."));
        }
        try {
            int updated = chatService.markRead(request.getConversationId(), currentUser.getId(), request.getLastMessageId());
            Map<String, Object> payload = new HashMap<>();
            payload.put("conversationId", request.getConversationId());
            payload.put("lastMessageId", request.getLastMessageId());
            payload.put("readerId", currentUser.getId());
            payload.put("updated", updated);
            Long otherUserId = chatService.getOtherUserId(request.getConversationId(), currentUser.getId());
            if (otherUserId != null) {
                chatSseService.sendEvent(otherUserId, "seen", payload);
            }
            return ResponseEntity.ok(Map.of("updated", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/react")
    public ResponseEntity<?> react(@RequestBody ReactionRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Trebuie să fiți logat."));
        }
        if (request.getMessageId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Parametri invalizi."));
        }
        try {
            List<com.scutelnic.rutex.dto.ReactionSummaryDTO> reactions = chatService.toggleReaction(request.getMessageId(), currentUser.getId(), request.getEmoji());
            Map<String, Object> payload = new HashMap<>();
            payload.put("messageId", request.getMessageId());
            payload.put("reactions", reactions);
            broadcastToConversation(request.getMessageId(), payload, "reaction");
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> unreadCount(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("count", 0));
        }
        long count = chatService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }


    private void broadcastToConversation(Long messageId, Map<String, Object> payload, String eventName) {
        Conversation conversation = chatService.getConversationByMessageId(messageId);
        if (conversation == null) {
            return;
        }
        Long userOneId = conversation.getUserOne().getId();
        Long userTwoId = conversation.getUserTwo().getId();
        chatSseService.sendEvent(userOneId, eventName, payload);
        chatSseService.sendEvent(userTwoId, eventName, payload);
    }

}
