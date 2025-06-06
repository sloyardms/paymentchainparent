## Code exercises completed during the course Microservices and REST APIs with Spring Boot, OAuth2, and Docker by Carlos Adrian Soto Botero (Udemy)

## Main commands and urls

### Customers

1. Swagger interface: http://localhost:8081/swagger.html
2. REST mapping: http://localhost:8081/customer/check
3. Access to H2 database: http://localhost:8081/h2-console
4. REST config server: http://localhost:8888/customer-dev/development

### Products

1. Swagger interface: http://localhost:8083/swagger.html
2. REST mapping: http://localhost:8083/product/check
3. Access to H2 database: http://localhost:8083/h2-console
4. REST config server: http://localhost:8888/product-dev/development

### Transactions

1. Swagger interface: http://localhost:8082/swagger.html
2. REST mapping: http://localhost:8082/transaction/check
3. Access to H2 database: http://localhost:8082/h2-console
4. REST config server: http://localhost:8888/transaction-dev/development

1. * Execute jar form terminal, passing spring boot profile, change the path, and change the profile by your desired *
```shell
 java -jar  -Dspring.cloud.config.name=customers -Dspring.cloud.config.profile=local change_path/paymentchainparent/businessdomain/customer/target/customer-0.0.1-SNAPSHOT.jar
```