package com.library.repository;

import com.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserIdOrderByIssueDateDescIdDesc(Long userId);

    /** All books currently out, soonest due date first. */
    List<Loan> findByReturnDateIsNullOrderByDueDateAsc();

    /** Every loan that produced a fine (paid or unpaid). */
    List<Loan> findByFineAmountGreaterThanOrderByReturnDateDesc(int amount);

    boolean existsByUserIdAndBookIdAndReturnDateIsNull(Long userId, Long bookId);

    boolean existsByUserIdAndFinePaidFalseAndFineAmountGreaterThan(Long userId, int amount);

    boolean existsByBookId(Long bookId);
}
