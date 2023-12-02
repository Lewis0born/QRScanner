# StockScan Android App

## Overview

StockScan is an Android app that allows users to scan barcodes and send the scanned data to a Google Sheets document.

## Dependencies

- ZXing Android Embedded Library
- Google Apps Script for handling data on Google Sheets

## Configuration

1. Clone the repository.
2. Open the project in Android Studio.
3. Update the `googleScriptUrl` variable in `MainActivity.java` with your Google Apps Script URL, which should contain URL to your Google Sheets.

## Usage

1. Open the app.
2. Click the "Scan" button.
3. Scan a barcode.
4. The scanned data will be sent to the specified Google Sheets document.

## Google Apps Script

```javascript
function doGet(e) {
  Logger.log("Received GET request: " + JSON.stringify(e));
  var ss = SpreadsheetApp.openByUrl("https://docs.google.com/spreadsheets/d/1NRGIz85f7rwK7Y4O9KX_ko6EOEn6Ngdf3W7j4Ib0W5c/edit");
  var sheet = ss.getSheetByName("Sheet1");
  return insert(e, sheet);
}

function doPost(e) {
  Logger.log("Received POST request: " + JSON.stringify(e));
  var ss = SpreadsheetApp.openByUrl("https://docs.google.com/spreadsheets/d/1NRGIz85f7rwK7Y4O9KX_ko6EOEn6Ngdf3W7j4Ib0W5c/edit");
  var sheet = ss.getSheetByName("Sheet1");
  return insert(e, sheet);
}

function insert(e, sheet) {
  var scannedData = e.parameter.sdata;
  var d = new Date();
  var ctime = d.toLocaleString();
  
  sheet.appendRow([scannedData, ctime]);
  
  return ContentService
    .createTextOutput("Success")
    .setMimeType(ContentService.MimeType.TEXT);  
}


