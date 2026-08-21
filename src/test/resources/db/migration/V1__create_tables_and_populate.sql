-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email varchar(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Todos table
CREATE TABLE IF NOT EXISTS todos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    completed BOOLEAN DEFAULT false,
    position INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_todos_user_position ON todos(user_id, position);

-- Create test user
INSERT INTO users (id, email, password_hash) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'test@test.com', 'asdfasdf12345678');

-- Create test todos
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'this is a todo', 0, true);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'this is not a todo', 1, false);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'this is another todo', 2, true);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'this is a test todo', 3, false);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'blah blah blah', 4, false);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'I will get this done', 5, true);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'Test values 1234', 6, true);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'such test, very todo', 7, false);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', 'SQL testing is fun', 8, true);
INSERT INTO todos (user_id, text, position, completed) VALUES ('26248245-7afd-42b5-a65b-3e21ea693ce2', '890-s9df-0asdf98a-s09f8', 9, false);
