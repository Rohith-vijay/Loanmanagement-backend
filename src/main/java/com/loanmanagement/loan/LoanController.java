package com.loanmanagement.loan;

import com.loanmanagement.loan.dto.LoanRequest;
import com.loanmanagement.loan.dto.LoanResponse;
import com.loanmanagement.loan.dto.LoanSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@RequestBody LoanRequest request) {
        return ResponseEntity.ok(loanService.createLoan(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponse>> getLoansByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoansByUserId(userId));
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<List<LoanSummaryDTO>> getLoanSummariesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoanSummariesByUserId(userId));
    }
}
