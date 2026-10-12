package com.library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** One issue of one book to one member. returnDate == null means the book is still out. */
@Entity
@Getter @Setter @NoArgsConstructor
public class Loan {

    public static final int LOAN_DAYS = 14;     // how long a member may keep a book
    public static final int FINE_PER_DAY = 5;   // Rs. per overdue day

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Book book;

    @ManyToOne(optional = false)
    private User user;

    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    private int fineAmount;       // fixed in at the moment of return
    private boolean finePaid;

    public Loan(Book book, User user) {
        this.book = book;
        this.user = user;
        this.issueDate = LocalDate.now();
        this.dueDate = issueDate.plusDays(LOAN_DAYS);
    }

    /** Days past the due date (until today if still issued, or until the return date). */
    public int getOverdueDays() {
        LocalDate end = (returnDate != null) ? returnDate : LocalDate.now();
        return (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }

    /** Fine accrued so far (if still issued) or the final fine (if returned). */
    public int getCurrentFine() {
        return (returnDate == null) ? getOverdueDays() * FINE_PER_DAY : fineAmount;
    }
}
