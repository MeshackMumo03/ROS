package com.example.ros;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddReport extends AppCompatActivity {

    private EditText dateField, profitField, lossField, notesField, coinsField, paybillField, totalField, expensesField;
    private ImageView reportImageView;
    private Button submitButton, uploadImageButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Uri reportImageUri;
    private FirebaseStorage storage;
    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private ArrayList<ReportC> reportList;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        initViews();
        initFirebase();

        reportImageView.setOnClickListener(v -> openFileChooser());
        uploadImageButton.setOnClickListener(v -> openFileChooser());
        submitButton.setOnClickListener(view -> submitReport());

        setupRecyclerView();
        loadReports();
    }

    private void initViews() {
        dateField = findViewById(R.id.date);
        profitField = findViewById(R.id.profit);
        lossField = findViewById(R.id.loss);
        notesField = findViewById(R.id.money_notes);
        coinsField = findViewById(R.id.money_coins);
        paybillField = findViewById(R.id.money_paybill);
        totalField = findViewById(R.id.total);
        expensesField = findViewById(R.id.expenses);
        reportImageView = findViewById(R.id.report_image);
        submitButton = findViewById(R.id.submit_button);
        uploadImageButton = findViewById(R.id.upload_image_button);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            reportList = new ArrayList<>();
            reportAdapter = new ReportAdapter(this, reportList);
            recyclerView.setAdapter(reportAdapter);
        } else {
            Toast.makeText(this, "RecyclerView not found in layout", Toast.LENGTH_SHORT).show();
        }
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

    private void submitReport() {
        String date = dateField.getText().toString().trim();
        String profit = profitField.getText().toString().trim();
        String loss = lossField.getText().toString().trim();
        String notes = notesField.getText().toString().trim();
        String coins = coinsField.getText().toString().trim();
        String paybill = paybillField.getText().toString().trim();
        String total = totalField.getText().toString().trim();
        String expenses = expensesField.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(profit) || TextUtils.isEmpty(loss) || TextUtils.isEmpty(notes) ||
                TextUtils.isEmpty(coins) || TextUtils.isEmpty(paybill) || TextUtils.isEmpty(total) || TextUtils.isEmpty(expenses) || reportImageUri == null) {
            Toast.makeText(getApplicationContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(AddReport.this, "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();
        uploadReportImage(date, profit, loss, notes, coins, paybill, total, expenses, userEmail);
    }

    private void uploadReportImage(String date, String profit, String loss, String notes, String coins, String paybill, String total, String expenses, String userEmail) {
        StorageReference fileReference = storage.getReference("reportImages")
                .child(System.currentTimeMillis() + ".jpg");

        fileReference.putFile(reportImageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fileReference.getDownloadUrl().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        String reportImageUrl = task1.getResult().toString();
                        saveReportToDatabase(date, profit, loss, notes, coins, paybill, total, expenses, userEmail, reportImageUrl);
                    } else {
                        showToast("Failed to get report image URL");
                    }
                });
            } else {
                showToast("Report image upload failed: " + task.getException());
            }
        });
    }

    private void saveReportToDatabase(String date, String profit, String loss, String notes, String coins, String paybill, String total, String expenses, String userEmail, String reportImageUrl) {
        Map<String, Object> report = new HashMap<>();
        report.put("date", date);
        report.put("profit", profit);
        report.put("loss", loss);
        report.put("notes", notes);
        report.put("coins", coins);
        report.put("paybill", paybill);
        report.put("total", total);
        report.put("expenses", expenses);
        report.put("userEmail", userEmail);
        report.put("reportImageUrl", reportImageUrl);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String senderName = currentUser != null ? currentUser.getDisplayName() : "Unknown User";
        report.put("senderName", senderName);

        db.collection("dailyReports").add(report)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Report submitted successfully!");
                        loadReports();
                    } else {
                        showToast("Failed to submit report: " + task.getException());
                    }
                });
    }

    private void loadReports() {
        if (reportList == null || reportAdapter == null) {
            Toast.makeText(this, "Report list or adapter not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("dailyReports").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reportList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    ReportC reportC = document.toObject(ReportC.class);
                    reportC.setId(document.getId());
                    reportList.add(reportC);
                }
                reportAdapter.notifyDataSetChanged();
            } else {
                showToast("Failed to load reports: " + task.getException());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(AddReport.this, message, Toast.LENGTH_SHORT).show();
    }
}