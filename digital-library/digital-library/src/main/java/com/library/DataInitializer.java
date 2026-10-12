package com.library;

import com.library.model.Book;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Runs at startup: creates the default admin account and some sample books if the DB is empty. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository users;
    private final BookRepository books;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!users.existsByRole(User.Role.ADMIN)) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail("admin@library.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            users.save(admin);
        }
        if (books.count() == 0) {
            addBook("Clean Code", "Robert C. Martin", "9780132350884", "Programming", 3);
            addBook("Effective Java", "Joshua Bloch", "9780134685991", "Programming", 2);
            addBook("Head First Java", "Kathy Sierra", "9781491910771", "Programming", 4);
            addBook("Introduction to Algorithms", "Thomas H. Cormen", "9780262046305", "Computer Science", 2);
            addBook("Wings of Fire", "A. P. J. Abdul Kalam", "9788173711466", "Biography", 3);
            addBook("The Alchemist", "Paulo Coelho", "9780062315007", "Fiction", 1);
        }
    }

    private void addBook(String title, String author, String isbn, String category, int qty) {
        Book b = new Book();
        b.setTitle(title);
        b.setAuthor(author);
        b.setIsbn(isbn);
        b.setCategory(category);
        b.setTotalQuantity(qty);
        b.setAvailableQuantity(qty);
        books.save(b);
    }
}
