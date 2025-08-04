package com.example.receipt_data.controllers;


import com.example.receipt_data.DTO.ReceiptDTO;
import com.example.receipt_data.DTO.ReceiptsDTO;
import com.example.receipt_data.services.ReceiptService;
import com.example.receipt_data.validators.ReceiptValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ReceiptValidator receiptValidator;

    @PutMapping(consumes = "multipart/form-data", value = "/add")
    public ResponseEntity<ReceiptDTO> addNewReceiptAndDecodeQRCode(@RequestPart("file") MultipartFile file, Principal principal) throws Exception {
        receiptValidator.validate(file);

        ReceiptDTO receiptDataDTO = receiptService.decode(file);
        receiptService.save(receiptDataDTO, Long.parseLong(principal.getName()));

        return new ResponseEntity<>(receiptDataDTO, HttpStatus.OK);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ReceiptDTO> get(@PathVariable("id") long id){
        ReceiptDTO receiptDataDTO = receiptService.get(id);
        return new ResponseEntity<>(receiptDataDTO, HttpStatus.OK);
    }

    @GetMapping("/get")
    public ResponseEntity<ReceiptsDTO> getUserReceipts(Principal principal){
        List<ReceiptDTO> receipts = receiptService.findByUserTelegramIdAndSortByCreationDate(Long.parseLong(principal.getName()));
        return new ResponseEntity<>(new ReceiptsDTO(receipts), HttpStatus.OK);
    }

    @GetMapping("/get/month/{month_number}")
    public ResponseEntity<ReceiptsDTO> getReceiptsByMonth(Principal principal, @PathVariable("month_number") int monthNum){
        List<ReceiptDTO> receipts = receiptService.findByMonth(Long.parseLong(principal.getName()), monthNum);
        return new ResponseEntity<>(new ReceiptsDTO(receipts), HttpStatus.OK);
    }
}
