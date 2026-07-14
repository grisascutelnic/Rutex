package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.Conversation;
import com.scutelnic.rutex.entity.Message;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.repository.ConversationRepository;
import com.scutelnic.rutex.repository.MessageRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/messages")
public class AdminMessagesController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public AdminMessagesController(ConversationRepository conversationRepository,
                                   MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/conversations")
    @Transactional(readOnly = true)
    public ResponseEntity<?> conversations(HttpSession session) {
        ResponseEntity<?> denied = requireAdmin(session);
        if (denied != null) {
            return denied;
        }

        List<Map<String, Object>> payload = new ArrayList<>();
        for (Conversation conversation : conversationRepository.findAllWithMessages()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("conversationId", conversation.getId());
            item.put("userOne", userPayload(conversation.getUserOne()));
            item.put("userTwo", userPayload(conversation.getUserTwo()));
            Message lastMessage = conversation.getLastMessage();
            item.put("lastMessageText", lastMessage.getContentText());
            item.put("lastMessageImageUrl", lastMessage.getImageUrl());
            item.put("lastMessageAt", lastMessage.getCreatedAt());
            item.put("lastMessageSenderId", lastMessage.getSender().getId());
            payload.add(item);
        }
        return ResponseEntity.ok(payload);
    }

    @GetMapping("/conversations/{conversationId}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> messages(@PathVariable Long conversationId,
                                      @RequestParam(required = false) Long beforeId,
                                      @RequestParam(defaultValue = "50") int limit,
                                      HttpSession session) {
        ResponseEntity<?> denied = requireAdmin(session);
        if (denied != null) {
            return denied;
        }

        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }

        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Message> messages = beforeId == null
                ? messageRepository.findByConversationIdOrderByIdDesc(conversationId, PageRequest.of(0, safeLimit))
                : messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(conversationId, beforeId, PageRequest.of(0, safeLimit));
        Collections.reverse(messages);

        List<Map<String, Object>> payload = new ArrayList<>();
        for (Message message : messages) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", message.getId());
            item.put("senderId", message.getSender().getId());
            item.put("senderName", fullName(message.getSender()));
            item.put("contentText", message.getContentText());
            item.put("imageUrl", message.getImageUrl());
            item.put("createdAt", message.getCreatedAt());
            item.put("deliveredAt", message.getDeliveredAt());
            item.put("readAt", message.getReadAt());
            payload.add(item);
        }
        return ResponseEntity.ok(payload);
    }

    private ResponseEntity<?> requireAdmin(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Trebuie să fiți logat."));
        }
        boolean admin = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        return admin ? null : ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acces interzis."));
    }

    private Map<String, Object> userPayload(User user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", user.getId());
        payload.put("name", fullName(user));
        payload.put("profileImage", user.getProfileImage());
        return payload;
    }

    private String fullName(User user) {
        return ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
    }
}
