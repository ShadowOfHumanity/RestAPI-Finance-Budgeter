package com.example.demo.DB;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;

@Service
@Transactional
public class GlobalSpentYearlyService {
    private final GlobalSpentYearlyRepository globalSpentYearlyRepository;

    @Autowired
    public GlobalSpentYearlyService(GlobalSpentYearlyRepository globalSpentYearlyRepository) {
        this.globalSpentYearlyRepository = globalSpentYearlyRepository;
    }

    public GlobalSpentYearly createOrUpdateYearRecord(Integer year) {
        return globalSpentYearlyRepository.findByYear(year)
                .orElseGet(() -> {
                    GlobalSpentYearly newRecord = new GlobalSpentYearly();
                    newRecord.setYear(year);
                    return globalSpentYearlyRepository.save(newRecord);
                });
    }

    public void updateConvenienceSpending(Integer year, BigDecimal amount) {
        GlobalSpentYearly record = getOrCreateRecord(year);
        record.setConvenience(record.getConvenience().add(amount));
        record.updateTotal();
        globalSpentYearlyRepository.save(record);
    }

    public void updateSuperMarketSpending(Integer year, BigDecimal amount) {
        GlobalSpentYearly record = getOrCreateRecord(year);
        record.setSuperMarket(record.getSuperMarket().add(amount));
        record.updateTotal();
        globalSpentYearlyRepository.save(record);
    }

    public void setConvenienceSpending(Integer year, BigDecimal amount) {
        GlobalSpentYearly record = getOrCreateRecord(year);
        record.setConvenience(amount);
        record.updateTotal();
        globalSpentYearlyRepository.save(record);
    }

    public void setSuperMarketSpending(Integer year, BigDecimal amount) {
        GlobalSpentYearly record = getOrCreateRecord(year);
        record.setSuperMarket(amount);
        record.updateTotal();
        globalSpentYearlyRepository.save(record);
    }

    public GlobalSpentYearly getYearlyRecord(Integer year) {
        return globalSpentYearlyRepository.findByYear(year)
                .orElseThrow(() -> new IllegalArgumentException("No record found for year " + year));
    }

    public GlobalSpentYearly getCurrentYearRecord() {
        return getOrCreateRecord(Year.now().getValue());
    }

    public void resetYearlyRecord(Integer year) {
        GlobalSpentYearly record = getOrCreateRecord(year);
        record.setConvenience(BigDecimal.ZERO);
        record.setSuperMarket(BigDecimal.ZERO);
        record.setTotal(BigDecimal.ZERO);
        globalSpentYearlyRepository.save(record);
    }

    private GlobalSpentYearly getOrCreateRecord(Integer year) {
        return globalSpentYearlyRepository.findByYear(year)
                .orElseGet(() -> createOrUpdateYearRecord(year));
    }

    // Additional admin-only operations
    public void deleteYearRecord(Integer year) {
        globalSpentYearlyRepository.deleteById(year);
    }

    public void manuallySetTotal(Integer year, BigDecimal total) {
        GlobalSpentYearly record = getOrCreateRecord(year);
        record.setTotal(total);
        globalSpentYearlyRepository.save(record);
    }

    public BigDecimal calculateTotalAcrossAllYears() {
        return globalSpentYearlyRepository.findAll().stream()
                .map(GlobalSpentYearly::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void recalculateAllTotals() {
        globalSpentYearlyRepository.findAll().forEach(record -> {
            record.updateTotal();
            globalSpentYearlyRepository.save(record);
        });
    }
}