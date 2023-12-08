# StockScan Android App

## Overview

StockScan is an Android app that allows users to scan barcodes and send the scanned data to a Google Sheets document.

## TODO:
1. Search bar 
2. Remove items from IN when they are scanned into OUT
3. Check item hasn't already been scanned IN to avoid duplicates
4. Show list of scanned data under searchbar

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
4. Select whether stock is being sent IN, OUT, or SOLD.
5. The scanned data will be sent to the specified Google Sheets document.
6. If IN or SOLD, product count will increase or decrease in ProductCounter sheet.

## Google Apps Script

```javascript
function doGet(e) {
  Logger.log("Received GET request: " + JSON.stringify(e));
  let ss = SpreadsheetApp.openByUrl("https://docs.google.com/spreadsheets/d/16LrNS06mjl7x0aNM17287dZ6mhSKxjAxM9gOc-hh1Dk/edit");
  let sheet = ss.getSheetByName("StockScan Order Tracker");

  // Check for "action" param
  let action = e.parameter.action;

  if (action === "IN" || action === "OUT" || action === "SOLD") {
    return insert(e, sheet, action);
  }
}

function doPost(e) {
  Logger.log("Received POST request: " + JSON.stringify(e));
  let ss = SpreadsheetApp.openByUrl("https://docs.google.com/spreadsheets/d/16LrNS06mjl7x0aNM17287dZ6mhSKxjAxM9gOc-hh1Dk/edit");
  let sheet = ss.getSheetByName("StockScan Order Tracker");

  // Check for "action" param
  let action = e.parameter.action;

  if (action === "IN" || action === "OUT" || action === "SOLD") {
    return insert(e, sheet, action);
  }
}

function insert(e, sheet, action) {
  let scannedData = e.parameter.sdata;
  let d = new Date();
  let ctime = d.toLocaleString();

  // Determine the start row based on the first empty row in both "IN" and "OUT" columns
  let startRowIn = getFirstEmptyRow(sheet, 1);
  let startRowOut = getFirstEmptyRow(sheet, 4);

  let startRow;
  let dataColumn;
  let productName;

  // Set start row for data insertion
  if (action === "IN" || action == "SOLD") {
    startRow = startRowIn;
    productName = scannedData.split(":")[1]; // Extract product name
  } else if (action === "OUT") {
    startRow = startRowOut;
  }

  // Determine the data column based on the action
  if (action === "IN" || action === "SOLD") {
    dataColumn = 1;
  } else if (action === "OUT") {
    dataColumn = 4;
  }

   // Insert scannedData, but only if the action is not "SOLD"
  if (action !== "SOLD") {
    sheet.getRange(startRow, dataColumn).setValue(scannedData);
    // Insert date/time
    sheet.getRange(startRow, dataColumn + 1).setValue(ctime);
  }

  // UPDATE PRODUCT COUNTER
  let sheet2 = SpreadsheetApp.openByUrl("https://docs.google.com/spreadsheets/d/1cZ7_WoVpVOr4wVqNNbMA73gQptsnOCIoDNftqNyJ4mM/edit").getSheetByName("StockScan Stock Counter"); // Access Sheet2 by URL
  let productRow = findProduct(sheet2, productName); // Find product row

  // Increment product count if coming IN
  if (action === "IN") {
    if (productRow === -1) {
      sheet2.appendRow([productName, 1]); // Add new product with initial count
    } else {
      let count = sheet2.getRange(productRow, 2).getValue(); // Get product count
      sheet2.getRange(productRow, 2).setValue(count + 1); // Update product count
    }
  }

  // Decrease product count if SOLD
  if (action === "SOLD") {
    if (productRow === -1) {
      sheet2.appendRow([productName, 1]); // Add new product with initial count 0
    } else {
      let count = sheet2.getRange(productRow, 2).getValue(); // Get product count
      if (count > 0) {  // Ensure the count doesn't go below 0
        sheet2.getRange(productRow, 2).setValue(count - 1); // Update product count
      } 
    }
  }

  return ContentService
    .createTextOutput("Success")
    .setMimeType(ContentService.MimeType.TEXT);
}

// Function to find the first empty row in a specific column
function getFirstEmptyRow(sheet, column) {
  let values = sheet.getRange(1, column, sheet.getLastRow(), 1).getValues();
  for (let i = 0; i < values.length; i++) {
    if (!values[i][0]) {
      return i + 1; // Adjust to 1-based index
    }
  }
  return values.length + 1; // If no empty row is found, return the next row
}

// Function to find a product in Sheet2
function findProduct(sheet, productName) {
  let values = sheet.getRange(1, 1, sheet.getLastRow(), 2).getValues();
  for (let i = 0; i < values.length; i++) {
    if (values[i][0] === productName) {
      return i + 1; // Adjust to 1-based index
    }
  }
  return -1; // Product not found
}






