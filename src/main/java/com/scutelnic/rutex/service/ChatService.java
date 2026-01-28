package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.ChatMessageDTO;
import com.scutelnic.rutex.dto.ConversationDTO;
import com.scutelnic.rutex.dto.ReactionSummaryDTO;
import com.scutelnic.rutex.entity.Conversation;
import com.scutelnic.rutex.entity.Message;
import com.scutelnic.rutex.entity.MessageReaction;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.repository.ConversationRepository;
import com.scutelnic.rutex.repository.MessageReactionRepository;
import com.scutelnic.rutex.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Set<String> ALLOWED_REACTIONS = Set.of("👍", "❤️", "😂", "😮", "😢", "😡");

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageReactionRepository messageReactionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    public Conversation getOrCreateConversation(Long userIdA, Long userIdB) {
        if (userIdA.equals(userIdB)) {
            throw new IllegalArgumentException("Nu poți conversa cu tine însuți.");
        }
        Long userOneId = Math.min(userIdA, userIdB);
        Long userTwoId = Math.max(userIdA, userIdB);

        Optional<Conversation> existing = conversationRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId);
        if (existing.isPresent()) {
            return existing.get();
        }

        User userOne = userService.getUserById(userOneId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizatorul nu a fost găsit."));
        User userTwo = userService.getUserById(userTwoId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizatorul nu a fost găsit."));

        Conversation conversation = new Conversation();
        conversation.setUserOne(userOne);
        conversation.setUserTwo(userTwo);
        return conversationRepository.save(conversation);
    }

    public ConversationDTO getConversationWithUser(Long currentUserId, Long otherUserId) {
        Conversation conversation = getOrCreateConversation(currentUserId, otherUserId);
        return buildConversationDto(conversation, currentUserId, 0L);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversations(Long currentUserId) {
        cleanupEmptyConversationsForUser(currentUserId);
        List<Conversation> conversations = conversationRepository.findAllForUserWithMessages(currentUserId);
        if (conversations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> conversationIds = conversations.stream().map(Conversation::getId).collect(Collectors.toList());
        Map<Long, Long> unreadMap = new HashMap<>();
        for (Object[] row : messageRepository.countUnreadByConversation(conversationIds, currentUserId)) {
            Long conversationId = (Long) row[0];
            Long count = (Long) row[1];
            unreadMap.put(conversationId, count);
        }

        List<ConversationDTO> results = new ArrayList<>();
        for (Conversation conversation : conversations) {
            long unreadCount = unreadMap.getOrDefault(conversation.getId(), 0L);
            results.add(buildConversationDto(conversation, currentUserId, unreadCount));
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessages(Long conversationId, Long currentUserId, Long beforeId, int limit) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversația nu a fost găsită."));
        ensureParticipant(conversation, currentUserId);

        List<Message> messages;
        PageRequest pageRequest = PageRequest.of(0, limit);
        if (beforeId != null) {
            messages = messageRepository.findByConversationIdAndIdLessThanOrderByIdDesc(conversationId, beforeId, pageRequest);
        } else {
            messages = messageRepository.findByConversationIdOrderByIdDesc(conversationId, pageRequest);
        }
        Collections.reverse(messages);

        List<Long> messageIds = messages.stream().map(Message::getId).collect(Collectors.toList());
        Map<Long, List<ReactionSummaryDTO>> reactionsMap = buildReactionSummary(messageIds, currentUserId);

        List<ChatMessageDTO> results = new ArrayList<>();
        for (Message message : messages) {
            results.add(buildMessageDto(message, currentUserId, reactionsMap.getOrDefault(message.getId(), Collections.emptyList())));
        }
        return results;
    }

    @Transactional
    public ChatMessageDTO sendMessage(Long senderId, Long recipientId, String contentText, String imageUrl, boolean recipientOnline) {
        Conversation conversation = getOrCreateConversation(senderId, recipientId);
        User sender = userService.getUserById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizatorul nu a fost găsit."));
        User recipient = userService.getUserById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Utilizatorul nu a fost găsit."));

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContentText(contentText != null && !contentText.trim().isEmpty() ? contentText.trim() : null);
        message.setImageUrl(imageUrl);
        if (recipientOnline) {
            message.setDeliveredAt(LocalDateTime.now());
        }

        Message saved = messageRepository.save(message);
        conversation.setLastMessage(saved);
        conversationRepository.save(conversation);

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        String titleRo = "Mesaj nou";
        String messageRo = "Ai primit un mesaj pe Rutex de la " + senderName + ".";
        String titleRu = "Новое сообщение";
        String messageRu = "Вы получили сообщение на Rutex от " + senderName + ".";
        notificationService.createNotification(recipient, titleRo, messageRo, titleRu, messageRu);

        return buildMessageDto(saved, senderId, Collections.emptyList());
    }

    @Transactional
    public int markRead(Long conversationId, Long currentUserId, Long lastMessageId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversația nu a fost găsită."));
        ensureParticipant(conversation, currentUserId);

        if (lastMessageId == null) {
            return 0;
        }

        return messageRepository.markReadUpTo(conversationId, currentUserId, lastMessageId, LocalDateTime.now());
    }

    @Transactional
    public List<Message> markDeliveredForUser(Long currentUserId) {
        List<Message> undelivered = messageRepository.findUndeliveredForUser(currentUserId);
        if (undelivered.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDateTime now = LocalDateTime.now();
        for (Message message : undelivered) {
            message.setDeliveredAt(now);
        }
        return messageRepository.saveAll(undelivered);
    }

    @Transactional
    public List<ReactionSummaryDTO> toggleReaction(Long messageId, Long currentUserId, String emoji) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mesajul nu a fost găsit."));

        Conversation conversation = message.getConversation();
        ensureParticipant(conversation, currentUserId);

        String trimmedEmoji = emoji != null ? emoji.trim() : "";
        if (!trimmedEmoji.isEmpty() && !ALLOWED_REACTIONS.contains(trimmedEmoji)) {
            throw new IllegalArgumentException("Reacție invalidă.");
        }

        Optional<MessageReaction> existing = messageReactionRepository.findByMessageIdAndUserId(messageId, currentUserId);
        if (trimmedEmoji.isEmpty()) {
            existing.ifPresent(messageReactionRepository::delete);
        } else if (existing.isPresent()) {
            MessageReaction reaction = existing.get();
            if (trimmedEmoji.equals(reaction.getEmoji())) {
                messageReactionRepository.delete(reaction);
            } else {
                reaction.setEmoji(trimmedEmoji);
                messageReactionRepository.save(reaction);
            }
        } else {
            MessageReaction reaction = new MessageReaction();
            reaction.setMessage(message);
            reaction.setUser(userService.getUserById(currentUserId).orElseThrow(() -> new IllegalArgumentException("Utilizatorul nu a fost găsit.")));
            reaction.setEmoji(trimmedEmoji);
            messageReactionRepository.save(reaction);
        }

        return buildReactionSummary(Arrays.asList(messageId), currentUserId).getOrDefault(messageId, Collections.emptyList());
    }

    public long getUnreadCount(Long currentUserId) {
        return messageRepository.countUnreadForUser(currentUserId);
    }

    @Transactional
    public boolean deleteConversationIfEmpty(Long conversationId, Long currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return false;
        }
        ensureParticipant(conversation, currentUserId);
        long messageCount = messageRepository.countByConversationId(conversationId);
        if (messageCount > 0) {
            return false;
        }
        conversationRepository.delete(conversation);
        return true;
    }

    public Conversation getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId).orElse(null);
    }

    public Conversation getConversationByMessageId(Long messageId) {
        return messageRepository.findById(messageId).map(Message::getConversation).orElse(null);
    }

    @Transactional(readOnly = true)
    public Long getOtherUserId(Long conversationId, Long currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return null;
        }
        Long userOneId = conversation.getUserOne().getId();
        Long userTwoId = conversation.getUserTwo().getId();
        if (userOneId.equals(currentUserId)) {
            return userTwoId;
        }
        if (userTwoId.equals(currentUserId)) {
            return userOneId;
        }
        return null;
    }

    private void ensureParticipant(Conversation conversation, Long currentUserId) {
        if (!conversation.getUserOne().getId().equals(currentUserId) && !conversation.getUserTwo().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Nu ai acces la această conversație.");
        }
    }

    @Transactional
    public void cleanupEmptyConversationsForUser(Long currentUserId) {
        List<Conversation> empty = conversationRepository.findEmptyForUser(currentUserId);
        if (empty.isEmpty()) {
            return;
        }
        conversationRepository.deleteAll(empty);
    }

    private ConversationDTO buildConversationDto(Conversation conversation, Long currentUserId, long unreadCount) {
        User otherUser = conversation.getUserOne().getId().equals(currentUserId) ? conversation.getUserTwo() : conversation.getUserOne();
        ConversationDTO dto = new ConversationDTO();
        dto.setConversationId(conversation.getId());
        dto.setOtherUserId(otherUser.getId());
        dto.setOtherUserName(otherUser.getFirstName() + " " + otherUser.getLastName());
        dto.setOtherUserProfileImage(buildProfileImageUrl(otherUser.getProfileImage()));
        dto.setUnreadCount(unreadCount);

        Message last = conversation.getLastMessage();
        if (last != null) {
            dto.setLastMessageText(last.getContentText());
            dto.setLastMessageImageUrl(last.getImageUrl());
            dto.setLastMessageAt(last.getCreatedAt());
            dto.setLastMessageSenderId(last.getSender().getId());
            if (last.getSender().getId().equals(currentUserId)) {
                dto.setLastMessageStatus(getStatusLabel(last));
            }
        }

        return dto;
    }

    private ChatMessageDTO buildMessageDto(Message message, Long currentUserId, List<ReactionSummaryDTO> reactions) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFirstName() + " " + message.getSender().getLastName());
        dto.setSenderProfileImage(buildProfileImageUrl(message.getSender().getProfileImage()));
        dto.setContentText(message.getContentText());
        dto.setImageUrl(message.getImageUrl());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setDeliveredAt(message.getDeliveredAt());
        dto.setReadAt(message.getReadAt());
        dto.setOwn(message.getSender().getId().equals(currentUserId));
        dto.setReactions(reactions);
        return dto;
    }

    private String getStatusLabel(Message message) {
        if (message.getReadAt() != null) {
            return "seen";
        }
        if (message.getDeliveredAt() != null) {
            return "delivered";
        }
        return "sent";
    }

    private String buildProfileImageUrl(String profileImage) {
        if (profileImage == null || profileImage.trim().isEmpty()) {
            return null;
        }
        if (profileImage.startsWith("http")) {
            return profileImage;
        }
        return "/uploads/profile-images/" + profileImage;
    }

    private Map<Long, List<ReactionSummaryDTO>> buildReactionSummary(List<Long> messageIds, Long currentUserId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<MessageReaction> reactions = messageReactionRepository.findByMessageIds(messageIds);
        Map<Long, Map<String, ReactionSummaryDTO>> grouped = new HashMap<>();

        for (MessageReaction reaction : reactions) {
            Long messageId = reaction.getMessage().getId();
            String emoji = reaction.getEmoji();
            Map<String, ReactionSummaryDTO> summaryMap = grouped.computeIfAbsent(messageId, key -> new HashMap<>());
            ReactionSummaryDTO summary = summaryMap.get(emoji);
            if (summary == null) {
                summary = new ReactionSummaryDTO();
                summary.setEmoji(emoji);
                summary.setCount(0);
                summary.setReacted(false);
                summaryMap.put(emoji, summary);
            }
            summary.setCount(summary.getCount() + 1);
            if (reaction.getUser().getId().equals(currentUserId)) {
                summary.setReacted(true);
            }
        }

        Map<Long, List<ReactionSummaryDTO>> results = new HashMap<>();
        for (Map.Entry<Long, Map<String, ReactionSummaryDTO>> entry : grouped.entrySet()) {
            List<ReactionSummaryDTO> list = new ArrayList<>(entry.getValue().values());
            results.put(entry.getKey(), list);
        }
        return results;
    }
}
