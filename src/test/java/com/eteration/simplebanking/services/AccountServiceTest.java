package com.eteration.simplebanking.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.UUID;

import com.eteration.simplebanking.dto.AccountDTO;
import com.eteration.simplebanking.dto.ApiResponse;
import com.eteration.simplebanking.model.*;
import com.eteration.simplebanking.repository.AccountRepository;
import com.eteration.simplebanking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(logService).logInfo(anyString(), anyString(), anyString());
        doNothing().when(logService).logError(anyString(), anyString(), anyString(), any(Exception.class));
    }

    @Test
    public void testFindAccount_Success() throws InsufficientBalanceException {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        account.post(new DepositTransaction(1000.0));
        
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);

        // Act
        ApiResponse<AccountDTO> response = accountService.findAccount("669-7788");

        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Account found successfully", response.getMessage());
        assertNotNull(response.getObject());
        assertEquals("669-7788", response.getObject().getAccountNumber());
        assertEquals("Kerem Karaca", response.getObject().getOwner());
        assertEquals(1000.0, response.getObject().getBalance());
        
        // Verify logging
        verify(logService).logInfo(contains("Account found: 669-7788"), 
            eq(accountService.getClass().getSimpleName()), eq("findAccount"));
    }

    @Test
    public void testFindAccount_NotFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("non-existent")).thenReturn(null);

        // Act
        ApiResponse<AccountDTO> response = accountService.findAccount("non-existent");

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Account not found", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Account not found: non-existent"), 
            eq(accountService.getClass().getSimpleName()), eq("findAccount"), any(RuntimeException.class));
    }

    @Test
    public void testFindAccount_UnexpectedException() {
        // Arrange
        when(accountRepository.findByAccountNumber("669-7788"))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        ApiResponse<AccountDTO> response = accountService.findAccount("669-7788");

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Database connection failed", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Error finding account: 669-7788"), 
            eq(accountService.getClass().getSimpleName()), eq("findAccount"), any(RuntimeException.class));
    }

    @Test
    public void testCredit_Success() {
        // Arrange
        String accountNumber = "17892";
        Account account = new Account("Test Owner", accountNumber);
        double amount = 500.0;
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setApprovalCode(UUID.randomUUID().toString());
            return t;
        });
        
        // Act
        ApiResponse<String> response = accountService.credit(accountNumber, amount);
        
        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Credit successful", response.getMessage());
        assertNotNull(response.getObject()); // approvalCode
        assertEquals(500.0, account.getBalance(), 0.001);
        
        // Verify
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(DepositTransaction.class));
        verify(logService).logInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void testCredit_AccountNotFound() {
        // Arrange
        when(accountRepository.findByAccountNumber("non-existent")).thenReturn(null);

        // Act
        ApiResponse<String> response = accountService.credit("non-existent", 1000.0);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Account not found", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Account not found for credit: non-existent"), 
            eq(accountService.getClass().getSimpleName()), eq("credit"), any(RuntimeException.class));
    }

    @Test
    public void testCredit_InvalidAmount() {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);

        // Act
        ApiResponse<String> response = accountService.credit("669-7788", -100.0);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Amount must be greater than zero", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Invalid amount for credit: -100.0"), 
            eq(accountService.getClass().getSimpleName()), eq("credit"), any(RuntimeException.class));
    }

    @Test
    public void testCredit_UnexpectedException() {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);
        when(accountRepository.save(any(Account.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ApiResponse<String> response = accountService.credit("669-7788", 1000.0);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Database error", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Error processing credit: 669-7788"), 
            eq(accountService.getClass().getSimpleName()), eq("credit"), any(RuntimeException.class));
    }

    @Test
    public void testDebit_Success() throws InsufficientBalanceException {
        // Arrange
        String accountNumber = "17892";
        Account account = new Account("Test Owner", accountNumber);
        account.credit(1000.0); // Initial balance
        double amount = 500.0;
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setApprovalCode(UUID.randomUUID().toString());
            return t;
        });
        
        // Act
        ApiResponse<String> response = accountService.debit(accountNumber, amount);
        
        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Debit successful", response.getMessage());
        assertNotNull(response.getObject()); // approvalCode
        assertEquals(500.0, account.getBalance(), 0.001);
        
        // Verify
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(WithdrawalTransaction.class));
        verify(logService).logInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void testDebit_InsufficientBalance() {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            accountService.debit("669-7788", 500.0);
        });
        
        // Verify error logging
        verify(logService).logError(contains("Insufficient balance for debit: 669-7788"), 
            eq(accountService.getClass().getSimpleName()), eq("debit"), eq(exception));
    }

    @Test
    public void testDebit_AccountNotFound() throws InsufficientBalanceException {
        // Arrange
        when(accountRepository.findByAccountNumber("non-existent")).thenReturn(null);

        // Act
        ApiResponse<String> response = accountService.debit("non-existent", 500.0);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Account not found", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Account not found for debit: non-existent"), 
            eq(accountService.getClass().getSimpleName()), eq("debit"), any(RuntimeException.class));
    }

    @Test
    public void testDebit_InvalidAmount() throws InsufficientBalanceException {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);

        // Act
        ApiResponse<String> response = accountService.debit("669-7788", -100.0);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Amount must be greater than zero", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Invalid amount for debit: -100.0"), 
            eq(accountService.getClass().getSimpleName()), eq("debit"), any(RuntimeException.class));
    }

    @Test
    public void testDebit_UnexpectedException() throws InsufficientBalanceException {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        account.post(new DepositTransaction(1000.0));
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);
        when(accountRepository.save(any(Account.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ApiResponse<String> response = accountService.debit("669-7788", 500.0);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Database error", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Error processing debit: 669-7788"), 
            eq(accountService.getClass().getSimpleName()), eq("debit"), any(RuntimeException.class));
    }

    @Test
    public void testPayPhoneBill_Success() throws InsufficientBalanceException {
        // Arrange
        String accountNumber = "17892";
        Account account = new Account("Test Owner", accountNumber);
        account.credit(1000.0); // Initial balance
        String payee = "Vodafone";
        String phoneNumber = "5423345566";
        double amount = 96.50;
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setApprovalCode(UUID.randomUUID().toString());
            return t;
        });
        
        // Act
        ApiResponse<String> response = accountService.payPhoneBill(accountNumber, payee, phoneNumber, amount);
        
        // Assert
        assertTrue(response.getSuccess());
        assertEquals("Bill payment successful", response.getMessage());
        assertNotNull(response.getObject()); // approvalCode
        assertEquals(903.50, account.getBalance(), 0.001);
        
        // Verify
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(PhoneBillPaymentTransaction.class));
        verify(logService).logInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void testPayPhoneBill_InsufficientBalance() {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            accountService.payPhoneBill("669-7788", "Vodafone", "5423345566", 96.50);
        });
        
        // Verify error logging
        verify(logService).logError(contains("Insufficient balance for bill payment: 669-7788"), 
            eq(accountService.getClass().getSimpleName()), eq("payPhoneBill"), eq(exception));
    }

    @Test
    public void testPayPhoneBill_AccountNotFound() throws InsufficientBalanceException {
        // Arrange
        when(accountRepository.findByAccountNumber("non-existent")).thenReturn(null);

        // Act
        ApiResponse<String> response = accountService.payPhoneBill("non-existent", "Vodafone", "5423345566", 96.50);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Account not found", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Account not found for bill payment: non-existent"), 
            eq(accountService.getClass().getSimpleName()), eq("payPhoneBill"), any(RuntimeException.class));
    }

    @Test
    public void testPayPhoneBill_InvalidAmount() throws InsufficientBalanceException {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);

        // Act
        ApiResponse<String> response = accountService.payPhoneBill("669-7788", "Vodafone", "5423345566", -100.0);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Amount must be greater than zero", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Invalid amount for bill payment: -100.0"), 
            eq(accountService.getClass().getSimpleName()), eq("payPhoneBill"), any(RuntimeException.class));
    }

    @Test
    public void testPayPhoneBill_UnexpectedException() throws InsufficientBalanceException {
        // Arrange
        Account account = new Account("Kerem Karaca", "669-7788");
        account.post(new DepositTransaction(1000.0));
        when(accountRepository.findByAccountNumber("669-7788")).thenReturn(account);
        when(accountRepository.save(any(Account.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ApiResponse<String> response = accountService.payPhoneBill("669-7788", "Vodafone", "5423345566", 96.50);

        // Assert
        assertFalse(response.getSuccess());
        assertEquals("Database error", response.getMessage());
        assertNull(response.getObject());
        
        // Verify error logging
        verify(logService).logError(contains("Error processing bill payment: 669-7788"), 
            eq(accountService.getClass().getSimpleName()), eq("payPhoneBill"), any(RuntimeException.class));
    }
} 
