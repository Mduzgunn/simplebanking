package com.eteration.simplebanking.model;

import javax.persistence.Entity;

@Entity
public class WithdrawalTransaction extends Transaction {

    protected WithdrawalTransaction() {
        super();
    }

    public WithdrawalTransaction(double amount) {
        super(amount);
    }

    @Override
    public void execute(Account account) throws InsufficientBalanceException {
        account.debit(getAmount()); // Para çekme işlemi burada gerçekleştiriliyor
    }
}


