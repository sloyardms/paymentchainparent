package com.paymentchain.customer.business.transactions;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.exceptions.BusinessRuleException;
import com.paymentchain.customer.repository.CustomerRepository;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BusinessTransaction {

    private final CustomerRepository customerRepository;
    private final WebClient.Builder webClientBuilder;

    public BusinessTransaction(CustomerRepository customerRepository, WebClient.Builder webClientBuilder) {
        this.customerRepository = customerRepository;
        this.webClientBuilder = webClientBuilder;
    }

    //webClient requires HttpClient library to work properly
    HttpClient client = HttpClient.create()
            //Connection Timeout: is a period within which a connection between a client and a server must be established
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            //Response Timeout: The maximum time we wait to receive a response after sending a request
            .responseTimeout(Duration.ofSeconds(1))
            //Read Timeout: A read timeout occurs when no data was read within a certain period of time
            //Write Timeout: A write timeout occurs when a write operation cannot finish at a specific time
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                conn.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    public Customer getByCode(String code) {
        Customer customer = customerRepository.findByCode(code);
        if (customer != null) {

            //for each product, get the product name
            customer.getProducts().forEach(product -> {
                try {
                    product.setProductName(getProductName(product.getProductId()));
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            });

            //find all transactions by a customer iban
            customer.setTransactions(getTransactions(customer.getIban()));
        }
        return customer;
    }

    /**
     * Call Producto Microservice, find a product by Id and return the name of the product
     *
     * @param id Product Id
     * @return Product Name
     */
    public String getProductName(long id) throws UnknownHostException{
        String name;
        try {
            WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                    .baseUrl("http://PRODUCT/business/product/V1")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", "http://PRODUCT/business/product/V1/"))
                    .build();
            JsonNode block = build.method(HttpMethod.GET).uri("/{id}", id)
                    .retrieve().bodyToMono(JsonNode.class).block();
            name = block.get("name").asText();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "";
            } else {
                throw new UnknownHostException(ex.getMessage());
            }
        }
        return name;
    }

    /**
     * Call Transaction Microservice, find all transactions by a customer iban
     *
     * @param iban Customer Iban
     * @return List of Transactions
     */
    public List<?> getTransactions(String iban) {
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://TRANSACTIONS/business/transaction/V1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://TRANSACTIONS/business/transaction/V1/"))
                .build();

        return build.method(HttpMethod.GET).uri(uriBuilder -> uriBuilder
                        .path("/customer/transactions")
                        .queryParam("ibanAccount", iban)
                        .build())
                .retrieve().bodyToFlux(Object.class).collectList().block();
    }

    public Customer post(Customer input) throws BusinessRuleException, UnknownHostException {
        if(input.getProducts() != null){
            for (CustomerProduct product : input.getProducts()) {
                String productName = getProductName(product.getProductId());

                if (productName.isBlank()) {
                    //Product doesnt exists
                    throw new BusinessRuleException("1025", "Product not found with ID " + product.getProductId(), HttpStatus.PRECONDITION_FAILED);
                } else {
                    product.setCustomer(input);
                }
            }
        }
        return customerRepository.save(input);
    }


}
