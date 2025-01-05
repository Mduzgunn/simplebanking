package com.eteration.simplebanking.model;

import javax.persistence.Entity;

@Entity
public class PhoneBillPaymentTransaction extends Transaction {
    
    private String payee;
    private String phoneNumber;

    protected PhoneBillPaymentTransaction() {
        super();
    }

    public PhoneBillPaymentTransaction(String payee, String phoneNumber, double amount) {
        super(amount);
        this.payee = payee;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void execute(Account account) throws InsufficientBalanceException {
        account.debit(getAmount()); // Telefon faturası tutarı hesaptan çekilir
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
} 
