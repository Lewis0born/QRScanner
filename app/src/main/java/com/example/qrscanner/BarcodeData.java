package com.example.qrscanner;

public class BarcodeData {
    private String barcode;
    private String status;

    public BarcodeData(String barcode, String status) {
        this.barcode = barcode;
        this.status = status;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getStatus() {
        return status;
    }
}

