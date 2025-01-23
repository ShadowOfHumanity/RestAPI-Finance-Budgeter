package com.example.demo.DB;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "GLOBAL_SPENT_YEARLY")
public class GlobalSpentYearly {
    @Id
    private Integer year;

    @Column(name = "Total")
    private BigDecimal total;

    @Column(name = "Convenience")
    private BigDecimal convenience;

    @Column(name = "SuperMarket")
    private BigDecimal superMarket;


    public GlobalSpentYearly() {
        this.total = BigDecimal.ZERO;
        this.convenience = BigDecimal.ZERO;
        this.superMarket = BigDecimal.ZERO;
    }


    public GlobalSpentYearly(Integer year, BigDecimal total, BigDecimal convenience, BigDecimal superMarket) {
        this.year = year;
        this.total = total;
        this.convenience = convenience;
        this.superMarket = superMarket;
    }

    // Getters and Setters
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getConvenience() {
        return convenience;
    }

    public void setConvenience(BigDecimal convenience) {
        this.convenience = convenience;
    }

    public BigDecimal getSuperMarket() {
        return superMarket;
    }

    public void setSuperMarket(BigDecimal superMarket) {
        this.superMarket = superMarket;
    }

    // Helper method to update total
    public void updateTotal() {
        this.total = (this.convenience != null ? this.convenience : BigDecimal.ZERO)
                .add(this.superMarket != null ? this.superMarket : BigDecimal.ZERO);
    }
}