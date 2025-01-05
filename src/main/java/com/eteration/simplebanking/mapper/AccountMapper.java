package com.eteration.simplebanking.mapper;

import com.eteration.simplebanking.dto.AccountDTO;
import com.eteration.simplebanking.dto.TransactionDTO;
import com.eteration.simplebanking.model.Account;
import com.eteration.simplebanking.model.Transaction;

import java.util.stream.Collectors;

public class AccountMapper {
    /**
     * Account entity'sini AccountDTO'ya dönüştürür
     * @param account Dönüştürülecek Account nesnesi
     * @return AccountDTO Dönüştürülmüş DTO nesnesi
     */
    public static AccountDTO toDTO(Account account) {
        if (account == null) {
            return null;
        }
        
        AccountDTO dto = new AccountDTO();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setOwner(account.getOwner());
        dto.setBalance(account.getBalance());
        dto.setCreateDate(account.getCreateDate());
        
        // Map transactions
        if (account.getTransactions() != null) {
            dto.setTransactions(account.getTransactions().stream()
                .map(AccountMapper::toTransactionDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private static TransactionDTO toTransactionDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setDate(transaction.getDate());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getClass().getSimpleName());
        dto.setApprovalCode(transaction.getApprovalCode());
        return dto;
    }
} 