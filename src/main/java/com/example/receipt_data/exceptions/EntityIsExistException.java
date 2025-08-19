package com.example.receipt_data.exceptions;

public class EntityIsExistException extends RuntimeException{
    public EntityIsExistException(String msg){
        super(msg);
    }
}
