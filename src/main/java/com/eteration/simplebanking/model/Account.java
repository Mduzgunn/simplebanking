package com.eteration.simplebanking.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String owner;
    private String accountNumber;
    
    private double balance;

    @OneToMany(mappedBy = "account")
    private List<Transaction> transactions = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    private String lastTransactionApprovalCode;

    protected Account() {
        this.createDate = new Date();
        this.balance = 0.0;
    }

    public Account(String owner, String accountNumber) {
        this();
        this.owner = owner;
        this.accountNumber = accountNumber;
    }

    /**
     * İşlemi hesaba uygular ve işlem kaydını tutar
     * @param transaction Uygulanacak işlem
     * @throws InsufficientBalanceException Yetersiz bakiye durumunda
     */
    public void post(Transaction transaction) throws InsufficientBalanceException {
        transaction.setAccount(this);
        transaction.execute(this);
        transactions.add(transaction);
        this.lastTransactionApprovalCode = transaction.getApprovalCode();
    }

    /**
     * Hesaba para yatırma işlemini gerçekleştirir
     * @param amount Yatırılacak miktar
     */
    public void credit(double amount) {
        // Round to 4 decimal places
        double roundedAmount = Math.round(amount * 10000.0) / 10000.0;
        this.balance = Math.round((this.balance + roundedAmount) * 10000.0) / 10000.0;
    }

    /**
     * Hesaptan para çekme işlemini gerçekleştirir
     * @param amount Çekilecek miktar
     * @throws InsufficientBalanceException Yetersiz bakiye durumunda
     */
    public void debit(double amount) throws InsufficientBalanceException {
        // Round to 4 decimal places
        double roundedAmount = Math.round(amount * 10000.0) / 10000.0;
        if (this.balance < roundedAmount) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal!");
        }
        this.balance = Math.round((this.balance - roundedAmount) * 10000.0) / 10000.0;
    }

    // Getters and Setters
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = Math.round(balance * 10000.0) / 10000.0;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getLastTransactionApprovalCode() {
        return lastTransactionApprovalCode;
    }

    public void setLastTransactionApprovalCode(String lastTransactionApprovalCode) {
        this.lastTransactionApprovalCode = lastTransactionApprovalCode;
    }
}
