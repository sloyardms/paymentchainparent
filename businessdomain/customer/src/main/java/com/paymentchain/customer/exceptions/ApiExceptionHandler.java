package com.paymentchain.customer.exceptions;

import com.paymentchain.customer.common.StandarizedApiExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnknownHostException(Exception ex){
        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse("TECNICO", "Input Output Error", "1024", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(standarizedApiExceptionResponse);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<?> handleUnknownHostException(BusinessRuleException ex){
        StandarizedApiExceptionResponse standarizedApiExceptionResponse = new StandarizedApiExceptionResponse("BUSINESS", "Validation error", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(standarizedApiExceptionResponse);
    }

}
