package com.example.receipt_data.forExceptions.exceptions;

public class EntityIsExistException extends RuntimeException{
    public EntityIsExistException(String msg){
        super(msg);
    }
}
