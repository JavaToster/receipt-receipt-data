package com.example.receipt_data.util;

import com.example.receipt_data.DTO.ReceiptDTO;
import com.example.receipt_data.DTO.UserDTO;
import com.example.receipt_data.domainModels.User;
import com.example.receipt_data.models.Receipt;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class Convertor {

    private final ModelMapper modelMapper;

    public Receipt convertToReceipt(ReceiptDTO receiptDTO){
        return modelMapper.map(receiptDTO, Receipt.class);
    }

    public ReceiptDTO convertToReceiptDTO(Receipt receipt) {
        return modelMapper.map(receipt, ReceiptDTO.class);
    }

    public List<ReceiptDTO> convertToReceiptDTO(List<Receipt> receipts){
        return receipts.stream().map(this::convertToReceiptDTO).toList();
    }

    public com.example.receipt_data.domainModels.User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}
