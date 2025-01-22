package com.example.demo.DB;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SpentYearlyRepository extends JpaRepository<SpentYearly, Long> {
    List<SpentYearly> findByUser(User user);
    Optional<SpentYearly> findByUserAndYear(User user, Integer year);
    boolean existsByUserAndYear(User user, Integer year);
}