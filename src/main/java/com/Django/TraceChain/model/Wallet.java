package com.Django.TraceChain.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @Column(nullable = false)
    private String address;      // 지갑 주소 (기본키, NOT NULL)

    @Column(nullable = false)
    private int type;            // 지갑 유형 (1 = 비트코인 or 2 = 이더리움, NOT NULL)

    @Column(nullable = false)
    private long balance;        // 보유 금액 (satoshi 등, NOT NULL)

    @Column(columnDefinition = "TINYINT(1)", nullable = true)
    private Boolean fixedAmountPattern;

    @Column(columnDefinition = "TINYINT(1)", nullable = true)
    private Boolean multiIOPattern;

    @Column(columnDefinition = "TINYINT(1)", nullable = true)
    private Boolean loopingPattern;

    @Column(columnDefinition = "TINYINT(1)", nullable = true)
    private Boolean relayerPattern;

    @Column(columnDefinition = "TINYINT(1)", nullable = true)
    private Boolean peelChainPattern;


    @Column(nullable = true)
    private int patternCnt;

    // 다대다 관계: Wallet ↔ Transaction
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "wallet_transaction",
        joinColumns = @JoinColumn(name = "wallet_address"),
        inverseJoinColumns = @JoinColumn(name = "transaction_id")
    )
    private List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(Transaction tx) {
        if (!transactions.contains(tx)) {
            transactions.add(tx);
            tx.getWallets().add(this);
        }
    }

    public void removeTransaction(Transaction tx) {
        if (transactions.remove(tx)) {
            tx.getWallets().remove(this);
        }
    }

    public Wallet() {}

    public Wallet(String address, int type, long balance) {
        this.address = address;
        this.type = type;
        this.balance = balance;
    }

    public Wallet(String address,
                  int type,
                  long balance,
                  Boolean fixedAmountPattern,
                  Boolean multiIOPattern,
                  Boolean loopingPattern,
                  Boolean relayerPattern,
                  Boolean peelChainPattern,
                  int patternCnt) {
        this.address = address;
        this.type = type;
        this.balance = balance;
        this.fixedAmountPattern = fixedAmountPattern;
        this.multiIOPattern = multiIOPattern;
        this.loopingPattern = loopingPattern;
        this.relayerPattern = relayerPattern;
        this.peelChainPattern = peelChainPattern;
        this.patternCnt = patternCnt;
    }

    // Getter / Setter
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }

    public Boolean getFixedAmountPattern() { return fixedAmountPattern; }
    public void setFixedAmountPattern(Boolean fixedAmountPattern) {
        this.fixedAmountPattern = fixedAmountPattern;
    }

    public Boolean getMultiIOPattern() { return multiIOPattern; }
    public void setMultiIOPattern(Boolean multiIOPattern) {
        this.multiIOPattern = multiIOPattern;
    }

    public Boolean getLoopingPattern() { return loopingPattern; }
    public void setLoopingPattern(Boolean loopingPattern) {
        this.loopingPattern = loopingPattern;
    }

    public Boolean getRelayerPattern() { return relayerPattern; }
    public void setRelayerPattern(Boolean relayerPattern) {
        this.relayerPattern = relayerPattern;
    }

    public Boolean getPeelChainPattern() { return peelChainPattern; }
    public void setPeelChainPattern(Boolean peelChainPattern) {
        this.peelChainPattern = peelChainPattern;
    }

    public int getPatternCnt() {
        return patternCnt;
    }

    public void setPatternCnt(int patternCnt) {
        this.patternCnt = patternCnt;
    }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        for (Transaction tx : transactions) {
            if (!tx.getWallets().contains(this)) {
                tx.getWallets().add(this);
            }
        }
    }
}
