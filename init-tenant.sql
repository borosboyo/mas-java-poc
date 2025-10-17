-- Initialize tenant database schema
-- This script runs on each tenant database (tenant_company1, tenant_company2, etc.)

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create authors table
CREATE TABLE IF NOT EXISTS authors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create books table
CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    genre VARCHAR(100),
    page_count INTEGER,
    language VARCHAR(50),
    publication_date DATE,
    isbn VARCHAR(20),
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_books_author_id ON books(author_id);
CREATE INDEX IF NOT EXISTS idx_books_genre ON books(genre);
CREATE INDEX IF NOT EXISTS idx_authors_name ON authors(name);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Insert default admin user
-- Password is 'pass' (bcrypt encoded)
INSERT INTO users (username, password, email, role, enabled)
VALUES ('admin', '$2a$12$8LOsmw8jcp/.GCg6pp7KDOG172e2GcSY9pB.9hpC2L3mN7RgRMWti', 'admin@tenant.com', 'ADMIN', true)
ON CONFLICT (username) DO NOTHING;

-- Insert sample authors
INSERT INTO authors (name, birth_date) VALUES
('George Orwell', '1903-06-25'),
('J.K. Rowling', '1965-07-31'),
('J.R.R. Tolkien', '1892-01-03')
ON CONFLICT DO NOTHING;

-- Insert sample books (only if authors exist)
INSERT INTO books (title, genre, page_count, language, publication_date, author_id)
SELECT '1984', 'Dystopian', 328, 'English', '1949-06-08', id FROM authors WHERE name = 'George Orwell' LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO books (title, genre, page_count, language, publication_date, author_id)
SELECT 'Animal Farm', 'Political Satire', 112, 'English', '1945-08-17', id FROM authors WHERE name = 'George Orwell' LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO books (title, genre, page_count, language, publication_date, author_id)
SELECT 'Harry Potter and the Philosopher''s Stone', 'Fantasy', 223, 'English', '1997-06-26', id FROM authors WHERE name = 'J.K. Rowling' LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO books (title, genre, page_count, language, publication_date, author_id)
SELECT 'The Hobbit', 'Fantasy', 310, 'English', '1937-09-21', id FROM authors WHERE name = 'J.R.R. Tolkien' LIMIT 1
ON CONFLICT DO NOTHING;

INSERT INTO books (title, genre, page_count, language, publication_date, author_id)
SELECT 'The Lord of the Rings', 'Fantasy', 1178, 'English', '1954-07-29', id FROM authors WHERE name = 'J.R.R. Tolkien' LIMIT 1
ON CONFLICT DO NOTHING;

