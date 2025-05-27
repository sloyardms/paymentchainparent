package com.paymentchain.transactions.controller;

import com.paymentchain.transactions.entities.Transaction;
import com.paymentchain.transactions.repository.TransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transaction")
class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping()
    public List<Transaction> list() {
        return transactionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> get(@PathVariable(name = "id") long id) {
        return transactionRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/transactions")
    public List<Transaction> get(@RequestParam(name = "ibanAccount") String ibanAccount) {
        return transactionRepository.findByIbanAccount(ibanAccount);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable(name = "id") long id, @RequestBody Transaction input) {
        Optional<Transaction> find = transactionRepository.findById(id);
        if (find.isPresent()) {
            Transaction transaction = find.get();

            transaction.setAmount(input.getAmount());
            transaction.setChannel(input.getChannel());
            transaction.setDate(input.getDate());
            transaction.setDescription(input.getDescription());
            transaction.setFee(input.getFee());
            transaction.setIbanAccount(input.getIbanAccount());
            transaction.setReference(input.getReference());
            transaction.setStatus(input.getStatus());

            Transaction save = transactionRepository.save(transaction);
            return ResponseEntity.ok(save);
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Transaction input) {
        Transaction save = transactionRepository.save(input);
        return ResponseEntity.ok(save);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") long id) {
        Optional<Transaction> findById = transactionRepository.findById(id);
        findById.ifPresent(transactionRepository::delete);
        return ResponseEntity.ok().build();
    }
}
