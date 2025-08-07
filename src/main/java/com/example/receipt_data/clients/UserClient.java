package com.example.receipt_data.clients;

import com.example.receipt_data.DTO.user.UserDTO;
import com.example.receipt_data.configuration.UserClientFeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-client", url="${clients.user.url}", configuration = UserClientFeignConfiguration.class)
public interface UserClient {
    @GetMapping("/get/{telegramId}")
    UserDTO getUser(@PathVariable("telegramId") long telegramId);
}
