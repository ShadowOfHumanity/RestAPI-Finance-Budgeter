package com.example.demo.DB;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Finances")
public class Finances {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long financeID;

    @Column(name = "SpentOnCard")
    private BigDecimal spentOnCard;

    @Column(name = "SpentWithReceipt")
    private BigDecimal spentWithReceipt;

    @Column(name = "SpentTotal")
    private BigDecimal spentTotal;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    // Default constructor
    public Finances() {
        this.spentOnCard = BigDecimal.ZERO;
        this.spentWithReceipt = BigDecimal.ZERO;
        this.spentTotal = BigDecimal.ZERO;
    }

    // Constructor with all fields
    public Finances(Long financeID, BigDecimal spentOnCard, BigDecimal spentWithReceipt,
                    BigDecimal spentTotal, User user) {
        this.financeID = financeID;
        this.spentOnCard = spentOnCard;
        this.spentWithReceipt = spentWithReceipt;
        this.spentTotal = spentTotal;
        this.user = user;
    }

    // Getters and Setters
    public Long getFinanceID() {
        return financeID;
    }

    public void setFinanceID(Long financeID) {
        this.financeID = financeID;
    }

    public BigDecimal getSpentOnCard() {
        return spentOnCard;
    }

    public void setSpentOnCard(BigDecimal spentOnCard) {
        this.spentOnCard = spentOnCard;
        updateTotal();
    }

    public BigDecimal getSpentWithReceipt() {
        return spentWithReceipt;
    }

    public void setSpentWithReceipt(BigDecimal spentWithReceipt) {
        this.spentWithReceipt = spentWithReceipt;
        updateTotal();
    }

    public BigDecimal getSpentTotal() {
        return spentTotal;
    }

    public void setSpentTotal(BigDecimal spentTotal) {
        this.spentTotal = spentTotal;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Helper method to update total
    private void updateTotal() {
        this.spentTotal = (this.spentOnCard != null ? this.spentOnCard : BigDecimal.ZERO)
                .add(this.spentWithReceipt != null ? this.spentWithReceipt : BigDecimal.ZERO);
    }
}
