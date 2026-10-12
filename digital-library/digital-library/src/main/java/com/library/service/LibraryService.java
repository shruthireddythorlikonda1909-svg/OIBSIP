package com.library.service;

import com.library.model.*;
import com.library.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * All the business rules live here: issuing, returning, fines, reservations, book management.
 * Every method is one database transaction - if an exception is thrown, nothing is saved.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LibraryService {

    private final BookRepository books;
    private final LoanRepository loans;
    private final ReservationRepository reservations;
    private final UserRepository users;

    // ======================= ADMIN: BOOKS =======================

    /** Adds a book (id == null) or edits an existing one. */
    public void saveBook(Long id, String title, String author, String isbn, String category, int quantity) {
        title = title.trim();
        author = author.trim();
        isbn = isbn.trim();
        category = category.trim();
        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || category.isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        Optional<Book> sameIsbn = books.findByIsbn(isbn);
        if (sameIsbn.isPresent() && !sameIsbn.get().getId().equals(id)) {
            throw new IllegalArgumentException("A book with this ISBN already exists.");
        }

        Book book;
        if (id == null) {
            book = new Book();
            book.setAvailableQuantity(quantity);
        } else {
            book = books.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found."));
            // keep "available" consistent: copies on the shelf change by the same amount as the total
            int available = book.getAvailableQuantity() + (quantity - book.getTotalQuantity());
            if (available < 0) {
                throw new IllegalArgumentException("Quantity is lower than the number of copies currently issued.");
            }
            book.setAvailableQuantity(available);
        }
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setTotalQuantity(quantity);
        books.save(book);
    }

    public void deleteBook(Long id) {
        if (loans.existsByBookId(id) || reservations.existsByBookId(id)) {
            throw new IllegalStateException(
                    "This book has issue/reservation history and cannot be deleted. Set its quantity to 0 instead.");
        }
        books.deleteById(id);
    }

    // ======================= ADMIN: MEMBERS & FINES =======================

    /** Blocks or unblocks a member. Blocked members cannot log in. */
    public boolean toggleMember(Long userId) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Member not found."));
        if (user.getRole() == User.Role.ADMIN) {
            throw new IllegalStateException("Admin accounts cannot be blocked.");
        }
        user.setEnabled(!user.isEnabled());
        return user.isEnabled();
    }

    public void markFinePaid(Long loanId) {
        Loan loan = loans.findById(loanId).orElseThrow(() -> new IllegalArgumentException("Record not found."));
        if (loan.getFineAmount() <= 0) {
            throw new IllegalStateException("This loan has no fine.");
        }
        loan.setFinePaid(true);
    }

    // ======================= USER: ISSUE / RETURN =======================

    public void issueBook(Long userId, Long bookId) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        Book book = books.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found."));

        if (loans.existsByUserIdAndFinePaidFalseAndFineAmountGreaterThan(userId, 0)) {
            throw new IllegalStateException("You have an unpaid fine. Please pay it at the library desk first.");
        }
        if (loans.existsByUserIdAndBookIdAndReturnDateIsNull(userId, bookId)) {
            throw new IllegalStateException("You have already issued this book.");
        }
        if (book.getAvailableQuantity() <= 0) {
            throw new IllegalStateException("No copies available right now. You can reserve this book instead.");
        }

        // Copies that are free but already promised to other members' reservations are off limits.
        Optional<Reservation> mine = reservations.findByUserIdAndBookIdAndStatus(userId, bookId, Reservation.Status.ACTIVE);
        long reservedByOthers = reservations.countByBookIdAndStatus(bookId, Reservation.Status.ACTIVE)
                - (mine.isPresent() ? 1 : 0);
        if (book.getAvailableQuantity() <= reservedByOthers) {
            throw new IllegalStateException("The available copies are held for members who reserved earlier.");
        }

        mine.ifPresent(r -> r.setStatus(Reservation.Status.FULFILLED));
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        loans.save(new Loan(book, user));
    }

    /** Returns the loan so the caller can show the fine (if any). */
    public Loan returnBook(Long loanId, Long userId) {
        Loan loan = loans.findById(loanId).orElseThrow(() -> new IllegalArgumentException("Loan not found."));
        if (!loan.getUser().getId().equals(userId)) {
            throw new IllegalStateException("This loan does not belong to you.");
        }
        if (loan.getReturnDate() != null) {
            throw new IllegalStateException("This book has already been returned.");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setFineAmount(loan.getOverdueDays() * Loan.FINE_PER_DAY);   // 0 if returned on time
        loan.setFinePaid(false);

        Book book = loan.getBook();
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        return loan;
    }

    // ======================= USER: RESERVATIONS =======================

    public void reserveBook(Long userId, Long bookId) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        Book book = books.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Book not found."));

        if (book.getAvailableQuantity() > 0) {
            throw new IllegalStateException("Copies are available - you can issue this book directly.");
        }
        if (loans.existsByUserIdAndBookIdAndReturnDateIsNull(userId, bookId)) {
            throw new IllegalStateException("You currently have this book issued.");
        }
        if (reservations.findByUserIdAndBookIdAndStatus(userId, bookId, Reservation.Status.ACTIVE).isPresent()) {
            throw new IllegalStateException("You have already reserved this book.");
        }
        reservations.save(new Reservation(book, user));
    }

    public void cancelReservation(Long reservationId, Long userId) {
        Reservation r = reservations.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (!r.getUser().getId().equals(userId)) {
            throw new IllegalStateException("This reservation does not belong to you.");
        }
        if (r.getStatus() != Reservation.Status.ACTIVE) {
            throw new IllegalStateException("Only active reservations can be cancelled.");
        }
        r.setStatus(Reservation.Status.CANCELLED);
    }
}
