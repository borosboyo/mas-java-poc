-- Create tenants table in master database
CREATE TABLE IF NOT EXISTS tenants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    database_name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
    author_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_author FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_books_author_id ON books(author_id);
CREATE INDEX idx_books_genre ON books(genre);
CREATE INDEX idx_authors_name ON authors(name);
CREATE INDEX idx_users_username ON users(username);

-- Insert sample data
INSERT INTO users (username, password, email, role, enabled) VALUES
('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@example.com', 'ADMIN', true),
('user', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'user@example.com', 'USER', true);

INSERT INTO authors (name, birth_date) VALUES
('George Orwell', '1903-06-25'),
('J.K. Rowling', '1965-07-31'),
('J.R.R. Tolkien', '1892-01-03');

INSERT INTO books (title, genre, page_count, language, publication_date, author_id) VALUES
('1984', 'Dystopian', 328, 'English', '1949-06-08', 1),
('Animal Farm', 'Political Satire', 112, 'English', '1945-08-17', 1),
('Harry Potter and the Philosopher''s Stone', 'Fantasy', 223, 'English', '1997-06-26', 2),
('The Hobbit', 'Fantasy', 310, 'English', '1937-09-21', 3),
('The Lord of the Rings', 'Fantasy', 1178, 'English', '1954-07-29', 3);

