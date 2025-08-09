package com.example.receipt_data.controllers;


import com.example.receipt_data.DTO.statistics.DailyStatisticDTO;
import com.example.receipt_data.DTO.receipt.ReceiptDTO;
import com.example.receipt_data.DTO.receipt.ReceiptsDTO;
import com.example.receipt_data.services.ReceiptService;
import com.example.receipt_data.validators.ReceiptValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

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

        return ResponseEntity.ok(receiptDataDTO);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ReceiptDTO> get(@PathVariable("id") long id){
        ReceiptDTO receiptDataDTO = receiptService.get(id);
        return ResponseEntity.ok(receiptDataDTO);
    }

    @GetMapping("/get")
    public ResponseEntity<ReceiptsDTO> getUserReceipts(Principal principal){
        ReceiptsDTO receipts = receiptService.findByUserTelegramIdAndSortByCreationDate(Long.parseLong(principal.getName()));
        return ResponseEntity.ok(receipts);
    }

    @GetMapping("/get/daily-statistic")
    public ResponseEntity<DailyStatisticDTO> getDailyStatistic(){
        DailyStatisticDTO statistic = receiptService.getDailyStatistic();

        return new ResponseEntity<>(statistic, HttpStatus.OK);
    }
}
