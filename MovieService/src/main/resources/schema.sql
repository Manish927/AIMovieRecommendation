-- MySQL Database Schema for Movie Recommendation System
-- Run this script to create the database and tables

CREATE DATABASE IF NOT EXISTS movieBooking;
USE movieBooking;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(50) UNIQUE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    phone_verification_code VARCHAR(10),
    password_reset_token VARCHAR(255),
    password_reset_expires DATETIME,
    last_login DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Movies Table
CREATE TABLE IF NOT EXISTS movies (
    movie_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    genre VARCHAR(255),
    director VARCHAR(255),
    cast TEXT COMMENT 'JSON array stored as text: ["Actor1", "Actor2"]',
    release_date DATE,
    duration INT COMMENT 'Duration in minutes',
    poster_url VARCHAR(500),
    rating DECIMAL(3,2) DEFAULT 0.00 COMMENT 'Average rating',
    total_ratings INT DEFAULT 0 COMMENT 'Number of ratings',
    language VARCHAR(50),
    certification VARCHAR(10) COMMENT 'PG, PG-13, R, etc.',
    INDEX idx_title (title),
    INDEX idx_genre (genre),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Ratings Table
CREATE TABLE IF NOT EXISTS ratings (
    rating_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_movie (user_id, movie_id),
    INDEX idx_user_id (user_id),
    INDEX idx_movie_id (movie_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Theaters Table
CREATE TABLE IF NOT EXISTS theaters (
    theater_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    phone VARCHAR(50),
    total_screens INT DEFAULT 0,
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Theater Movies (Showtimes) Table
CREATE TABLE IF NOT EXISTS theater_movies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    theater_id INT NOT NULL,
    movie_id INT NOT NULL,
    screen_number INT NOT NULL,
    show_time DATETIME NOT NULL,
    ticket_price DECIMAL(10,2) COMMENT 'Base price',
    dynamic_price DECIMAL(10,2) COMMENT 'Current dynamic price',
    base_price DECIMAL(10,2) COMMENT 'Original base price for reference',
    predicted_demand DECIMAL(3,2) COMMENT 'Predicted demand score (0.0 to 1.0)',
    available_seats INT DEFAULT 0,
    total_seats INT DEFAULT 0,
    last_price_update DATETIME COMMENT 'When price was last updated',
    FOREIGN KEY (theater_id) REFERENCES theaters(theater_id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
    INDEX idx_theater_id (theater_id),
    INDEX idx_movie_id (movie_id),
    INDEX idx_show_time (show_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Bookings Table (for demand prediction)
CREATE TABLE IF NOT EXISTS bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    theater_movie_id INT NOT NULL,
    number_of_seats INT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL COMMENT 'Price before taxes',
    tax_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT 'GST/Tax amount',
    service_charge DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Service charge',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Discount if any',
    total_price DECIMAL(10,2) NOT NULL COMMENT 'Final price including taxes',
    price_per_ticket DECIMAL(10,2) NOT NULL,
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, CONFIRMED, CANCELLED, COMPLETED',
    reservation_expires_at DATETIME COMMENT 'When reservation expires if payment not completed',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (theater_movie_id) REFERENCES theater_movies(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_theater_movie_id (theater_movie_id),
    INDEX idx_booking_time (booking_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Data
INSERT INTO users (user_id, name, email, phone, password) VALUES
(1, 'John Doe', 'john.doe@example.com', '123-456-7890', 'password123'),
(2, 'Jane Smith', 'jane.smith@example.com', '987-654-3210', 'password123'),
(3, 'Bob Johnson', 'bob.johnson@example.com', '555-123-4567', 'password123');

INSERT INTO movies (movie_id, title, description, genre, director, cast, release_date, duration, rating, total_ratings, language, certification) VALUES
(1, 'The Matrix', 'A computer hacker learns about the true nature of reality', 'Action,Sci-Fi', 'Lana Wachowski', '["Keanu Reeves", "Laurence Fishburne", "Carrie-Anne Moss"]', '1999-03-31', 136, 4.5, 100, 'English', 'R'),
(2, 'Inception', 'A thief who steals corporate secrets through dream-sharing technology', 'Action,Sci-Fi,Thriller', 'Christopher Nolan', '["Leonardo DiCaprio", "Marion Cotillard", "Tom Hardy"]', '2010-07-16', 148, 4.7, 150, 'English', 'PG-13'),
(3, 'The Dark Knight', 'Batman faces the Joker in Gotham City', 'Action,Crime,Drama', 'Christopher Nolan', '["Christian Bale", "Heath Ledger", "Aaron Eckhart"]', '2008-07-18', 152, 4.8, 200, 'English', 'PG-13'),
(4, 'Pulp Fiction', 'The lives of two mob hitmen, a boxer, and more intertwine', 'Crime,Drama', 'Quentin Tarantino', '["John Travolta", "Samuel L. Jackson", "Uma Thurman"]', '1994-10-14', 154, 4.6, 120, 'English', 'R'),
(5, 'Interstellar', 'A team of explorers travel through a wormhole in space', 'Adventure,Drama,Sci-Fi', 'Christopher Nolan', '["Matthew McConaughey", "Anne Hathaway", "Jessica Chastain"]', '2014-11-07', 169, 4.6, 180, 'English', 'PG-13');

INSERT INTO ratings (user_id, movie_id, rating, review) VALUES
(1, 1, 5, 'Amazing movie!'),
(1, 2, 5, 'Mind-blowing concept'),
(1, 3, 4, 'Great action sequences'),
(2, 2, 5, 'One of the best movies ever'),
(2, 4, 4, 'Classic Tarantino'),
(2, 5, 5, 'Emotional and visually stunning'),
(3, 1, 4, 'Revolutionary for its time'),
(3, 3, 5, 'Perfect superhero movie'),
(3, 5, 4, 'Great sci-fi');

INSERT INTO theaters (theater_id, name, address, city, state, zip_code, phone, total_screens) VALUES
(1, 'Cinema World', '123 Main Street', 'New York', 'NY', '10001', '555-0101', 8),
(2, 'Movie Palace', '456 Broadway', 'Los Angeles', 'CA', '90001', '555-0102', 12),
(3, 'Star Theater', '789 Oak Avenue', 'Chicago', 'IL', '60601', '555-0103', 6);

INSERT INTO theater_movies (theater_id, movie_id, screen_number, show_time, ticket_price, base_price, dynamic_price, available_seats, total_seats, predicted_demand) VALUES
(1, 1, 1, '2024-01-15 19:00:00', 12.50, 12.50, 12.50, 45, 50, 0.75),
(1, 2, 2, '2024-01-15 19:30:00', 13.00, 13.00, 13.00, 30, 50, 0.85),
(1, 3, 3, '2024-01-15 20:00:00', 12.50, 12.50, 12.50, 40, 50, 0.80),
(2, 2, 1, '2024-01-15 18:00:00', 14.00, 14.00, 14.00, 35, 50, 0.70),
(2, 4, 2, '2024-01-15 20:30:00', 11.50, 11.50, 11.50, 48, 50, 0.60),
(3, 5, 1, '2024-01-15 19:00:00', 13.50, 13.50, 13.50, 25, 50, 0.90);

-- Admins Table
CREATE TABLE IF NOT EXISTS admins (
    admin_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) DEFAULT 'ADMIN' COMMENT 'ADMIN, SUPER_ADMIN',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Bookings (for demand prediction)
-- Note: These bookings include tax calculations (18% GST + 5% service charge)
INSERT INTO bookings (user_id, theater_movie_id, number_of_seats, base_price, tax_amount, service_charge, discount_amount, total_price, price_per_ticket, booking_time, status) VALUES
(1, 1, 2, 25.00, 4.50, 1.25, 0.00, 30.75, 12.50, '2024-01-10 10:00:00', 'CONFIRMED'),
(2, 2, 3, 39.00, 7.02, 1.95, 0.00, 47.97, 13.00, '2024-01-11 14:30:00', 'CONFIRMED'),
(3, 3, 1, 12.50, 2.25, 0.63, 0.00, 15.38, 12.50, '2024-01-12 09:15:00', 'CONFIRMED'),
(1, 2, 2, 26.00, 4.68, 1.30, 0.00, 31.98, 13.00, '2024-01-13 16:20:00', 'CONFIRMED');

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'WALLET', 'NET_BANKING') NOT NULL,
    payment_status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    transaction_id VARCHAR(255) UNIQUE COMMENT 'Payment gateway transaction ID',
    gateway_response TEXT COMMENT 'Response from payment gateway',
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    refund_amount DECIMAL(10,2) DEFAULT 0.00,
    refund_date DATETIME,
    refund_reason TEXT,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_transaction_id (transaction_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_payment_date (payment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Admin Users (default password: admin123)
INSERT INTO admins (admin_id, username, password, email, role, is_active) VALUES
(1, 'admin', 'admin123', 'admin@moviebooking.com', 'ADMIN', TRUE),
(2, 'superadmin', 'superadmin123', 'superadmin@moviebooking.com', 'SUPER_ADMIN', TRUE);

