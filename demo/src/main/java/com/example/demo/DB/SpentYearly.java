package com.example.demo.DB;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SPENT_YEARLY")
public class SpentYearly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long yearID;

    @Column(name = "Total")
    private BigDecimal total;

    @Column(name = "Year")
    private Integer year;

    @ManyToOne
    @JoinColumn(name = "UserID", nullable = false)
    private User user;


    public SpentYearly() {
        this.total = BigDecimal.ZERO;
    }


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