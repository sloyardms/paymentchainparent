package com.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/customer")
class CustomerController {

    private final CustomerRepository customerRepository;
    private final WebClient.Builder webClientBuilder;

    public CustomerController(CustomerRepository customerRepository, WebClient.Builder webClientBuilder) {
        this.customerRepository = customerRepository;
        this.webClientBuilder = webClientBuilder;
    }

    //webClient requires HttpClient library to work properly
    HttpClient client = HttpClient.create()
            //Connection Timeout: is a period within which a connection between a client and a server must be established
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            //Response Timeout: The maximum time we wait to receive a response after sending a request
            .responseTimeout(Duration.ofSeconds(1))
            //Read Timeout: A read timeout occurs when no data was read within a certain period of time
            //Write Timeout: A write timeout occurs when a write operation cannot finish at a specific time
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                conn.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

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
    public ResponseEntity<?> post(@RequestBody Customer input) {
        input.getProducts().forEach(product -> product.setCustomer(input));
        return ResponseEntity.ok(customerRepository.save(input));
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
        Customer customer = customerRepository.findByCode(code);
        if (customer != null) {

            //for each product, get the product name
            customer.getProducts().forEach(product -> product.setProductName(getProductName(product.getProductId())));

            //find all transactions by a customer iban
            customer.setTransactions(getTransactions(customer.getIban()));

            return ResponseEntity.ok(customer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Call Producto Microservice, find a product by Id and return the name of the product
     *
     * @param id Product Id
     * @return Product Name
     */
    private String getProductName(long id) {
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8083/product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8083/product/"))
                .build();
        JsonNode block = build.method(HttpMethod.GET).uri("/{id}", id)
                .retrieve().bodyToMono(JsonNode.class).block();
        return block.get("name").asText();
    }

    /**
     * Call Transaction Microservice, find all transactions by a customer iban
     *
     * @param iban Customer Iban
     * @return List of Transactions
     */
    private List<?> getTransactions(String iban) {
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8082/transaction")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8082/transaction/"))
                .build();

        List<?> transactions = build.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
                        .path("/customer/transactions")
                        .queryParam("ibanAccount", iban)
                        .build())
                .retrieve().bodyToFlux(Object.class).collectList().block();
        return transactions;
    }
}
