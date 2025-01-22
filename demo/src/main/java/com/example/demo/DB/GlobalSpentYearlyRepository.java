package com.example.demo.DB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GlobalSpentYearlyRepository extends JpaRepository<GlobalSpentYearly, Integer> {
    Optional<GlobalSpentYearly> findByYear(Integer year);
}