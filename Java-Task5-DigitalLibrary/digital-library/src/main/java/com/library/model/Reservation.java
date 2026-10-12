package com.library.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** An advance booking on a book that has no copies available right now. */
@Entity
@Getter @Setter @NoArgsConstructor
public class Reservation {

    public enum Status { ACTIVE, FULFILLED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Book book;

    @ManyToOne(optional = false)
    private User user;

    private LocalDateTime reservedOn = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public Reservation(Book book, User user) {
        this.book = book;
        this.user = user;
    }
}
