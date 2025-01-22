package com.example.demo.DB;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Year;
import java.util.List;

@Service
@Transactional
public class SpentYearlyService {
    private final SpentYearlyRepository spentYearlyRepository;
    private final UserRepository userRepository;

    @Autowired
    public SpentYearlyService(SpentYearlyRepository spentYearlyRepository, UserRepository userRepository) {
        this.spentYearlyRepository = spentYearlyRepository;
        this.userRepository = userRepository;
    }

    public SpentYearly createYearlyRecord(User user, Integer year) {
        if (spentYearlyRepository.existsByUserAndYear(user, year)) {
            throw new IllegalStateException("Yearly record already exists for user in year " + year);
        }

        SpentYearly spentYearly = new SpentYearly();
        spentYearly.setUser(user);
        spentYearly.setYear(year);
        spentYearly.setTotal(BigDecimal.ZERO);
        return spentYearlyRepository.save(spentYearly);
    }

    public SpentYearly getOrCreateCurrentYearRecord(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        int currentYear = Year.now().getValue();
        return spentYearlyRepository.findByUserAndYear(user, currentYear)
                .orElseGet(() -> createYearlyRecord(user, currentYear));
    }

    public SpentYearly updateYearlyTotal(Long userId, BigDecimal amount) {
        SpentYearly spentYearly = getOrCreateCurrentYearRecord(userId);
        BigDecimal currentTotal = spentYearly.getTotal();
        spentYearly.setTotal(currentTotal.add(amount));
        return spentYearlyRepository.save(spentYearly);
    }

    public List<SpentYearly> getAllYearlyRecords(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return spentYearlyRepository.findByUser(user);
    }

    public SpentYearly getYearlyRecord(Long userId, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return spentYearlyRepository.findByUserAndYear(user, year)
                .orElseThrow(() -> new IllegalArgumentException("No record found for year " + year));
    }

    public void createInitialYearlyRecord(User user) {
        int currentYear = Year.now().getValue();
        if (!spentYearlyRepository.existsByUserAndYear(user, currentYear)) {
            createYearlyRecord(user, currentYear);
        }
    }
}