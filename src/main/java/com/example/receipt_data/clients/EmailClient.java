package com.example.receipt_data.clients;

import com.example.receipt_data.DTO.reports.ReportOfUserDTO;
import com.example.receipt_data.config.feign.ClientsFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "email-client", url="${clients.email.url}", configuration = ClientsFeignConfiguration.class)
public interface EmailClient {
    @PostMapping("/send/report")
    ResponseEntity<Void> sendReport(ReportOfUserDTO reportOfUserDTO);
}
