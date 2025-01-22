package com.example.demo.DB;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@Transactional
public class FinancesService {
    private final FinancesRepository financesRepository;
    private final UserRepository userRepository;

    @Autowired
    public FinancesService(FinancesRepository financesRepository, UserRepository userRepository) {
        this.financesRepository = financesRepository;
        this.userRepository = userRepository;
    }

    public Finances createFinancesForUser(User user) {
        Finances finances = new Finances();
        finances.setUser(user);
        finances.setSpentOnCard(BigDecimal.ZERO);
        finances.setSpentWithReceipt(BigDecimal.ZERO);
        finances.setSpentTotal(BigDecimal.ZERO);
        return financesRepository.save(finances);
    }

    public Finances updateCardSpending(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Finances finances = financesRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseGet(() -> createFinancesForUser(user));

        BigDecimal currentAmount = finances.getSpentOnCard();
        finances.setSpentOnCard(currentAmount.add(amount));

        return financesRepository.save(finances);
    }

    public Finances updateReceiptSpending(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Finances finances = financesRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseGet(() -> createFinancesForUser(user));

        BigDecimal currentAmount = finances.getSpentWithReceipt();
        finances.setSpentWithReceipt(currentAmount.add(amount));

        return financesRepository.save(finances);
    }

    public Finances getFinancesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return financesRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Finances not found for user"));
    }

    public void resetMonthlySpending(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Finances finances = financesRepository.findByUser(user)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Finances not found for user"));

        finances.setSpentOnCard(BigDecimal.ZERO);
        finances.setSpentWithReceipt(BigDecimal.ZERO);
        financesRepository.save(finances);
    }
}