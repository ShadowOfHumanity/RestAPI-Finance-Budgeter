package com.example.demo;



import com.example.demo.DB.*;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class RController {
    private final UserService userService;
    @Autowired
    public RController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello, World!";
    }

    @PostMapping("/ScanReceipt")
    public String scanReceipt(@RequestParam("file") MultipartFile file) {
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

    @PostMapping("/CreateUser")
    public User createUser(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("email") String email) {
        return userService.createUser(username, password, email);
    }


}
