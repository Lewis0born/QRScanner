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
4. Select whether stock is being sent IN or OUT (in store or been delivered).
5. The scanned data will be sent to the specified Google Sheets document.

## Google Apps Script

```javascript
function doGet(e) {
  Logger.log("Received GET request: " + JSON.stringify(e));
  let ss = SpreadsheetApp.openByUrl("https://docs.google.com/spreadsheets/d/1NRGIz85f7rwK7Y4O9KX_ko6EOEn6Ngdf3W7j4Ib0W5c/edit");
  let sheet = ss.getSheetByName("Sheet1");

  // Check for "action" param
  let action = e.parameter.action;

  if(action === "IN" || action === "OUT"){
    return insert(e, sheet, action);
  }

}

function doPost(e) {
  Logger.log("Received POST request: " + JSON.stringify(e));
  let ss = SpreadsheetApp.openByUrl("https://docs.google.com/spreadsheets/d/1NRGIz85f7rwK7Y4O9KX_ko6EOEn6Ngdf3W7j4Ib0W5c/edit");
  let sheet = ss.getSheetByName("Sheet1");

  // Check for "action" param
  let action = e.parameter.action;

  if(action === "IN" || action === "OUT"){
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

  // Set start row for data insertion
  if(action ==="IN"){
    startRow = startRowIn;
  } else if(action === "OUT"){
    startRow = startRowOut;
  }

  // Determine the data column based on the action
  if(action === "IN"){
    dataColumn = 1;
  } else if(action === "OUT"){
    dataColumn = 4;
  }

  // Insert scannedData
  sheet.getRange(startRow, dataColumn).setValue(scannedData);

  // Insert date/time
  sheet.getRange(startRow, dataColumn + 1).setValue(ctime);

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




