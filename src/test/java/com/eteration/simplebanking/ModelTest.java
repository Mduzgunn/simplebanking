package com.eteration.simplebanking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.eteration.simplebanking.model.*;
import org.junit.jupiter.api.Test;

public class ModelTest {
	
	@Test
	public void testCreateAccount() {
		Account account = new Account("Kerem Karaca", "669-7788");
		assertNotNull(account.getCreateDate());
		assertEquals("Kerem Karaca", account.getOwner());
		assertEquals("669-7788", account.getAccountNumber());
		assertEquals(0.0, account.getBalance());
	}

	@Test
	public void testDepositTransaction() throws InsufficientBalanceException {
		Account account = new Account("Kerem Karaca", "669-7788");
		DepositTransaction deposit = new DepositTransaction(1000.0);
		account.post(deposit);
		assertEquals(1000.0, account.getBalance());
		assertEquals(deposit.getApprovalCode(), account.getLastTransactionApprovalCode());
	}

	@Test
	public void testWithdrawalTransaction() throws InsufficientBalanceException {
		Account account = new Account("Kerem Karaca", "669-7788");
		account.post(new DepositTransaction(1000.0));
		WithdrawalTransaction withdrawal = new WithdrawalTransaction(500.0);
		account.post(withdrawal);
		assertEquals(500.0, account.getBalance());
		assertEquals(withdrawal.getApprovalCode(), account.getLastTransactionApprovalCode());
	}

	@Test
	public void testWithdrawalTransactionInsufficientBalance() {
		Account account = new Account("Kerem Karaca", "669-7788");
		WithdrawalTransaction withdrawal = new WithdrawalTransaction(500.0);
		assertThrows(InsufficientBalanceException.class, () -> account.post(withdrawal));
	}

	@Test
	public void testBillPaymentTransaction() throws InsufficientBalanceException {
		Account account = new Account("Kerem Karaca", "669-7788");
		account.post(new DepositTransaction(1000.0));
		PhoneBillPaymentTransaction payment = new PhoneBillPaymentTransaction("Vodafone", "5423345566", 96.50);
		account.post(payment);
		assertEquals(903.50, account.getBalance());
		assertEquals("Vodafone", payment.getPayee());
		assertEquals("5423345566", payment.getPhoneNumber());
		assertEquals(payment.getApprovalCode(), account.getLastTransactionApprovalCode());
	}

	@Test
	public void testTransactionDate() {
		DepositTransaction transaction = new DepositTransaction(1000.0);
		assertNotNull(transaction.getDate());
	}

	@Test
	public void testTransactionAmount() {
		DepositTransaction transaction = new DepositTransaction(1000.0);
		assertEquals(1000.0, transaction.getAmount());
	}

	@Test
	public void testApprovalCode() {
		DepositTransaction transaction = new DepositTransaction(1000.0);
		assertNotNull(transaction.getApprovalCode());
	}

	@Test
	public void testAccountBalance() {
		Account account = new Account("Kerem Karaca", "669-7788");
		account.setBalance(1000.0);
		assertEquals(1000.0, account.getBalance());
	}

	@Test
	public void testTransactionList() throws InsufficientBalanceException {
		Account account = new Account("Kerem Karaca", "669-7788");
		account.post(new DepositTransaction(1000.0));
		account.post(new WithdrawalTransaction(500.0));
		assertEquals(2, account.getTransactions().size());
	}

	@Test
	public void testComplexScenario() throws InsufficientBalanceException {
		// Create account and perform multiple transactions
		Account account = new Account("Kerem Karaca", "669-7788");
		
		// Deposit 2000
		account.post(new DepositTransaction(2000.0));
		assertEquals(2000.0, account.getBalance());
		
		// Withdraw 500
		account.post(new WithdrawalTransaction(500.0));
		assertEquals(1500.0, account.getBalance());
		
		// Pay bill 350
		account.post(new PhoneBillPaymentTransaction("Vodafone", "5423345566", 350.0));
		assertEquals(1150.0, account.getBalance());
		
		// Deposit 1000
		account.post(new DepositTransaction(1000.0));
		assertEquals(2150.0, account.getBalance());
		
		// Verify transaction count
		assertEquals(4, account.getTransactions().size());
	}
}
