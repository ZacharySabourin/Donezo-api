TRUNCATE TABLE todos RESTART IDENTITY CASCADE;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

-- Insert test users
INSERT INTO users (id, username, password_hash) VALUES 
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'testmctest', '$2a$12$8c8HzqrAoMs6ytuvSTnOP.okuOBu6zojRgqH9TFJDoOnVtr7qgOya'),
('12345678-7afd-42b5-a65b-3e21ea693ce2', 'john-test', 'lkrjqopweir19082341');

-- Insert test todos (referenced by tests)
INSERT INTO todos (user_id, text, position, completed) VALUES 
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'this is a todo', 0, true),
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'this is not a todo', 1, false),
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'this is another todo', 2, true),
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'this is a test todo', 3, false),
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'blah blah blah', 4, false),
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'I will get this done', 5, true),
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'Test values 1234', 6, true),
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'such test, very todo', 7, false),
('26248245-7afd-42b5-a65b-3e21ea693ce2', 'SQL testing is fun', 8, true),
('26248245-7afd-42b5-a65b-3e21ea693ce2', '890-s9df-0asdf98a-s09f8', 9, false);