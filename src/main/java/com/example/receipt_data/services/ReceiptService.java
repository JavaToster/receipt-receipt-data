package com.example.receipt_data.services;

import com.example.receipt_data.DTO.receipt.ReceiptsDTO;
import com.example.receipt_data.DTO.reports.ReportOfUserDTO;
import com.example.receipt_data.DTO.statistics.DailyStatisticDTO;
import com.example.receipt_data.DTO.receipt.ReceiptDTO;
import com.example.receipt_data.DTO.statistics.StatisticDTO;
import com.example.receipt_data.DTO.statistics.Top3RatingDTO;
import com.example.receipt_data.DTO.user.UserDTO;
import com.example.receipt_data.clients.EmailClient;
import com.example.receipt_data.util.QRCode.QRCodeDecoder;
import com.example.receipt_data.clients.UserClient;
import com.example.receipt_data.exceptions.EntityIsExistException;
import com.example.receipt_data.exceptions.ReadingQRCodeDataException;
import com.example.receipt_data.models.Receipt;
import com.example.receipt_data.repositories.ReceiptRepository;
import com.example.receipt_data.util.Convertor;
import com.example.receipt_data.util.Sorter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptService {
    private static final DateTimeFormatter FORMATTER_FOR_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");
    private static final DateTimeFormatter FORMATTER_FOR_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final ReceiptRepository receiptRepository;
    private final QRCodeDecoder qrCodeDecoder;
    private final Convertor convertor;
    private final Sorter sorter;
    private final UserClient userClient;
    private final EmailClient emailClient;

    public ReceiptDTO decode(MultipartFile file){
        String valueOfQRCode;
        try{
            valueOfQRCode = qrCodeDecoder.readQRFromReceipt(file);
        }catch (Exception e){
            throw new ReadingQRCodeDataException("Could not read QRCode data, please try send again");
        }

        return decodeQRCodeData(valueOfQRCode);

    }

    @Transactional
    public void save(ReceiptDTO receiptDTO, long telegramIdOfOwner) {
        if(isExist(receiptDTO)){
            throw new EntityIsExistException("Receipt is exist");
        }
        Receipt receipt = convertor.convertToReceipt(receiptDTO);
        receipt.setOwnerId(telegramIdOfOwner);
        receiptRepository.save(receipt);
        receiptDTO.setId(receipt.getId());
    }

    //    t=20250630T1702&s=1057.00&fn=7380440801524581&i=659&fp=1350420190&n=1
    private ReceiptDTO decodeQRCodeData(String query) {
        ReceiptDTO receipt = new ReceiptDTO();
        List<String> pairs = splitQueryIntoPairs(query);

        for (String pair : pairs) {
            processKeyValuePair(pair, receipt);
        }

        return receipt;
    }

    private List<String> splitQueryIntoPairs(String query) {
        return Arrays.asList(query.split("&"));
    }

    private void processKeyValuePair(String pair, ReceiptDTO receipt) {
        int separator = pair.indexOf('=');
        if (separator == -1) return;

        String key = pair.substring(0, separator);
        String value = pair.substring(separator + 1);

        switch (key) {
            case "s" -> receipt.setSum(parseDouble(value));
            case "t" -> receipt.setCreationDate(parseDate(value));
            case "fn" -> receipt.setFn(value);
            case "fp" -> receipt.setFp(value);
            case "i" -> receipt.setI(value);
            case "n" -> receipt.setN(parseInt(value));
        }
    }

    private Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ReadingQRCodeDataException("Could not parse parse Double");
        }
    }

    private LocalDateTime parseDate(String value) {
        try {
            return parseDateTime(value); // предположим, что этот метод уже есть
        } catch (Exception e) {
            throw new ReadingQRCodeDataException("Could not parse parse time");
        }
    }

    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ReadingQRCodeDataException("Could not parse parse Integer");
        }
    }


    private LocalDateTime parseDateTime(String time) {
        LocalDateTime creationTime;
        try{
            creationTime = LocalDateTime.parse(time, FORMATTER_FOR_TIMESTAMP);
        }catch (DateTimeParseException e){
            creationTime = LocalDate.parse(time.split("T")[0], FORMATTER_FOR_DATE).atStartOfDay();
        }
        return creationTime;
    }

    public ReceiptDTO get(long id){
        Receipt receipt = receiptRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Receipt with this id not found, please try sending another id"));
        return convertor.convertToReceiptDTO(receipt);
    }

    public ReceiptsDTO findByUserTelegramIdAndSortByCreationDate(long telegramId){
        List<Receipt> receipts = receiptRepository.findAllByOwnerId(telegramId);
        List<Receipt> sortedReceiptsByCreationDate = sorter.sortByCreationDate(receipts);
        return new ReceiptsDTO(convertor.convertToReceiptDTO(sortedReceiptsByCreationDate));
    }

    public List<ReceiptDTO> findByMonth(long telegramId, int month) {
        List<Receipt> receipts = receiptRepository.findAllByOwnerId(telegramId);
        List<Receipt> boughtReceiptsInMonth = filterByMonth(receipts, month);
        return convertor.convertToReceiptDTO(boughtReceiptsInMonth);
    }

    private List<Receipt> filterByMonth(List<Receipt> allReceipts, int monthNumber){
        return allReceipts.stream().filter(receipt -> receipt.getCreationDate().getMonth().getValue() == monthNumber).toList();
    }

    private boolean isExist(ReceiptDTO receipt){
        return receiptRepository.existsByQrRawData(receipt.getQrRawData());
    }

    @Cacheable(value = "daily-statistic")
    public DailyStatisticDTO getDailyStatistic() {
        List<Top3RatingDTO> rating = receiptRepository.findTop3UserIdAndReceiptCount();

        List<Long> ids = getIdsFromTop3Rating(rating);

        List<UserDTO> users = userClient.getSeveralUsers(ids);

        Map<Long, UserDTO> mapOfUsers = users.stream()
                .collect(Collectors.toMap(UserDTO::getTelegramId, u -> u));

        List<StatisticDTO> statistic = rating.stream()
                .map(top -> {
                    UserDTO u = mapOfUsers.get(top.getOwner_id());
                    return new StatisticDTO(u.getUsername(), top.getCnt());
                })
                .toList();

        return new DailyStatisticDTO(statistic);
    }

    private List<Long> getIdsFromTop3Rating(List<Top3RatingDTO> top){
        return top.stream()
                .map(Top3RatingDTO::getOwner_id).toList();
    }

    public void sendReport(long userId){
        ReportOfUserDTO reportOfUserDTO = new ReportOfUserDTO();
        UserDTO userDTO = userClient.getUser(userId);
        reportOfUserDTO.setUserDTO(userDTO);
        long count = receiptRepository.countByOwnerId(userId);
        reportOfUserDTO.setReceiptsCount(count);

        emailClient.sendReport(reportOfUserDTO);
    }
}

