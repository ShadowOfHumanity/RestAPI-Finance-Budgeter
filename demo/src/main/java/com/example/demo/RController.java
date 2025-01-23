package com.example.demo;



import com.example.demo.DB.*;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class RController {
    private final UserService userService;
    private final FinancesService financesService;
    private final SpentYearlyService spentYearlyService;
    private final GlobalSpentYearlyService globalSpentYearlyService;

    @Autowired
    public RController(UserService userService,
                       FinancesService financesService,
                       SpentYearlyService spentYearlyService,
                       GlobalSpentYearlyService globalSpentYearlyService) {
        this.userService = userService;
        this.financesService = financesService;
        this.spentYearlyService = spentYearlyService;
        this.globalSpentYearlyService = globalSpentYearlyService;
    }
    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello, World!";
    }

    @PostMapping("/ScanReceipt")
    public String scanReceipt(@RequestParam("file") MultipartFile file, @RequestParam("password") String password, @RequestParam("username") String username) {
        boolean isInDb = userService.checkUser(username, password);
        if (!isInDb) {
            return "Invalid username or password";
        } else {
            try {
                byte[] bytes = file.getBytes();
                ByteArrayEntity requestEntity = new ByteArrayEntity(bytes); //without contrast
                return ReceiptPhotoScanner.scanReceipt(requestEntity); //without contrast

                //            byte[] contrastAdjustedBytes = ReceiptPhotoScanner.adjustContrast(bytes, 1.5f);
                //            ByteArrayEntity requestEntity = new ByteArrayEntity(contrastAdjustedBytes);
                //            return ReceiptPhotoScanner.scanReceipt(requestEntity);
            } catch (Exception e) {
                e.printStackTrace();
                return "Error processing file";
            }
        }
    }

    @PostMapping("/ConfirmReceipt")
    public ResponseEntity<?> confirmReceipt(@RequestParam("username") String username,
                                            @RequestParam("password") String password,
                                            @RequestParam("amount") BigDecimal amount,
                                            @RequestParam(value = "companyName", required = false) String companyName) {
        // Existing validation logic remains the same
        if (!userService.checkUser(username, password)) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        try {
            User user = userService.getUserByUsername(username);
            Finances updated = financesService.updateReceiptSpending(user.getUserID(), amount);
            spentYearlyService.updateYearlyTotal(user.getUserID(), amount);

            // JSON response with optional company name
            if (companyName != null) {
                return ResponseEntity.ok("{Company_Name: " + companyName + ", Total_Price: €" + amount + "}");
            } else {
                return ResponseEntity.ok("{Total_Price: €" + amount + "}");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

//    @PostMapping("/CreateUser")
//    public User createUser(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("email") String email) {
//        return userService.createUser(username, password, email);
//    }
    @PostMapping("/CreateUser")
    public ResponseEntity<?> createUser(@RequestParam("username") String username,
                                    @RequestParam("password") String password,
                                        @RequestParam("email") String email) {
        try {
            User newUser = userService.createUser(username, password, email);
            // Initialize financial records for new user
            financesService.createFinancesForUser(newUser);
            spentYearlyService.createInitialYearlyRecord(newUser);
            return ResponseEntity.ok(newUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/Login")
    public ResponseEntity<?> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        if (userService.checkUser(username, password)) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    // Financial endpoints
    @PostMapping("/spending/card")
    public ResponseEntity<?> updateCardSpending(@RequestParam("username") String username,
                                                @RequestParam("password") String password,
                                                @RequestParam("amount") BigDecimal amount) {
        if (!userService.checkUser(username, password)) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        try {
            User user = userService.getUserByUsername(username);
            Finances updated = financesService.updateCardSpending(user.getUserID(), amount);
            spentYearlyService.updateYearlyTotal(user.getUserID(), amount);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/spending/receipt")
    public ResponseEntity<?> updateReceiptSpending(@RequestParam("username") String username,
                                                   @RequestParam("password") String password,
                                                   @RequestParam("amount") BigDecimal amount) {
        if (!userService.checkUser(username, password)) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        try {
            User user = userService.getUserByUsername(username);
            Finances updated = financesService.updateReceiptSpending(user.getUserID(), amount);
            spentYearlyService.updateYearlyTotal(user.getUserID(), amount);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/spending/current")
    public ResponseEntity<?> getCurrentSpending(@RequestParam("username") String username,
                                                @RequestParam("password") String password) {
        if (!userService.checkUser(username, password)) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        try {
            User user = userService.getUserByUsername(username);
            return ResponseEntity.ok(financesService.getFinancesForUser(user.getUserID()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/spending/yearly")
    public ResponseEntity<?> getYearlySpending(@RequestParam("username") String username,
                                               @RequestParam("password") String password,
                                               @RequestParam(required = false) Integer year) {
        if (!userService.checkUser(username, password)) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        try {
            User user = userService.getUserByUsername(username);
            if (year != null) {
                return ResponseEntity.ok(spentYearlyService.getYearlyRecord(user.getUserID(), year));
            } else {
                return ResponseEntity.ok(spentYearlyService.getAllYearlyRecords(user.getUserID()));
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @GetMapping("/global/yearly")
    public ResponseEntity<?> getGlobalYearlySpending(@RequestParam Integer year) {
        try {
            return ResponseEntity.ok(globalSpentYearlyService.getYearlyRecord(year));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}



