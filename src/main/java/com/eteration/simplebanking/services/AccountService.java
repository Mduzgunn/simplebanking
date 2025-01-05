package com.eteration.simplebanking.services;

import com.eteration.simplebanking.dto.ApiResponse;
import com.eteration.simplebanking.dto.AccountDTO;
import com.eteration.simplebanking.mapper.AccountMapper;
import com.eteration.simplebanking.model.*;
import com.eteration.simplebanking.repository.AccountRepository;
import com.eteration.simplebanking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private LogService logService;

    /**
     * Hesap bilgilerini sorgular
     * @param accountNumber Hesap numarası
     * @return ApiResponse<AccountDTO> Hesap bilgileri ve işlem durumu
     */
    public ApiResponse<AccountDTO> findAccount(String accountNumber) {
        try {
            Account account = accountRepository.findByAccountNumber(accountNumber);
            if (account == null) {
                logService.logError("Account not found: " + accountNumber, this.getClass().getSimpleName(), "findAccount", new RuntimeException("Account not found"));
                return new ApiResponse<>(false, "Account not found", null);
            }
            AccountDTO accountDTO = AccountMapper.toDTO(account);
            logService.logInfo("Account found: " + accountNumber, this.getClass().getSimpleName(), "findAccount");
            return new ApiResponse<>(true, "Account found successfully", accountDTO);
        } catch (Exception e) {
            logService.logError("Error finding account: " + accountNumber, this.getClass().getSimpleName(), "findAccount", e);
            return new ApiResponse<>(false, e.getMessage(), null);
        }
    }

    /**
     * Hesaba para yatırma işlemini gerçekleştirir
     * @param accountNumber Hesap numarası
     * @param amount Yatırılacak miktar
     * @return ApiResponse<String> İşlem durumu ve onay kodu
     */
    @Transactional
    public ApiResponse<String> credit(String accountNumber, double amount) {
        try {
            Account account = accountRepository.findByAccountNumber(accountNumber);
            if (account == null) {
                logService.logError("Account not found for credit: " + accountNumber, this.getClass().getSimpleName(),
                        "credit", new RuntimeException("Account not found"));
                return new ApiResponse<>(false, "Account not found", null);
            }

            double roundedAmount = Math.round(amount * 10000.0) / 10000.0;
            if (roundedAmount <= 0) {
                logService.logError("Invalid amount for credit: " + amount, this.getClass().getSimpleName(),
                        "credit", new RuntimeException("Invalid amount"));
                return new ApiResponse<>(false, "Amount must be greater than zero", null);
            }

            DepositTransaction transaction = new DepositTransaction(roundedAmount);
            transaction.setApprovalCode(UUID.randomUUID().toString());
            account.post(transaction);
            accountRepository.save(account);
            transactionRepository.save(transaction);

            logService.logInfo(
                String.format("Credit successful: account=%s, amount=%.4f", accountNumber, roundedAmount),
                this.getClass().getSimpleName(),
                "credit"
            );
            return new ApiResponse<>(true, "Credit successful", transaction.getApprovalCode());
        } catch (Exception e) {
            logService.logError("Error processing credit: " + accountNumber, this.getClass().getSimpleName(), "credit", e);
            return new ApiResponse<>(false, e.getMessage(), null);
        }
    }

    /**
     * Hesaptan para çekme işlemini gerçekleştirir
     * @param accountNumber Hesap numarası
     * @param amount Çekilecek miktar
     * @return ApiResponse<String> İşlem durumu ve onay kodu
     * @throws InsufficientBalanceException Yetersiz bakiye durumunda
     */
    @Transactional
    public ApiResponse<String> debit(String accountNumber, double amount) throws InsufficientBalanceException {
        try {
            Account account = accountRepository.findByAccountNumber(accountNumber);
            if (account == null) {
                logService.logError("Account not found for debit: " + accountNumber,
                    this.getClass().getSimpleName(), "debit", new RuntimeException("Account not found"));
                return new ApiResponse<>(false, "Account not found", null);
            }

            double roundedAmount = Math.round(amount * 10000.0) / 10000.0;
            if (roundedAmount <= 0) {
                logService.logError("Invalid amount for debit: " + amount,
                    this.getClass().getSimpleName(), "debit", new RuntimeException("Invalid amount"));
                return new ApiResponse<>(false, "Amount must be greater than zero", null);
            }

            WithdrawalTransaction transaction = new WithdrawalTransaction(roundedAmount);
            transaction.setApprovalCode(UUID.randomUUID().toString());
            account.post(transaction);
            accountRepository.save(account);
            transactionRepository.save(transaction);

            logService.logInfo(
                String.format("Debit successful: account=%s, amount=%.4f", accountNumber, roundedAmount),
                this.getClass().getSimpleName(),
                "debit"
            );
            return new ApiResponse<>(true, "Debit successful", transaction.getApprovalCode());
        } catch (InsufficientBalanceException e) {
            logService.logError(
                "Insufficient balance for debit: " + accountNumber,
                this.getClass().getSimpleName(),
                "debit",
                e
            );
            throw e;
        } catch (Exception e) {
            logService.logError(
                "Error processing debit: " + accountNumber,
                this.getClass().getSimpleName(),
                "debit",
                e
            );
            return new ApiResponse<>(false, e.getMessage(), null);
        }
    }

    /**
     * Telefon faturası ödeme işlemini gerçekleştirir
     * @param accountNumber Hesap numarası
     * @param payee Fatura sahibi
     * @param phoneNumber Telefon numarası
     * @param amount Ödenecek miktar
     * @return ApiResponse<String> İşlem durumu ve onay kodu
     * @throws InsufficientBalanceException Yetersiz bakiye durumunda
     */
    @Transactional
    public ApiResponse<String> payPhoneBill(String accountNumber, String payee, String phoneNumber, double amount) throws InsufficientBalanceException {
        try {
            Account account = accountRepository.findByAccountNumber(accountNumber);
            if (account == null) {
                logService.logError(
                    "Account not found for bill payment: " + accountNumber,
                    this.getClass().getSimpleName(),
                    "payPhoneBill",
                    new RuntimeException("Account not found")
                );
                return new ApiResponse<>(false, "Account not found", null);
            }

            double roundedAmount = Math.round(amount * 10000.0) / 10000.0;
            if (roundedAmount <= 0) {
                logService.logError("Invalid amount for bill payment: " + amount, this.getClass().getSimpleName(),
                    "payPhoneBill", new RuntimeException("Invalid amount"));
                return new ApiResponse<>(false, "Amount must be greater than zero", null);
            }

            PhoneBillPaymentTransaction transaction = new PhoneBillPaymentTransaction(payee, phoneNumber, roundedAmount);
            transaction.setApprovalCode(UUID.randomUUID().toString());
            account.post(transaction);
            accountRepository.save(account);
            transactionRepository.save(transaction);

            logService.logInfo(
                String.format("Bill payment successful: account=%s, payee=%s, amount=%.4f", 
                    accountNumber, payee, roundedAmount),
                this.getClass().getSimpleName(),
                "payPhoneBill"
            );
            return new ApiResponse<>(true, "Bill payment successful", transaction.getApprovalCode());
        } catch (InsufficientBalanceException e) {
            logService.logError("Insufficient balance for bill payment: " + accountNumber, this.getClass().getSimpleName(),
                "payPhoneBill", e);
            throw e;
        } catch (Exception e) {
            logService.logError("Error processing bill payment: " + accountNumber, this.getClass().getSimpleName(),
                    "payPhoneBill", e);
            return new ApiResponse<>(false, e.getMessage(), null);
        }
    }
}
