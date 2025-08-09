package com.example.receipt_data.controllers;

import com.example.receipt_data.DTO.ErrorMessageDTO;
import com.example.receipt_data.forExceptions.exceptions.EntityIsExistException;
import com.example.receipt_data.forExceptions.exceptions.ReadingQRCodeDataException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionsController {
    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> exceptionHandle(ValidationException e){
        return new ResponseEntity<>(new ErrorMessageDTO(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> exceptionHandler(ReadingQRCodeDataException e){
        return new ResponseEntity<>(new ErrorMessageDTO(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> exceptionHandler(EntityNotFoundException e){
        return new ResponseEntity<>(new ErrorMessageDTO(e.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler
    public ResponseEntity<ErrorMessageDTO> exceptionHandler(EntityIsExistException e){
        return new ResponseEntity<>(new ErrorMessageDTO(e.getMessage()), HttpStatus.BAD_REQUEST);
    }
}