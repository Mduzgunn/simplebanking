package com.eteration.simplebanking.controller;

import com.eteration.simplebanking.dto.*;
import com.eteration.simplebanking.model.InsufficientBalanceException;
import com.eteration.simplebanking.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account/v1")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Verilen hesap numarasına ait hesap bilgilerini getirir
     * @param accountNumber Hesap numarası
     * @return ResponseEntity<AccountDTO> Hesap bilgileri veya 404 hatası
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountNumber) {
        ApiResponse<AccountDTO> response = accountService.findAccount(accountNumber);
        if (!response.getSuccess()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response.getObject());
    }

    /**
     * Hesaba para yatırma işlemini gerçekleştirir
     * @param accountNumber Hesap numarası
     * @param request Para yatırma miktarını içeren TransactionDTO
     * @return ResponseEntity<TransactionStatus> İşlem durumu ve onay kodu
     */
    @PostMapping("/credit/{accountNumber}")
    public ResponseEntity<TransactionStatus> credit(@PathVariable String accountNumber, @RequestBody TransactionDTO request) {
        ApiResponse<String> response = accountService.credit(accountNumber, request.getAmount());
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(new TransactionStatus("ERROR", null));
        }
        return ResponseEntity.ok(new TransactionStatus("OK", response.getObject()));
    }

    /**
     * Hesaptan para çekme işlemini gerçekleştirir
     * @param accountNumber Hesap numarası
     * @param request Para çekme miktarını içeren TransactionDTO
     * @return ResponseEntity<TransactionStatus> İşlem durumu ve onay kodu
     * @throws InsufficientBalanceException Yetersiz bakiye durumunda
     */
    @PostMapping("/debit/{accountNumber}")
    public ResponseEntity<TransactionStatus> debit(@PathVariable String accountNumber, @RequestBody TransactionDTO request) throws InsufficientBalanceException {
        ApiResponse<String> response = accountService.debit(accountNumber, request.getAmount());
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(new TransactionStatus("ERROR", null));
        }
        return ResponseEntity.ok(new TransactionStatus("OK", response.getObject()));
    }

    /**
     * Telefon faturası ödeme işlemini gerçekleştirir
     * @param accountNumber Hesap numarası
     * @param request Fatura detaylarını içeren TransactionDTO
     * @return ResponseEntity<TransactionStatus> İşlem durumu ve onay kodu
     * @throws InsufficientBalanceException Yetersiz bakiye durumunda
     */
    @PostMapping("/bill-payment/{accountNumber}")
    public ResponseEntity<TransactionStatus> payPhoneBill(@PathVariable String accountNumber, @RequestBody TransactionDTO request) throws InsufficientBalanceException {
        ApiResponse<String> response = accountService.payPhoneBill(
            accountNumber,
            request.getType(),
            request.getApprovalCode(),
            request.getAmount()
        );
        if (!response.getSuccess()) {
            return ResponseEntity.badRequest().body(new TransactionStatus("ERROR", null));
        }
        return ResponseEntity.ok(new TransactionStatus("OK", response.getObject()));
    }
}
