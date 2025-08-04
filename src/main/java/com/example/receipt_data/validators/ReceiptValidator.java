package com.example.receipt_data.validators;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ReceiptValidator {
    public void validate(MultipartFile file){
        if (file.isEmpty() || !isValidImageType(file)){
            throw new ValidationException("Incorrect file type!");
        }
    }

    private boolean isValidImageType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/jpg")
        );
    }
}
