package com.example.demo;



import org.apache.http.entity.ByteArrayEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class RController {

    @GetMapping("/hello")
    public String helloWorld() {
        return "Hello, World!";
    }

    @PostMapping("/ScanReceipt")
    public String scanReciept(@RequestParam("file") MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            ByteArrayEntity requestEntity = new ByteArrayEntity(bytes); //without contrast
            return RecieptPhotoScanner.scanReceipt(requestEntity); //without contrast

//            byte[] contrastAdjustedBytes = RecieptPhotoScanner.adjustContrast(bytes, 1.5f);
//            ByteArrayEntity requestEntity = new ByteArrayEntity(contrastAdjustedBytes);
//            return RecieptPhotoScanner.scanReceipt(requestEntity);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing file";
        }
    }


}
