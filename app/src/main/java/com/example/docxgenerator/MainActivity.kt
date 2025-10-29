package com.example.docxgenerator

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.docxgenerator.lib.AndroidDocBuilder
import com.example.docxgenerator.lib.RustLog
import com.example.docxgenerator.ui.theme.DocxGeneratorTheme
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : ComponentActivity() {
    val WRITE_REQUEST_CODE = 1235
    val READ_REQUEST_CODE = 1236
    val doc = AndroidDocBuilder()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showToast("App started")
        val permissionW = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionW != PackageManager.PERMISSION_GRANTED) {
            Log.i("DocumentBuilder", "Permission to write denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_REQUEST_CODE)
        }

        val permissionR = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permissionR != PackageManager.PERMISSION_GRANTED) {
            Log.i("DocumentBuilder", "Permission to read denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_REQUEST_CODE)
        }

        val folder = File(Environment.getExternalStorageDirectory().path.toString() + "/WDocuments")
        if (folder.exists()) {
            if (folder.isDirectory) {
                // The directory exists
            } else {
                folder.mkdir()
            }
        } else {
            folder.mkdir()
        }

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("fr")).format(Date())
        var fullPathToFile =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/${timeStamp}_doc.docx"
            
        setContent {
            DocxGeneratorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "DocX Generator - Enhanced Features",
                            style = MaterialTheme.typography.h5,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = longText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                        
                        var customText by remember { mutableStateOf("") }
                        var isBold by remember { mutableStateOf(false) }
                        var isItalic by remember { mutableStateOf(false) }

                        TextField(
                            value = customText,
                            onValueChange = { customText = it },
                            label = { Text("Custom Text") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = { isBold = !isBold }) {
                                Text(if (isBold) "Bold (On)" else "Bold (Off)")
                            }
                            Button(onClick = { isItalic = !isItalic }) {
                                Text(if (isItalic) "Italic (On)" else "Italic (Off)")
                            }
                        }

                        Button(
                            onClick = {
                                if (customText.isNotEmpty()) {
                                    if (doc.addFormattedText(
                                        customText,
                                        isBold,
                                        isItalic,
                                        false,
                                        12,
                                        "000000"
                                    )) {
                                        showToast("Custom text added")
                                        customText = ""
                                    } else {
                                        showToast("Failed to add custom text")
                                    }
                                } else {
                                    showToast("Please enter some text")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Custom Text")
                        }

                        var tableData by remember { mutableStateOf("") }

                        TextField(
                            value = tableData,
                            onValueChange = { tableData = it },
                            label = { Text("Table Data (CSV format)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (tableData.isNotEmpty()) {
                                    val rows = tableData.split("\n").map { it.split(",") }
                                    val jsonArray = JSONArray()
                                    for (row in rows) {
                                        val jsonRow = JSONArray()
                                        for (cell in row) {
                                            jsonRow.put(cell)
                                        }
                                        jsonArray.put(jsonRow)
                                    }
                                    if (doc.addCustomTable(jsonArray.toString())) {
                                        showToast("Custom table added")
                                        tableData = ""
                                    } else {
                                        showToast("Failed to add custom table")
                                    }
                                } else {
                                    showToast("Please enter some table data")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Custom Table")
                        }

                        // Basic Text
                        Button(
                            onClick = {
                                if (doc.addText(longText)) {
                                    showToast("Text added successfully")
                                } else {
                                    showToast("Failed to add text")
                                    Log.e("DocumentBuilder", "Failed to add text")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Plain Text")
                        }
                        
                        // Formatted Text
                        Button(
                            onClick = {
                                if (doc.addFormattedText(
                                    "Bold Red Text - Size 16", 
                                    true, 
                                    false, 
                                    false, 
                                    16, 
                                    "FF0000"
                                )) {
                                    showToast("Formatted text added")
                                } else {
                                    showToast("Failed to add formatted text")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Formatted Text (Bold Red)")
                        }
                        
                        // Italic Blue Text
                        Button(
                            onClick = {
                                doc.addFormattedText(
                                    "Italic Blue Text - Size 14", 
                                    false, 
                                    true, 
                                    false, 
                                    14, 
                                    "0000FF"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Italic Blue Text")
                        }
                        
                        // Centered Text
                        Button(
                            onClick = {
                                if (doc.addParagraphWithAlignment("This text is centered", "center")) {
                                    showToast("Centered text added")
                                } else {
                                    showToast("Failed to add centered text")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Centered Text")
                        }
                        
                        // Right Aligned Text
                        Button(
                            onClick = {
                                doc.addParagraphWithAlignment("This text is right-aligned", "right")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Right-Aligned Text")
                        }
                        
                        // Bullet List
                        Button(
                            onClick = {
                                doc.addBulletItem("First bullet point")
                                doc.addBulletItem("Second bullet point")
                                doc.addBulletItem("Third bullet point")
                                showToast("Bullet list added")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Bullet List (3 items)")
                        }
                        
                        // Numbered List
                        Button(
                            onClick = {
                                doc.addNumberedItem("First numbered item")
                                doc.addNumberedItem("Second numbered item")
                                doc.addNumberedItem("Third numbered item")
                                showToast("Numbered list added")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Numbered List (3 items)")
                        }
                        
                        // Table
                        Button(
                            onClick = {
                                if (doc.addTable(3, 3)) {
                                    showToast("3x3 table added")
                                } else {
                                    showToast("Failed to add table")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Table (3x3)")
                        }
                        
                        // Image
                        val galleryLauncher = rememberLauncherForActivityResult(
                            ActivityResultContracts.GetContent()) {
                            it?.let { uri ->
                                val path = FileUtils().getPath(this@MainActivity, uri)
                                if (path != null) {
                                    if (doc.addImage(path, 400, 300)) {
                                        showToast("Image added (with compression)")
                                    } else {
                                        showToast("Failed to add image")
                                        Log.e("DocumentBuilder", "Failed to add image")
                                    }
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(text = "Add Image (Auto-Compressed)")
                        }

                        // Generate Document
                        Button(
                            onClick = {
                                try {
                                    if (doc.generateDocx(fullPathToFile)) {
                                        showToast("Document generated successfully at $fullPathToFile")
                                        openFile(fullPathToFile)
                                    } else {
                                        showToast("Failed to generate document. Check logs for details.")
                                        Log.e("DocumentBuilder", "Failed to generate document at $fullPathToFile")
                                    }
                                } catch (e: Exception) {
                                    showToast("An error occurred: ${e.message}")
                                    Log.e("DocumentBuilder", "Exception generating document", e)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(text = "Generate & Open Document")
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun openFile(fileName: String) {
        val file = File(fileName)
        val uri: Uri = if (Build.VERSION.SDK_INT < 24) {
            Uri.fromFile(file)
        } else {
            Uri.parse(file.path)
        }
        val viewFile = Intent(Intent.ACTION_VIEW)
        viewFile.setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        startActivity(viewFile)
    }

    @Deprecated("Should change this")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("DocumentBuilder", "Permission has been denied by user")
                } else {
                    Log.i("DocumentBuilder", "Permission has been granted by user")
                }
            }
            READ_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("DocumentBuilder", "Permission has been denied by user")
                } else {
                    Log.i("DocumentBuilder", "Permission has been granted by user")
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    
    companion object {
        init {
            System.loadLibrary("docx_lib")
            RustLog.initialiseLogging()
        }
    }
}

const val longText = "Arriving at Changi airport, and after going through the immigration, I went straight to the Jewel Changi, seeing one of the iconic sites you usually would come across whenever you see anything talking about the best airports in the World. All this time, I was enjoying the free wifi so I could immediately update the status :)."