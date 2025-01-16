package com.example.demo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class CompanyService {
    private static final HashMap<String, String> companyMap = new HashMap<>();
    private static CompanyService instance;
    private final Resource resource;

    @Autowired
    public CompanyService(@Value("classpath:ListOfCompanies") Resource resource) {
        this.resource = resource;
        instance = this;
        readCompaniesToMap();
    }

    @PostConstruct
    private void init() {
        readCompaniesToMap();
    }

    private void readCompaniesToMap() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String content = reader.lines().collect(Collectors.joining());
            List<String> companies = Arrays.asList(content.split(","));
            for (String company : companies) {
                companyMap.put(company.trim(), company.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static CompanyService getInstance() {
        return instance;
    }


    public static boolean isCompanyPresent(String companyName) {
        companyName = companyName.toLowerCase();
        companyName = companyName.trim().replaceAll("'", "");;
        return companyMap.containsKey(companyName);
    }
}