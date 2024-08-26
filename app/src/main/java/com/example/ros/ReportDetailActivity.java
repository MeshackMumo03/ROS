package com.example.ros;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportDetailActivity extends AppCompatActivity {

    private TextView tvDate, tvProfit, tvLoss, tvNotes, tvCoins, tvPaybill, tvTotal, tvExpenses, tvUserEmail;
    private ImageView ivReportImage;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        db = FirebaseFirestore.getInstance();

        initializeViews();

        String reportId = getIntent().getStringExtra("reportId");
        if (reportId != null) {
            loadReportDetails(reportId);
        } else {
            Toast.makeText(this, "Report ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        tvDate = findViewById(R.id.tvDate);
        tvProfit = findViewById(R.id.tvProfit);
        tvLoss = findViewById(R.id.tvLoss);
        tvNotes = findViewById(R.id.tvNotes);
        tvCoins = findViewById(R.id.tvCoins);
        tvPaybill = findViewById(R.id.tvPaybill);
        tvTotal = findViewById(R.id.tvTotal);
        tvExpenses = findViewById(R.id.tvExpenses);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        ivReportImage = findViewById(R.id.ivReportImage);
    }

    private void loadReportDetails(String reportId) {
        db.collection("dailyReports").document(reportId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ReportC report = documentSnapshot.toObject(ReportC.class);
                        if (report != null) {
                            displayReportDetails(report);
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

    private void displayReportDetails(ReportC report) {
        tvDate.setText("Date: " + report.getDate());
        tvProfit.setText("Profit: " + report.getProfit());
        tvLoss.setText("Loss: " + report.getLoss());
        tvNotes.setText("Notes: " + report.getNotes());
        tvCoins.setText("Coins: " + report.getCoins());
        tvPaybill.setText("Paybill: " + report.getPaybill());
        tvTotal.setText("Total: " + report.getTotal());
        tvExpenses.setText("Expenses: " + report.getExpenses());
        tvUserEmail.setText("User Email: " + report.getUserEmail());

        if (report.getReportImageUrl() != null && !report.getReportImageUrl().isEmpty()) {
            Glide.with(this).load(report.getReportImageUrl()).into(ivReportImage);
        }
    }
}