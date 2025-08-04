package com.example.receipt_data.forExceptions;

import com.example.receipt_data.forExceptions.exceptions.EntityIsExistException;
import com.example.receipt_data.forExceptions.exceptions.ReadingQRCodeDataException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsHandler{
    @ExceptionHandler
    public ResponseEntity<ErrorMessage> exceptionHandle(ValidationException e){
        return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler
    public ResponseEntity<ErrorMessage> exceptionHandler(ReadingQRCodeDataException e){
        return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler
    public ResponseEntity<ErrorMessage> exceptionHandler(EntityNotFoundException e){
        return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler
    public ResponseEntity<ErrorMessage> exceptionHandler(EntityIsExistException e){
        return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}