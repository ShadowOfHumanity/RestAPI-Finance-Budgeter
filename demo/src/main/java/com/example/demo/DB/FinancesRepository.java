package com.example.demo.DB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FinancesRepository extends JpaRepository<Finances, Long> {
    List<Finances> findByUser(User user);
    Finances findByUserAndFinanceID(User user, Long financeID);

}