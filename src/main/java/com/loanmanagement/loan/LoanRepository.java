package com.loanmanagement.loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId);

    Page<Loan> findByUserId(Long userId, Pageable pageable);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    List<Loan> findByStatus(LoanStatus status);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.status = :status")
    long countByStatus(@Param("status") LoanStatus status);

    @Query("SELECT SUM(l.principalAmount) FROM Loan l WHERE l.status IN ('ACTIVE', 'CLOSED')")
    BigDecimal totalDisbursedAmount();

    @Query("SELECT SUM(l.remainingBalance) FROM Loan l WHERE l.status = 'ACTIVE'")
    BigDecimal totalOutstandingBalance();

    @Query("SELECT l FROM Loan l WHERE l.user.id = :userId AND l.status = :status")
    List<Loan> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") LoanStatus status);

    @Query("SELECT COUNT(l) FROM Loan l")
    long countAllLoans();
}
