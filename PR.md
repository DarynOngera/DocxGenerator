# DocxGenerator Feature Update

This pull request introduces several new features and improvements to the DocxGenerator app, focusing on providing users with more control over the content and formatting of their documents.

## Changes

### New Features

*   **Custom Text Formatting:** Users can now type their own text, apply **bold** and *italic* formatting, and add it to the document. This is handled through a new set of UI elements at the top of the screen.
*   **Custom Tables:** Users can now create tables with their own content by providing data in a simple CSV format.

### Bug Fixes & Improvements

*   **Improved Error Handling:** The document generation process now has more robust error handling. A `try-catch` block has been added to catch exceptions, and more informative toast messages are displayed to the user, including the full file path of the generated document. This helps in debugging file permission issues and other problems.

### Code Changes

*   **`docx_lib/src/lib.rs`:**
    *   A new `add_custom_table` function has been added to the `AndroidDocBuilder` to support the creation of tables from a 2D `Vec<Vec<String>>`.
*   **`app/src/main/java/com/example/docxgenerator/MainActivity.kt`:**
    *   The UI has been updated with new `TextField` and `Button` composables for custom text and table input.
    *   State variables have been added to manage the custom text and formatting options.
    -   Logic has been added to parse CSV data for table creation.

## Motivation

The goal of these changes is to enhance the functionality of the DocxGenerator app and provide a more flexible and powerful document creation experience. The initial version of the app only allowed for pre-defined content, and the new features give users the ability to create more dynamic and customized documents.

## How to Test

1.  **Custom Text:**
    1.  Run the app.
    2.  Enter text in the "Custom Text" field.
    3.  Toggle the "Bold" and "Italic" buttons to apply formatting.
    4.  Click the "Add Custom Text" button.
    5.  Repeat with different text and formatting.
    6.  Click "Generate & Open Document" and verify that the custom text appears with the correct formatting.

2.  **Custom Tables:**
    1.  Run the app.
    2.  Enter CSV data in the "Table Data (CSV format)" field (e.g., `a,b\nc,d`).
    3.  Click the "Add Custom Table" button.
    4.  Click "Generate & Open Document" and verify that the table appears with the correct content.

3.  **Error Handling:**
    1.  To test the error handling, you can try to generate a document without granting storage permissions. The app should display a toast message indicating the failure.
