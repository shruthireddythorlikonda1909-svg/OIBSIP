package com.library.repository;

import com.library.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByReservedOnDesc(Long userId);

    Optional<Reservation> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, Reservation.Status status);

    long countByBookIdAndStatus(Long bookId, Reservation.Status status);

    boolean existsByBookId(Long bookId);
}
