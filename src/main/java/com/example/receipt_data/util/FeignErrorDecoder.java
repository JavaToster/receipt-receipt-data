package com.example.receipt_data.util;

import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.validation.ValidationException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());

        if (status == HttpStatus.BAD_REQUEST){
            return new ValidationException("Validation error from email service");
        }
        return defaultDecoder.decode(s, response);
    }
}
