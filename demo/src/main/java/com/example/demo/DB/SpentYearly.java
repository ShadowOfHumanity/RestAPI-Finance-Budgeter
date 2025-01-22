package com.example.demo.DB;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "SPENT_YEARLY")
public class SpentYearly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long yearID;

    private BigDecimal total;
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    // Default constructor
    public SpentYearly() {
        this.total = BigDecimal.ZERO;
    }

    // Constructor with all fields
    public SpentYearly(Long yearID, BigDecimal total, Integer year, User user) {
        this.yearID = yearID;
        this.total = total;
        this.year = year;
        this.user = user;
    }

    // Getters and Setters
    public Long getYearID() {
        return yearID;
    }

    public void setYearID(Long yearID) {
        this.yearID = yearID;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}