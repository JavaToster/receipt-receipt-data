package com.example.receipt_data.forExceptions.exceptions;

public class ReadingQRCodeDataException extends RuntimeException{
    public ReadingQRCodeDataException(String msg){
        super(msg);
    }
}
