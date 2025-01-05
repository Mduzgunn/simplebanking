package com.eteration.simplebanking.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.eteration.simplebanking.dto.AccountDTO;
import com.eteration.simplebanking.dto.ApiResponse;
import com.eteration.simplebanking.dto.TransactionDTO;
import com.eteration.simplebanking.model.InsufficientBalanceException;
import com.eteration.simplebanking.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAccount_WhenAccountExists_ShouldReturnAccount() {
        // Arrange
        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setAccountNumber("669-7788");
        accountDTO.setOwner("Kerem Karaca");
        accountDTO.setBalance(1000.0);
        accountDTO.setCreateDate(new Date());

        ApiResponse<AccountDTO> apiResponse = new ApiResponse<>(true, "Account found successfully", accountDTO);
        when(accountService.findAccount("669-7788")).thenReturn(apiResponse);

        // Act
        ResponseEntity<AccountDTO> response = accountController.getAccount("669-7788");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("669-7788", response.getBody().getAccountNumber());
        assertEquals("Kerem Karaca", response.getBody().getOwner());
        assertEquals(1000.0, response.getBody().getBalance());
        assertNotNull(response.getBody().getCreateDate());
        verify(accountService).findAccount("669-7788");
    }

    @Test
    void getAccount_WhenAccountNotFound_ShouldReturnNotFound() {
        // Arrange
        ApiResponse<AccountDTO> apiResponse = new ApiResponse<>(false, "Account not found", null);
        when(accountService.findAccount("non-existent")).thenReturn(apiResponse);

        // Act
        ResponseEntity<AccountDTO> response = accountController.getAccount("non-existent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(accountService).findAccount("non-existent");
    }

    @Test
    void credit_WhenValidAmount_ShouldReturnSuccess() {
        // Arrange
        TransactionDTO request = new TransactionDTO();
        request.setAmount(1000.0);

        ApiResponse<String> apiResponse = new ApiResponse<>(true, "Credit successful", "approval-code");
        when(accountService.credit("669-7788", 1000.0)).thenReturn(apiResponse);

        // Act
        ResponseEntity<TransactionStatus> response = accountController.credit("669-7788", request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OK", response.getBody().getStatus());
        assertEquals("approval-code", response.getBody().getApprovalCode());
        verify(accountService).credit("669-7788", 1000.0);
    }

//    @Test
//    void credit_WhenNullAmount_ShouldReturnBadRequest() {
//        // Arrange
//        TransactionDTO request = new TransactionDTO();
//
//        // Act
//        ResponseEntity<TransactionStatus> response = accountController.credit("669-7788", request);
//
//        // Assert
//        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertEquals("ERROR", response.getBody().getStatus());
//        assertNull(response.getBody().getApprovalCode());
//        verify(accountService, never()).credit(anyString(), anyDouble());
//    }

    @Test
    void credit_WhenNegativeAmount_ShouldReturnBadRequest() {
        // Arrange
        TransactionDTO request = new TransactionDTO();
        request.setAmount(-100.0);

        ApiResponse<String> apiResponse = new ApiResponse<>(false, "Amount must be greater than zero", null);
        when(accountService.credit("669-7788", -100.0)).thenReturn(apiResponse);

        // Act
        ResponseEntity<TransactionStatus> response = accountController.credit("669-7788", request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ERROR", response.getBody().getStatus());
        assertNull(response.getBody().getApprovalCode());
        verify(accountService).credit("669-7788", -100.0);
    }

    @Test
    void debit_WhenValidAmount_ShouldReturnSuccess() throws InsufficientBalanceException {
        // Arrange
        TransactionDTO request = new TransactionDTO();
        request.setAmount(500.0);

        ApiResponse<String> apiResponse = new ApiResponse<>(true, "Debit successful", "approval-code");
        when(accountService.debit("669-7788", 500.0)).thenReturn(apiResponse);

        // Act
        ResponseEntity<TransactionStatus> response = accountController.debit("669-7788", request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OK", response.getBody().getStatus());
        assertEquals("approval-code", response.getBody().getApprovalCode());
        verify(accountService).debit("669-7788", 500.0);
    }

    @Test
    void payPhoneBill_WhenValidRequest_ShouldReturnSuccess() throws InsufficientBalanceException {
        // Arrange
        TransactionDTO request = new TransactionDTO();
        request.setAmount(96.50);
        request.setType("Vodafone");
        request.setApprovalCode("5423345566");

        ApiResponse<String> apiResponse = new ApiResponse<>(true, "Bill payment successful", "approval-code");
        when(accountService.payPhoneBill("669-7788", "Vodafone", "5423345566", 96.50))
                .thenReturn(apiResponse);

        // Act
        ResponseEntity<TransactionStatus> response = accountController.payPhoneBill("669-7788", request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("OK", response.getBody().getStatus());
        assertEquals("approval-code", response.getBody().getApprovalCode());
        verify(accountService).payPhoneBill("669-7788", "Vodafone", "5423345566", 96.50);
    }
} 
