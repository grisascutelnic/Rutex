(() => {
    const root = document.getElementById('admin-messages-root');
    if (!root) return;

    const list = document.getElementById('admin-conversation-list');
    const count = document.getElementById('admin-conversation-count');
    const messages = document.getElementById('admin-chat-messages');
    const title = document.getElementById('admin-chat-title');
    const subtitle = document.getElementById('admin-chat-subtitle');
    const avatars = document.getElementById('admin-chat-avatars');
    let conversations = [];
    let selectedId = Number(localStorage.getItem('adminSelectedConversation')) || null;
    let loaded = false;
    let oldestMessageId = null;
    let hasOlderMessages = false;
    let loadingOlder = false;

    const escapeHtml = value => String(value ?? '').replace(/[&<>'"]/g, character => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[character]));
    const initials = user => (user.name || '?').split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase();
    const avatar = user => user.profileImage
        ? `<img class="admin-pair-avatar" src="${escapeHtml(user.profileImage)}" alt="">`
        : `<span class="admin-pair-avatar">${escapeHtml(initials(user))}</span>`;
    const pairAvatars = conversation => avatar(conversation.userOne) + avatar(conversation.userTwo);
    const formatTime = value => value ? new Intl.DateTimeFormat('ro-RO', {day:'2-digit', month:'2-digit', hour:'2-digit', minute:'2-digit'}).format(new Date(value)) : '';

    async function loadConversations(force = false) {
        if (loaded && !force) return;
        list.innerHTML = '<div class="admin-chat-state">Se încarcă conversațiile...</div>';
        try {
            const response = await fetch('/admin/api/messages/conversations', {headers: {'Accept': 'application/json'}});
            if (!response.ok) throw new Error('Conversațiile nu au putut fi încărcate.');
            conversations = await response.json();
            loaded = true;
            renderConversations();
            if (selectedId && conversations.some(item => item.conversationId === selectedId)) selectConversation(selectedId);
        } catch (error) {
            list.innerHTML = `<div class="admin-chat-state">${escapeHtml(error.message)}</div>`;
        }
    }

    function renderConversations() {
        count.textContent = `${conversations.length} conversații`;
        if (!conversations.length) {
            list.innerHTML = '<div class="admin-chat-state">Nu există conversații cu mesaje.</div>';
            return;
        }
        list.innerHTML = conversations.map(conversation => {
            const preview = conversation.lastMessageText || (conversation.lastMessageImageUrl ? 'Fotografie' : 'Mesaj');
            return `<button type="button" class="admin-conversation-item${conversation.conversationId === selectedId ? ' active' : ''}" data-conversation-id="${conversation.conversationId}">
                <span class="admin-pair-avatars">${pairAvatars(conversation)}</span>
                <span class="admin-conversation-copy"><span class="admin-conversation-title">${escapeHtml(conversation.userOne.name)} · ${escapeHtml(conversation.userTwo.name)}</span><span class="admin-conversation-preview">${escapeHtml(preview)}</span></span>
                <span class="admin-conversation-time">${escapeHtml(formatTime(conversation.lastMessageAt))}</span>
            </button>`;
        }).join('');
    }

    async function selectConversation(conversationId) {
        const conversation = conversations.find(item => item.conversationId === conversationId);
        if (!conversation) return;
        selectedId = conversationId;
        localStorage.setItem('adminSelectedConversation', String(conversationId));
        root.classList.add('chat-open');
        renderConversations();
        title.textContent = `${conversation.userOne.name} · ${conversation.userTwo.name}`;
        subtitle.textContent = 'Vizualizare doar pentru citire';
        avatars.innerHTML = pairAvatars(conversation);
        messages.innerHTML = '<div class="admin-chat-state">Se încarcă mesajele...</div>';
        try {
            const response = await fetch(`/admin/api/messages/conversations/${conversationId}?limit=50`, {headers: {'Accept': 'application/json'}});
            if (!response.ok) throw new Error('Mesajele nu au putut fi încărcate.');
            const payload = await response.json();
            renderMessages(payload, conversation);
        } catch (error) {
            messages.innerHTML = `<div class="admin-chat-state">${escapeHtml(error.message)}</div>`;
        }
    }

    function renderMessages(payload, conversation) {
        if (!payload.length) {
            messages.innerHTML = '<div class="admin-chat-state">Conversația nu conține mesaje.</div>';
            return;
        }
        oldestMessageId = payload[0].id;
        hasOlderMessages = payload.length === 50;
        messages.innerHTML = olderMessagesControl() + payload.map(message => {
            const side = message.senderId === conversation.userTwo.id ? ' user-two' : '';
            const text = message.contentText ? `<div>${escapeHtml(message.contentText).replace(/\n/g, '<br>')}</div>` : '';
            const image = message.imageUrl ? `<a href="${escapeHtml(message.imageUrl)}" target="_blank" rel="noopener"><img class="admin-message-image" src="${escapeHtml(message.imageUrl)}" alt="Imagine mesaj"></a>` : '';
            return `<div class="admin-message-row${side}"><div class="admin-message-bubble"><div class="admin-message-sender">${escapeHtml(message.senderName)}</div>${text}${image}<div class="admin-message-meta">${escapeHtml(formatTime(message.createdAt))}</div></div></div>`;
        }).join('');
        messages.scrollTop = messages.scrollHeight;
    }

    function olderMessagesControl() {
        return hasOlderMessages
            ? '<button type="button" class="admin-chat-load-older" id="admin-load-older">Încarcă mesajele mai vechi</button>'
            : '';
    }

    async function loadOlderMessages() {
        if (!selectedId || !oldestMessageId || !hasOlderMessages || loadingOlder) return;
        loadingOlder = true;
        const control = document.getElementById('admin-load-older');
        if (control) control.outerHTML = '<div class="admin-chat-load-status" id="admin-load-older">Se încarcă...</div>';
        const previousHeight = messages.scrollHeight;
        try {
            const response = await fetch(`/admin/api/messages/conversations/${selectedId}?beforeId=${oldestMessageId}&limit=50`, {headers: {'Accept': 'application/json'}});
            if (!response.ok) throw new Error('Mesajele mai vechi nu au putut fi încărcate.');
            const payload = await response.json();
            hasOlderMessages = payload.length === 50;
            if (payload.length) oldestMessageId = payload[0].id;
            const conversation = conversations.find(item => item.conversationId === selectedId);
            const html = payload.map(message => messageHtml(message, conversation)).join('');
            messages.insertAdjacentHTML('afterbegin', olderMessagesControl() + html);
            document.querySelectorAll('#admin-load-older').forEach((item, index) => { if (index > 0) item.remove(); });
            messages.scrollTop = messages.scrollHeight - previousHeight;
        } catch (error) {
            const status = document.getElementById('admin-load-older');
            if (status) status.outerHTML = `<button type="button" class="admin-chat-load-older" id="admin-load-older">${escapeHtml(error.message)} Reîncearcă</button>`;
        } finally {
            loadingOlder = false;
        }
    }

    function messageHtml(message, conversation) {
        const side = message.senderId === conversation.userTwo.id ? ' user-two' : '';
        const text = message.contentText ? `<div>${escapeHtml(message.contentText).replace(/\n/g, '<br>')}</div>` : '';
        const image = message.imageUrl ? `<a href="${escapeHtml(message.imageUrl)}" target="_blank" rel="noopener"><img class="admin-message-image" src="${escapeHtml(message.imageUrl)}" alt="Imagine mesaj"></a>` : '';
        return `<div class="admin-message-row${side}"><div class="admin-message-bubble"><div class="admin-message-sender">${escapeHtml(message.senderName)}</div>${text}${image}<div class="admin-message-meta">${escapeHtml(formatTime(message.createdAt))}</div></div></div>`;
    }

    list.addEventListener('click', event => {
        const item = event.target.closest('[data-conversation-id]');
        if (item) selectConversation(Number(item.dataset.conversationId));
    });
    messages.addEventListener('click', event => {
        if (event.target.closest('#admin-load-older')) loadOlderMessages();
    });
    messages.addEventListener('scroll', () => {
        if (messages.scrollTop <= 40) loadOlderMessages();
    });
    document.getElementById('admin-messages-refresh').addEventListener('click', () => loadConversations(true));
    document.getElementById('admin-chat-back').addEventListener('click', () => root.classList.remove('chat-open'));
    window.addEventListener('admin-tab-changed', event => {
        if (event.detail.tab === 'messages') loadConversations();
    });
    if (new URLSearchParams(window.location.search).get('tab') === 'messages' || localStorage.getItem('adminUsersActiveTab') === 'messages') loadConversations();
})();
