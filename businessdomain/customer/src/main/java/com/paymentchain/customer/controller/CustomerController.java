package com.paymentchain.customer.controller;

import com.paymentchain.customer.business.transactions.BusinessTransaction;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.exceptions.BusinessRuleException;
import com.paymentchain.customer.repository.CustomerRepository;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.Optional;

@RestController
@RequestMapping("/customer/V1")
class CustomerController {

    private final CustomerRepository customerRepository;
    private final Environment environment;
    private final BusinessTransaction businessTransaction;

    CustomerController(CustomerRepository customerRepository, Environment environment, BusinessTransaction businessTransaction) {
        this.customerRepository = customerRepository;
        this.environment = environment;
        this.businessTransaction = businessTransaction;
    }

    @GetMapping("/check")
    public String check(){
        return "Customer Microservice is running!: " + environment.getProperty("spring.profiles.active");
    }

    @GetMapping()
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(customerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return new ResponseEntity<>(customer.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> post(@RequestBody Customer input) throws BusinessRuleException, UnknownHostException {
        Customer customer = businessTransaction.post(input);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable("id") long id, @RequestBody Customer input) {
        Optional<Customer> find = customerRepository.findById(id);
        if (find.isPresent()) {
            Customer customer = find.get();
            customer.setPhone(input.getPhone());
            customer.setCode(input.getCode());
            customer.setIban(input.getIban());
            customer.setNames(input.getNames());
            customer.setAddress(input.getAddress());
            customer.setSurname(input.getSurname());

            customer.getProducts().clear();
            if (input.getProducts() != null) {
                for (CustomerProduct p : input.getProducts()) {
                    p.setCustomer(customer);
                    customer.getProducts().add(p);
                }
            }
            return ResponseEntity.ok(customerRepository.save(customer));
        } else {
            return ResponseEntity.ok(customerRepository.save(input));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        Optional<Customer> findById = customerRepository.findById(id);
        if (findById.isPresent()) {
            customerRepository.deleteById(id);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/full")
    public ResponseEntity<?> getByCode(@RequestParam("id") String code) {
        Customer customer = businessTransaction.getByCode(code);
        return ResponseEntity.ok(customer);
    }

}
