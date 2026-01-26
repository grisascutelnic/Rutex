const root = document.getElementById('messages-root');
const conversationListEl = document.getElementById('conversation-list');
const conversationEmptyEl = document.getElementById('conversation-empty');
const chatMessagesEl = document.getElementById('chat-messages');
const chatEmptyEl = document.getElementById('chat-empty');
const chatHeaderEl = document.getElementById('chat-header');
const chatUserAvatarEl = document.getElementById('chat-user-avatar');
const chatUserNameEl = document.getElementById('chat-user-name');
const chatBackBtn = document.getElementById('chat-back-btn');
const chatInputEl = document.getElementById('chat-input');
const chatInputText = document.getElementById('chat-input-text');
const chatImageInput = document.getElementById('chat-image-input');
const chatUploadPreview = document.getElementById('chat-upload-preview');
const sendMessageBtn = document.getElementById('send-message-btn');

const currentUserId = Number(root?.dataset.currentUserId || 0);
const currentLang = root?.dataset.currentLang || 'ro';
const statusSent = root?.dataset.statusSent || 'sent';
const statusDelivered = root?.dataset.statusDelivered || 'delivered';
const statusSeen = root?.dataset.statusSeen || 'seen';
const photoLabel = root?.dataset.photoLabel || 'Foto';

const allowedReactions = ['👍', '❤️', '😂', '😮', '😢', '😡'];

let conversations = [];
let activeConversationId = null;
let activeOtherUserId = null;
let activeOtherUserName = '';
let lastLoadedMessageId = null;
let eventSource = null;
let eventSourceReady = false;
let pollingTimer = null;
let pollingFallbackActive = false;
let isMobileView = window.matchMedia('(max-width: 960px)').matches;
let activeReactionPicker = null;
let suppressNextClickClose = false;

function formatTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString(currentLang === 'ru' ? 'ru-RU' : 'ro-RO', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatDateTime(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString(currentLang === 'ru' ? 'ru-RU' : 'ro-RO', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function getStatusLabel(message) {
    if (message.readAt) return statusSeen;
    if (message.deliveredAt) return statusDelivered;
    return statusSent;
}

function renderConversationList() {
    conversationListEl.querySelectorAll('.conversation-item').forEach(el => el.remove());

    if (!conversations.length) {
        conversationEmptyEl.style.display = 'block';
        return;
    }

    conversationEmptyEl.style.display = 'none';

    conversations.forEach(conversation => {
        const item = document.createElement('div');
        item.className = 'conversation-item' + (conversation.conversationId === activeConversationId ? ' active' : '');
        item.dataset.conversationId = conversation.conversationId;

        const avatar = document.createElement('div');
        avatar.className = 'conversation-avatar';
        if (conversation.otherUserProfileImage) {
            const img = document.createElement('img');
            img.src = conversation.otherUserProfileImage;
            img.alt = conversation.otherUserName;
            avatar.appendChild(img);
        } else {
            avatar.innerHTML = '<i class="fas fa-user"></i>';
        }

        const content = document.createElement('div');
        content.className = 'conversation-content';

        const title = document.createElement('div');
        title.className = 'conversation-title';
        title.textContent = conversation.otherUserName;

        const preview = document.createElement('div');
        preview.className = 'conversation-preview';
        if (conversation.lastMessageText) {
            preview.textContent = conversation.lastMessageText;
        } else if (conversation.lastMessageImageUrl) {
            preview.textContent = `📷 ${photoLabel}`;
        } else {
            preview.textContent = '';
        }

        content.appendChild(title);
        content.appendChild(preview);

        const meta = document.createElement('div');
        meta.className = 'conversation-meta';

        const time = document.createElement('div');
        time.textContent = formatTime(conversation.lastMessageAt);
        meta.appendChild(time);

        if (conversation.unreadCount > 0) {
            const badge = document.createElement('span');
            badge.className = 'conversation-badge';
            badge.textContent = conversation.unreadCount > 99 ? '99+' : conversation.unreadCount;
            meta.appendChild(badge);
        }

        item.appendChild(avatar);
        item.appendChild(content);
        item.appendChild(meta);
        item.addEventListener('click', () => selectConversation(conversation.conversationId));

        conversationListEl.appendChild(item);
    });
}

function updateChatHeader() {
    if (!activeConversationId) {
        chatHeaderEl.style.visibility = 'hidden';
        return;
    }
    chatHeaderEl.style.visibility = 'visible';
    chatUserAvatarEl.innerHTML = '';
    const conversation = conversations.find(c => c.conversationId === activeConversationId);
    if (conversation && conversation.otherUserProfileImage) {
        const img = document.createElement('img');
        img.src = conversation.otherUserProfileImage;
        img.alt = conversation.otherUserName;
        chatUserAvatarEl.appendChild(img);
    } else {
        chatUserAvatarEl.innerHTML = '<i class="fas fa-user"></i>';
    }
    chatUserNameEl.textContent = conversation ? conversation.otherUserName : '';
}

function selectConversation(conversationId) {
    activeConversationId = conversationId;
    const conversation = conversations.find(c => c.conversationId === conversationId);
    activeOtherUserId = conversation ? conversation.otherUserId : null;
    activeOtherUserName = conversation ? conversation.otherUserName : '';
    lastLoadedMessageId = null;

    renderConversationList();
    updateChatHeader();

    chatMessagesEl.innerHTML = '';
    chatEmptyEl.style.display = 'none';
    chatInputEl.style.display = 'flex';

    loadMessages();
    if (isMobileView) {
        showChatView();
    }
}

function loadMessages(beforeId) {
    if (!activeConversationId) return;

    const params = new URLSearchParams({ conversationId: activeConversationId, limit: 30 });
    if (beforeId) params.append('beforeId', beforeId);

    fetch('/api/messages/history?' + params.toString())
        .then(res => res.ok ? res.json() : [])
        .then(messages => {
            if (!messages.length && !beforeId) {
                chatMessagesEl.innerHTML = '';
                chatEmptyEl.style.display = 'block';
                return;
            }
            chatEmptyEl.style.display = 'none';
            renderMessages(messages, !!beforeId);
        })
        .catch(() => {});
}

function renderMessages(messages, prepend) {
    if (!messages.length) return;

    if (prepend) {
        const currentHeight = chatMessagesEl.scrollHeight;
        messages.forEach(message => renderMessage(message, true));
        const newHeight = chatMessagesEl.scrollHeight;
        chatMessagesEl.scrollTop = newHeight - currentHeight;
    } else {
        messages.forEach(message => renderMessage(message, false));
        scrollToBottom();
    }

    lastLoadedMessageId = messages[0]?.id || lastLoadedMessageId;

    const lastIncoming = messages.filter(m => !m.own).pop();
    if (lastIncoming) {
        markConversationRead(lastIncoming.id);
    }

    updateLastOwnStatusLabel();
}

function renderMessage(message, prepend) {
    const wrapper = document.createElement('div');
    wrapper.className = 'message-bubble' + (message.own ? ' own' : '');
    wrapper.dataset.messageId = message.id;
    wrapper.dataset.status = message.own ? getStatusLabel(message) : '';

    if (message.contentText) {
        const text = document.createElement('div');
        text.textContent = message.contentText;
        wrapper.appendChild(text);
    }

    if (message.imageUrl) {
        const img = document.createElement('img');
        img.className = 'message-image';
        img.src = message.imageUrl;
        img.alt = 'foto';
        wrapper.appendChild(img);
    }

    const time = document.createElement('div');
    time.className = 'message-time';
    time.textContent = formatTime(message.createdAt);
    wrapper.appendChild(time);

    const reactionContainer = document.createElement('div');
    reactionContainer.className = 'message-reactions';
    updateReactions(reactionContainer, message.reactions || [], message.id);
    wrapper.appendChild(reactionContainer);

    const reactionButton = document.createElement('button');
    reactionButton.type = 'button';
    reactionButton.className = 'reaction-trigger';
    reactionButton.textContent = '🙂';
    reactionButton.addEventListener('click', (event) => {
        event.stopPropagation();
        toggleReactionPicker(wrapper, message.id);
    });
    wrapper.appendChild(reactionButton);

    let longPressTimer = null;
    wrapper.addEventListener('touchstart', () => {
        longPressTimer = setTimeout(() => {
            toggleReactionPicker(wrapper, message.id);
            suppressNextClickClose = true;
        }, 500);
    }, { passive: true });
    wrapper.addEventListener('touchend', () => {
        if (longPressTimer) {
            clearTimeout(longPressTimer);
            longPressTimer = null;
        }
    });
    wrapper.addEventListener('touchmove', () => {
        if (longPressTimer) {
            clearTimeout(longPressTimer);
            longPressTimer = null;
        }
    });

    // picker stays open until user clicks outside it or selects a reaction

    if (prepend) {
        chatMessagesEl.prepend(wrapper);
    } else {
        chatMessagesEl.appendChild(wrapper);
    }
}

function updateReactions(container, reactions, messageId) {
    container.innerHTML = '';
    reactions.forEach(reaction => {
        const chip = document.createElement('div');
        chip.className = 'reaction-chip' + (reaction.reacted ? ' active' : '');
        chip.textContent = `${reaction.emoji} ${reaction.count}`;
        chip.addEventListener('click', (event) => {
            event.stopPropagation();
            sendReaction(messageId, reaction.emoji);
        });
        container.appendChild(chip);
    });
}

function toggleReactionPicker(wrapper, messageId) {
    const existing = wrapper.querySelector('.reaction-picker');
    if (existing) {
        existing.remove();
        activeReactionPicker = null;
        return;
    }
    if (activeReactionPicker) {
        activeReactionPicker.remove();
        activeReactionPicker = null;
    }
    const picker = document.createElement('div');
    picker.className = 'reaction-picker';
    allowedReactions.forEach(emoji => {
        const button = document.createElement('button');
        button.type = 'button';
        button.textContent = emoji;
        button.addEventListener('click', (event) => {
            event.stopPropagation();
            sendReaction(messageId, emoji);
            picker.remove();
        });
        picker.appendChild(button);
    });
    wrapper.appendChild(picker);
    activeReactionPicker = picker;
}

function sendReaction(messageId, emoji) {
    fetch('/api/messages/react', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ messageId, emoji })
    })
        .then(res => res.ok ? res.json() : null)
        .then(payload => {
            if (!payload || !payload.messageId) return;
            handleReactionUpdate(payload);
        })
        .catch(() => {});
}

function scrollToBottom() {
    chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight;
}

function markConversationRead(lastMessageId) {
    if (!activeConversationId) return;
    fetch('/api/messages/read', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ conversationId: activeConversationId, lastMessageId })
    }).then(() => {
        const convo = conversations.find(c => c.conversationId === activeConversationId);
        if (convo) {
            convo.unreadCount = 0;
            renderConversationList();
        }
        refreshChatBadge();
    }).catch(() => {});
}

function sendMessage() {
    if (!activeConversationId || !activeOtherUserId) return;
    const text = chatInputText.value.trim();
    const image = chatImageInput.files[0];
    if (!text && !image) return;

    const sendRequest = image
        ? fetch('/api/messages/send', {
            method: 'POST',
            body: (() => {
                const formData = new FormData();
                formData.append('recipientId', activeOtherUserId);
                if (text) formData.append('contentText', text);
                formData.append('image', image);
                return formData;
            })()
        })
        : fetch('/api/messages/send-text', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ recipientId: activeOtherUserId, contentText: text })
        });

    sendRequest
        .then(res => res.ok ? res.json() : null)
        .then(message => {
            if (!message) return;
            chatInputText.value = '';
            chatImageInput.value = '';
            chatUploadPreview.innerHTML = '';
            if (!eventSourceReady) {
                handleIncomingMessage(message);
            }
        })
        .catch(() => {});
}

function updateConversationFromMessage(message) {
    let conversation = conversations.find(c => c.conversationId === message.conversationId);
    if (!conversation) {
        fetch('/api/messages/conversations')
            .then(res => res.ok ? res.json() : [])
            .then(data => {
                conversations = data;
                renderConversationList();
            });
        return;
    }

    conversation.lastMessageText = message.contentText;
    conversation.lastMessageImageUrl = message.imageUrl;
    conversation.lastMessageAt = message.createdAt;
    conversation.lastMessageSenderId = message.senderId;
    if (message.senderId === currentUserId) {
        conversation.lastMessageStatus = getStatusLabel(message);
    }
    if (message.senderId !== currentUserId && message.conversationId !== activeConversationId) {
        conversation.unreadCount = (conversation.unreadCount || 0) + 1;
    }

    conversations = conversations.sort((a, b) => {
        const aTime = a.lastMessageAt ? new Date(a.lastMessageAt).getTime() : 0;
        const bTime = b.lastMessageAt ? new Date(b.lastMessageAt).getTime() : 0;
        return bTime - aTime;
    });
    renderConversationList();
}

function handleIncomingMessage(message) {
    updateConversationFromMessage(message);
    if (message.conversationId !== activeConversationId) {
        return;
    }
    renderMessage(message, false);
    scrollToBottom();

    if (!message.own) {
        markConversationRead(message.id);
    } else {
        refreshChatBadge();
    }
    updateLastOwnStatusLabel();
}

function handleReactionUpdate(payload) {
    const messageId = payload.messageId;
    const reactions = payload.reactions || [];
    const messageEl = chatMessagesEl.querySelector(`[data-message-id="${messageId}"]`);
    if (!messageEl) return;
    const container = messageEl.querySelector('.message-reactions');
    if (container) {
        updateReactions(container, reactions, messageId);
    }
}

function handleSeenUpdate(payload) {
    if (payload.conversationId !== activeConversationId) return;
    const messageEl = chatMessagesEl.querySelector(`[data-message-id="${payload.lastMessageId}"]`);
    if (messageEl) {
        messageEl.dataset.status = statusSeen;
    }
    updateLastOwnStatusLabel();
    refreshChatBadge();
}

function handleDeliveredUpdate(payload) {
    if (payload.conversationId !== activeConversationId) return;
    const messageEl = chatMessagesEl.querySelector(`[data-message-id="${payload.messageId}"]`);
    if (!messageEl) return;
    if (messageEl.dataset.status !== statusSeen) {
        messageEl.dataset.status = statusDelivered;
    }
    updateLastOwnStatusLabel();
}

function initEventSource() {
    if (!!window.EventSource) {
        try {
            eventSource = new EventSource('/api/messages/stream', { withCredentials: true });
        } catch (err) {
            eventSource = new EventSource('/api/messages/stream');
        }
        eventSource.addEventListener('message', (event) => {
            if (!event.data) return;
            const message = JSON.parse(event.data);
            handleIncomingMessage(message);
        });
        eventSource.addEventListener('ready', () => {
            eventSourceReady = true;
            stopPollingFallback();
        });
        eventSource.addEventListener('reaction', (event) => {
            if (!event.data) return;
            const payload = JSON.parse(event.data);
            handleReactionUpdate(payload);
        });
        eventSource.addEventListener('seen', (event) => {
            if (!event.data) return;
            const payload = JSON.parse(event.data);
            handleSeenUpdate(payload);
        });
        eventSource.addEventListener('delivered', (event) => {
            if (!event.data) return;
            const payload = JSON.parse(event.data);
            handleDeliveredUpdate(payload);
        });
        eventSource.addEventListener('error', () => {
            if (!pollingFallbackActive) {
                startPollingFallback();
            }
        });

        setTimeout(() => {
            if (!eventSourceReady) {
                startPollingFallback();
            }
        }, 4000);
    }
}

function loadConversations() {
    fetch('/api/messages/conversations')
        .then(res => res.ok ? res.json() : [])
        .then(data => {
            conversations = data;
            renderConversationList();
        })
        .catch(() => {});
}

function refreshActiveConversation() {
    if (!activeConversationId) return;
    const params = new URLSearchParams({ conversationId: activeConversationId, limit: 30 });
    fetch('/api/messages/history?' + params.toString())
        .then(res => res.ok ? res.json() : [])
        .then(messages => {
            if (!messages.length) return;
            messages.forEach(message => {
                const existing = chatMessagesEl.querySelector(`[data-message-id="${message.id}"]`);
                if (existing) {
                    const reactionsEl = existing.querySelector('.message-reactions');
                    if (reactionsEl) {
                        updateReactions(reactionsEl, message.reactions || [], message.id);
                    }
                    if (message.own) {
                        existing.dataset.status = getStatusLabel(message);
                    }
                    return;
                }
                renderMessage(message, false);
            });
            scrollToBottom();
            const lastIncoming = messages.filter(m => !m.own).pop();
            if (lastIncoming) {
                markConversationRead(lastIncoming.id);
            }
            updateLastOwnStatusLabel();
        })
        .catch(() => {});
}

function startPollingFallback() {
    pollingFallbackActive = true;
    if (pollingTimer) return;
    pollingTimer = setInterval(() => {
        loadConversations();
        refreshActiveConversation();
        refreshChatBadge();
    }, 5000);
}

function stopPollingFallback() {
    pollingFallbackActive = false;
    if (pollingTimer) {
        clearInterval(pollingTimer);
        pollingTimer = null;
    }
}

function openConversationFromQuery() {
    const params = new URLSearchParams(window.location.search);
    const userId = params.get('userId');
    if (!userId) return;

    fetch('/api/messages/conversation?userId=' + userId)
        .then(res => res.ok ? res.json() : null)
        .then(conversation => {
            if (!conversation) return;
            const exists = conversations.find(c => c.conversationId === conversation.conversationId);
            if (!exists) {
                conversations.unshift(conversation);
            }
            renderConversationList();
            selectConversation(conversation.conversationId);
        })
        .catch(() => {});
}

function bindEvents() {
    sendMessageBtn.addEventListener('click', sendMessage);
    chatInputText.addEventListener('keydown', (event) => {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            sendMessage();
        }
    });
    chatImageInput.addEventListener('change', () => {
        const file = chatImageInput.files[0];
        if (!file) {
            chatUploadPreview.innerHTML = '';
            return;
        }
        const reader = new FileReader();
        reader.onload = () => {
            chatUploadPreview.innerHTML = '';
            const img = document.createElement('img');
            img.src = reader.result;
            img.alt = 'preview';
            img.className = 'chat-upload-thumb';
            chatUploadPreview.appendChild(img);
        };
        reader.readAsDataURL(file);
    });

    document.addEventListener('click', (event) => {
        if (suppressNextClickClose) {
            suppressNextClickClose = false;
            return;
        }
        if (!activeReactionPicker) {
            return;
        }
        if (event.target.closest('.reaction-picker') || event.target.closest('.reaction-trigger')) {
            return;
        }
        activeReactionPicker.remove();
        activeReactionPicker = null;
    });

    if (chatBackBtn) {
        chatBackBtn.addEventListener('click', () => {
            showListView();
        });
    }

    window.addEventListener('resize', () => {
        const nowMobile = window.matchMedia('(max-width: 960px)').matches;
        if (nowMobile !== isMobileView) {
            isMobileView = nowMobile;
            if (!isMobileView) {
                showDesktopView();
            } else if (activeConversationId) {
                showChatView();
            } else {
                showListView();
            }
        }
    });
}

function initChat() {
    if (!root) return;
    chatInputEl.style.display = 'none';
    chatHeaderEl.style.visibility = 'hidden';
    loadConversations();
    openConversationFromQuery();
    initEventSource();
    bindEvents();
    refreshChatBadge();

    if (isMobileView) {
        showListView();
    }
}

function showListView() {
    root.classList.remove('chat-view');
    root.classList.add('list-view');
}

function showChatView() {
    root.classList.remove('list-view');
    root.classList.add('chat-view');
}

function showDesktopView() {
    root.classList.remove('list-view');
    root.classList.remove('chat-view');
}

function refreshChatBadge() {
    const badges = document.querySelectorAll('.chat-badge');
    if (!badges.length) return;
    fetch('/api/messages/unread-count')
        .then(res => res.ok ? res.json() : { count: 0 })
        .then(data => {
            const count = Number(data.count) || 0;
            badges.forEach(badge => {
                if (count > 0) {
                    badge.hidden = false;
                    badge.removeAttribute('hidden');
                    badge.textContent = count > 99 ? '99+' : count;
                } else {
                    badge.hidden = true;
                    badge.setAttribute('hidden', '');
                    badge.textContent = '0';
                }
            });
        })
        .catch(() => {});
}

function updateLastOwnStatusLabel() {
    const existing = chatMessagesEl.querySelectorAll('.message-status-row');
    existing.forEach(el => el.remove());

    const ownMessages = chatMessagesEl.querySelectorAll('.message-bubble.own');
    if (!ownMessages.length) return;

    const lastOwn = ownMessages[ownMessages.length - 1];
    const statusText = lastOwn.dataset.status || '';
    if (!statusText) return;

    const statusRow = document.createElement('div');
    statusRow.className = 'message-status-row';
    statusRow.textContent = statusText;
    lastOwn.after(statusRow);
}

initChat();
