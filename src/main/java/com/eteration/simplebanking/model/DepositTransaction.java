package com.eteration.simplebanking.model;

import javax.persistence.Entity;

@Entity
public class DepositTransaction extends Transaction {

    public DepositTransaction(double amount) {
        super(amount);
    }

    public DepositTransaction() {
        super();
    }

    @Override
    public void execute(Account account) {
        account.credit(getAmount());
    }
}
