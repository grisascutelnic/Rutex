const root = document.getElementById('messages-root');
const conversationListEl = document.getElementById('conversation-list');
const conversationEmptyEl = document.getElementById('conversation-empty');
const chatMessagesEl = document.getElementById('chat-messages');
const chatEmptyEl = document.getElementById('chat-empty');
const chatHeaderEl = document.getElementById('chat-header');
const chatUserAvatarEl = document.getElementById('chat-user-avatar');
const chatUserNameEl = document.getElementById('chat-user-name');
const chatUserStatusEl = document.getElementById('chat-user-status');
const chatUserEl = document.getElementById('chat-user');
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
let pendingMessageCounter = 0;
let inputFocused = false;
const isiOS = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;

function updateViewportHeight() {
    const vv = window.visualViewport;
    if (isiOS && root && root.classList.contains('list-view')) {
        root.style.setProperty('--app-height', '100svh');
        root.style.setProperty('--keyboard-offset', '0px');
        root.style.setProperty('--chat-input-height', '0px');
        return;
    }
    let height = vv ? vv.height : window.innerHeight;
    let offsetTop = vv ? vv.offsetTop : 0;
    let rawKeyboardOffset = Math.max(0, window.innerHeight - height - offsetTop);
    let keyboardOffset = inputFocused ? rawKeyboardOffset : 0;

    if (isiOS && !inputFocused) {
        height = window.innerHeight;
        offsetTop = 0;
        rawKeyboardOffset = 0;
        keyboardOffset = 0;
    }
    document.documentElement.style.setProperty('--app-height', `${height}px`);
    document.documentElement.style.setProperty('--keyboard-offset', `${keyboardOffset}px`);
    if (chatInputEl) {
        const rect = chatInputEl.getBoundingClientRect();
        document.documentElement.style.setProperty('--chat-input-height', `${rect.height}px`);
    }
}

function resetKeyboardOffset() {
    document.documentElement.style.setProperty('--keyboard-offset', '0px');
}

function forceIOSViewportReset() {
    if (!isiOS) return;
    inputFocused = false;
    resetKeyboardOffset();
    document.documentElement.style.setProperty('--app-height', `${window.innerHeight}px`);
    setTimeout(() => {
        resetKeyboardOffset();
        document.documentElement.style.setProperty('--app-height', `${window.innerHeight}px`);
    }, 120);
}

function resetListViewLayout() {
    if (!root) return;
    chatInputText.blur();
    inputFocused = false;
    resetKeyboardOffset();
    root.style.setProperty('--chat-input-height', '0px');
    root.style.setProperty('--keyboard-offset', '0px');
    root.style.setProperty('--app-height', '100svh');
    root.classList.remove('chat-view');
    root.classList.add('list-view');
    if (conversationListEl) {
        conversationListEl.scrollTop = 0;
    }
    requestAnimationFrame(() => {
        updateViewportHeight();
        if (isiOS) {
            forceIOSViewportReset();
        }
    });
}

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

    const visibleConversations = conversations.filter(conversation =>
        conversation.lastMessageAt || conversation.lastMessageText || conversation.lastMessageImageUrl
    );

    if (!visibleConversations.length) {
        conversationEmptyEl.style.display = 'block';
        return;
    }

    conversationEmptyEl.style.display = 'none';

    visibleConversations.forEach(conversation => {
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
    if (chatUserStatusEl) {
        chatUserStatusEl.textContent = buildStatusText(conversation);
    }
    if (chatUserEl) {
        chatUserEl.dataset.userId = conversation ? conversation.otherUserId : '';
    }
}

function buildStatusText(conversation) {
    if (!conversation) return '';
    if (conversation.otherUserOnline) {
        return currentLang === 'ru' ? 'В сети' : 'Online';
    }
    if (conversation.otherUserLastSeenAt) {
        const lastSeen = formatDateTime(conversation.otherUserLastSeenAt);
        return currentLang === 'ru'
            ? `Последний визит: ${lastSeen}`
            : `Ultima accesare: ${lastSeen}`;
    }
    return currentLang === 'ru' ? 'Офлайн' : 'Offline';
}

function selectConversation(conversationId) {
    if (activeConversationId && activeConversationId !== conversationId) {
        cleanupConversationIfEmpty(activeConversationId);
    }
    activeConversationId = conversationId;
    const conversation = conversations.find(c => c.conversationId === conversationId);
    activeOtherUserId = conversation ? conversation.otherUserId : null;
    activeOtherUserName = conversation ? conversation.otherUserName : '';
    lastLoadedMessageId = null;

    renderConversationList();
    updateChatHeader();
    updateConversationUrl();
    if (activeConversationId) {
        localStorage.setItem('lastConversationId', String(activeConversationId));
    }
    if (activeOtherUserId) {
        localStorage.setItem('lastOtherUserId', String(activeOtherUserId));
    }

    chatMessagesEl.innerHTML = '';
    chatEmptyEl.style.display = 'none';
    chatInputEl.style.display = 'flex';

    loadMessages();
    updateViewportHeight();
    setTimeout(() => {
        updateViewportHeight();
        scrollToBottom();
    }, 50);
    if (isMobileView) {
        showChatView();
    }
}

function cleanupConversationIfEmpty(conversationId) {
    if (!conversationId) return;
    fetch(`/api/messages/conversations/${conversationId}/cleanup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(res => res.ok ? res.json() : null)
        .then(payload => {
            if (!payload || !payload.deleted) return;
            conversations = conversations.filter(c => c.conversationId !== conversationId);
            if (activeConversationId === conversationId) {
                activeConversationId = null;
                activeOtherUserId = null;
                activeOtherUserName = '';
                chatMessagesEl.innerHTML = '';
                chatInputEl.style.display = 'none';
                chatHeaderEl.style.visibility = 'hidden';
                chatEmptyEl.style.display = 'block';
            }
            renderConversationList();
        })
        .catch(() => {});
}

function cleanupConversationOnExit() {
    if (!activeConversationId) return;
    const url = `/api/messages/conversations/${activeConversationId}/cleanup`;
    if (navigator.sendBeacon) {
        try {
            navigator.sendBeacon(url, '');
            return;
        } catch (e) {
            // ignore and fallback to fetch
        }
    }
    fetch(url, { method: 'POST', keepalive: true }).catch(() => {});
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
            if (!beforeId) {
                scrollToBottom();
            }
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
        if (isNearBottom()) {
            scrollToBottom();
        }
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
    if (message.tempId) {
        wrapper.dataset.tempId = message.tempId;
    }

    const row = document.createElement('div');
    row.className = 'message-row';

    if (message.contentText) {
        const text = document.createElement('div');
        text.className = 'message-text';
        text.textContent = message.contentText;
        row.appendChild(text);
    }

    const time = document.createElement('div');
    time.className = 'message-time';
    time.textContent = formatTime(message.createdAt);
    row.appendChild(time);
    wrapper.appendChild(row);

    if (message.imageUrl) {
        const img = document.createElement('img');
        img.className = 'message-image';
        img.src = message.imageUrl;
        img.alt = 'foto';
        wrapper.appendChild(img);
    }

    const reactionContainer = document.createElement('div');
    reactionContainer.className = 'message-reactions';
    const reactions = Array.isArray(message.reactions) ? message.reactions : [];
    updateReactions(reactionContainer, reactions, message.id);
    wrapper.classList.toggle('has-reactions', reactions.length > 0);
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
    const bubble = container.closest('.message-bubble');
    if (bubble) {
        bubble.classList.toggle('has-reactions', reactions.length > 0);
    }
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

function isNearBottom() {
    const threshold = 120;
    return chatMessagesEl.scrollHeight - chatMessagesEl.scrollTop - chatMessagesEl.clientHeight <= threshold;
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

    const tempId = `temp-${Date.now()}-${pendingMessageCounter++}`;
    const tempMessage = {
        id: tempId,
        tempId,
        own: true,
        contentText: text || '',
        imageUrl: image ? URL.createObjectURL(image) : null,
        createdAt: new Date().toISOString(),
        reactions: []
    };
    renderMessage(tempMessage, false);
    scrollToBottom();
    updateLastOwnStatusLabel();
    chatInputText.value = '';
    chatImageInput.value = '';
    chatUploadPreview.innerHTML = '';

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
            replacePendingMessage(tempId, message);
            if (!eventSourceReady) {
                updateConversationFromMessage(message);
            }
        })
        .catch(() => {});

    chatInputText.blur();
    setTimeout(() => {
        inputFocused = false;
        updateViewportHeight();
        if (isiOS) {
            forceIOSViewportReset();
        }
    }, 80);
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
    if (message && message.id) {
        const existing = chatMessagesEl.querySelector(`[data-message-id="${message.id}"]`);
        if (existing) {
            return;
        }
    }
    if (message && message.own) {
        const pendingEntry = findPendingMatch(message);
        if (pendingEntry) {
            replacePendingMessage(pendingEntry.tempId, message);
            return;
        }
    }
    updateConversationFromMessage(message);
    if (message.conversationId !== activeConversationId) {
        return;
    }
    renderMessage(message, false);
    if (message.own || isNearBottom()) {
        scrollToBottom();
    }

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
        startPollingFallback();
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
            const shouldStick = isNearBottom();
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
            if (shouldStick) {
                scrollToBottom();
            }
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
    chatInputText.addEventListener('focus', () => {
        inputFocused = true;
        updateViewportHeight();
        if (isNearBottom()) {
            scrollToBottom();
        }
    });
    chatInputText.addEventListener('blur', () => {
        inputFocused = false;
        updateViewportHeight();
    });
    chatImageInput.addEventListener('change', () => {
        const file = chatImageInput.files[0];
        if (!file) {
            chatUploadPreview.innerHTML = '';
            return;
        }
        updateViewportHeight();
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

    if (chatUserEl) {
        chatUserEl.addEventListener('click', (event) => {
            if (event.target.closest('#chat-back-btn')) return;
            const userId = Number(chatUserEl.dataset.userId || 0);
            if (userId) {
                navigateToUserProfile(userId);
            }
        });
        chatUserEl.addEventListener('keydown', (event) => {
            if (event.key !== 'Enter' && event.key !== ' ') return;
            event.preventDefault();
            const userId = Number(chatUserEl.dataset.userId || 0);
            if (userId) {
                navigateToUserProfile(userId);
            }
        });
    }

    if (chatBackBtn) {
        chatBackBtn.addEventListener('click', () => {
            if (activeConversationId) {
                cleanupConversationIfEmpty(activeConversationId);
            }
            resetListViewLayout();
            const url = new URL(window.location.href);
            url.searchParams.delete('userId');
            window.history.replaceState(null, '', url.toString());
            if (isiOS) {
                const resetUrl = `${window.location.pathname}?reset=1&ts=${Date.now()}`;
                window.location.replace(resetUrl);
            }
        });
    }

    window.addEventListener('resize', () => {
        updateViewportHeight();
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

    if (window.visualViewport) {
        window.visualViewport.addEventListener('resize', updateViewportHeight);
        window.visualViewport.addEventListener('scroll', updateViewportHeight);
    }

    window.addEventListener('pagehide', cleanupConversationOnExit);
    window.addEventListener('beforeunload', cleanupConversationOnExit);

    window.addEventListener('pageshow', (event) => {
        if (!isiOS) {
            return;
        }
        if (event && event.persisted) {
            const url = new URL(window.location.href);
            if (!url.searchParams.has('reset')) {
                url.searchParams.set('reset', '1');
                url.searchParams.set('ts', Date.now().toString());
                window.location.replace(url.toString());
            }
        }
    });
}

function initChat() {
    if (!root) return;
    updateViewportHeight();
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
    if (conversationListEl) {
        conversationListEl.scrollTop = 0;
    }
    updateViewportHeight();
    forceIOSViewportReset();
}

function showChatView() {
    root.classList.remove('list-view');
    root.classList.add('chat-view');
    if (root) {
        root.style.removeProperty('--app-height');
        root.style.removeProperty('--keyboard-offset');
        root.style.removeProperty('--chat-input-height');
    }
    updateViewportHeight();
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

function navigateToUserProfile(userId) {
    if (!userId) return;
    window.location.href = `/${currentLang}/profile/${userId}`;
}

function updateConversationUrl() {
    if (!activeOtherUserId) return;
    const url = new URL(window.location.href);
    url.searchParams.set('userId', activeOtherUserId);
    window.history.replaceState(null, '', url.toString());
}

function replacePendingMessage(tempId, message) {
    const pendingEl = chatMessagesEl.querySelector(`[data-temp-id="${tempId}"]`);
    if (!pendingEl) {
        return;
    }
    pendingEl.dataset.messageId = message.id;
    pendingEl.dataset.status = getStatusLabel(message);
    pendingEl.removeAttribute('data-temp-id');
    const timeEl = pendingEl.querySelector('.message-time');
    if (timeEl) {
        timeEl.textContent = formatTime(message.createdAt);
    }
    const imageEl = pendingEl.querySelector('.message-image');
    if (imageEl && message.imageUrl) {
        imageEl.src = message.imageUrl;
    }
    updateLastOwnStatusLabel();
}

function findPendingMatch(message) {
    if (!message || !message.own) return null;
    const pendingEls = chatMessagesEl.querySelectorAll('.message-bubble.own[data-temp-id]');
    if (!pendingEls.length) return null;
    const targetText = (message.contentText || '').trim();
    for (let i = pendingEls.length - 1; i >= 0; i -= 1) {
        const el = pendingEls[i];
        const textEl = el.querySelector('.message-text');
        const elText = textEl ? textEl.textContent.trim() : '';
        if (targetText && elText === targetText) {
            return { tempId: el.dataset.tempId };
        }
    }
    return null;
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
