package com.example.ros;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditReport extends AppCompatActivity {

    private EditText inputDate, inputProfit, inputLoss, inputNotes, inputCoins, inputPaybill, inputTotal, inputExpenses;
    private ImageView reportImageView;
    private Button saveButton, deleteButton;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri reportImageUri;
    private String reportImageUrl;
    private String reportId;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_report);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeViews();

        reportId = getIntent().getStringExtra("reportId");
        if (reportId != null) {
            loadReportDetails(reportId);
        } else {
            Toast.makeText(this, "No report data available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        reportImageView.setOnClickListener(v -> openFileChooser());
        saveButton.setOnClickListener(v -> saveReport());
        deleteButton.setOnClickListener(v -> confirmDelete());
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Report")
                .setMessage("Are you sure you want to delete this report?")
                .setPositiveButton("Yes", (dialog, which) -> deleteReport())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteReport() {
        db.collection("dailyReports").document(reportId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditReport.this, "Report deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(EditReport.this, "Error deleting report: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void initializeViews() {
        inputDate = findViewById(R.id.date);
        inputProfit = findViewById(R.id.profit);
        inputLoss = findViewById(R.id.loss);
        inputNotes = findViewById(R.id.money_notes);
        inputCoins = findViewById(R.id.money_coins);
        inputPaybill = findViewById(R.id.money_paybill);
        inputTotal = findViewById(R.id.total);
        inputExpenses = findViewById(R.id.expenses);
        reportImageView = findViewById(R.id.report_image);
        saveButton = findViewById(R.id.save);
        deleteButton = findViewById(R.id.delete);
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            reportImageUri = data.getData();
            reportImageView.setImageURI(reportImageUri);
        }
    }

    private void loadReportDetails(String reportId) {
        db.collection("dailyReports").document(reportId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ReportC reportc = documentSnapshot.toObject(ReportC.class);
                        if (reportc != null) {
                            inputDate.setText(reportc.getDate());
                            inputProfit.setText(reportc.getProfit());
                            inputLoss.setText(reportc.getLoss());
                            inputNotes.setText(reportc.getNotes());
                            inputCoins.setText(reportc.getCoins());
                            inputPaybill.setText(reportc.getPaybill());
                            inputTotal.setText(reportc.getTotal());
                            inputExpenses.setText(reportc.getExpenses());

                            if (reportc.getReportImageUrl() != null && !reportc.getReportImageUrl().isEmpty()) {
                                reportImageUrl = reportc.getReportImageUrl();
                                Glide.with(this).load(reportImageUrl).into(reportImageView);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Report not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading report: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void saveReport() {
        String date = inputDate.getText().toString().trim();
        String profit = inputProfit.getText().toString().trim();
        String loss = inputLoss.getText().toString().trim();
        String notes = inputNotes.getText().toString().trim();
        String coins = inputCoins.getText().toString().trim();
        String paybill = inputPaybill.getText().toString().trim();
        String total = inputTotal.getText().toString().trim();
        String expenses = inputExpenses.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(profit) || TextUtils.isEmpty(loss) || TextUtils.isEmpty(notes) ||
                TextUtils.isEmpty(coins) || TextUtils.isEmpty(paybill) || TextUtils.isEmpty(total) || TextUtils.isEmpty(expenses)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reportImageUri != null) {
            uploadReportImage(date, profit, loss, notes, coins, paybill, total, expenses);
        } else {
            saveReportToDatabase(date, profit, loss, notes, coins, paybill, total, expenses, reportImageUrl);
        }
    }

    private void uploadReportImage(final String date, final String profit, final String loss, final String notes,
                                   final String coins, final String paybill, final String total, final String expenses) {
        final StorageReference fileReference = storage.getReference("reportImages")
                .child(System.currentTimeMillis() + ".jpg");

        fileReference.putFile(reportImageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fileReference.getDownloadUrl().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        String newReportImageUrl = task1.getResult().toString();
                        saveReportToDatabase(date, profit, loss, notes, coins, paybill, total, expenses, newReportImageUrl);
                    } else {
                        Toast.makeText(EditReport.this, "Failed to get report image URL", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(EditReport.this, "Report image upload failed: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveReportToDatabase(String date, String profit, String loss, String notes, String coins,
                                      String paybill, String total, String expenses, String reportImageUrl) {
        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("profit", profit);
        report.put("loss", loss);
        report.put("notes", notes);
        report.put("coins", coins);
        report.put("paybill", paybill);
        report.put("total", total);
        report.put("expenses", expenses);
        report.put("reportImageUrl", reportImageUrl);


        String userEmail = "";
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userEmail = auth.getCurrentUser().getEmail();
        }


        report.put("userEmail", userEmail != null ? userEmail : "admin@example.com");

        db.collection("dailyReports").document(reportId).set(report)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditReport.this, "Report updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditReport.this, "Failed to update report: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}