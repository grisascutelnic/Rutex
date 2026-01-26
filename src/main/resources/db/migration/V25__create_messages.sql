CREATE TABLE IF NOT EXISTS conversations (
    id BIGSERIAL PRIMARY KEY,
    user_one_id BIGINT NOT NULL,
    user_two_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    last_message_id BIGINT,
    CONSTRAINT fk_conversation_user_one FOREIGN KEY (user_one_id) REFERENCES users(id),
    CONSTRAINT fk_conversation_user_two FOREIGN KEY (user_two_id) REFERENCES users(id),
    CONSTRAINT uq_conversation_pair UNIQUE (user_one_id, user_two_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content_text TEXT,
    image_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_message_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

ALTER TABLE conversations
    ADD CONSTRAINT fk_conversation_last_message
    FOREIGN KEY (last_message_id) REFERENCES messages(id);

CREATE TABLE IF NOT EXISTS message_reactions (
    id BIGSERIAL PRIMARY KEY,
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    emoji VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reaction_message FOREIGN KEY (message_id) REFERENCES messages(id),
    CONSTRAINT fk_reaction_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_reaction_user UNIQUE (message_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_conversation_user_one ON conversations(user_one_id);
CREATE INDEX IF NOT EXISTS idx_conversation_user_two ON conversations(user_two_id);
CREATE INDEX IF NOT EXISTS idx_message_conversation_id ON messages(conversation_id, id DESC);
CREATE INDEX IF NOT EXISTS idx_message_sender_id ON messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_reaction_message_id ON message_reactions(message_id);
