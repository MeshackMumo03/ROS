package com.example.ros;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewReportActivity extends AppCompatActivity {

    private TextView dateView, profitView, lossView, notesView, coinsView, paybillView, totalView, expensesView, senderNameView;
    private ImageView reportImageView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_report);

        db = FirebaseFirestore.getInstance();

        initializeViews();

        String reportId = getIntent().getStringExtra("reportId");
        if (reportId != null) {
            loadReportDetails(reportId);
        } else {
            Toast.makeText(this, "No report data available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        dateView = findViewById(R.id.date);
        profitView = findViewById(R.id.profit);
        lossView = findViewById(R.id.loss);
        notesView = findViewById(R.id.money_notes);
        coinsView = findViewById(R.id.money_coins);
        paybillView = findViewById(R.id.money_paybill);
        totalView = findViewById(R.id.total);
        expensesView = findViewById(R.id.expenses);
        senderNameView = findViewById(R.id.sender_name);
        reportImageView = findViewById(R.id.report_image);
    }

    private void loadReportDetails(String reportId) {
        db.collection("dailyReports").document(reportId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ReportC reportc = documentSnapshot.toObject(ReportC.class);
                        if (reportc != null) {
                            dateView.setText("Date: " + reportc.getDate());
                            profitView.setText("Profit: " + reportc.getProfit());
                            lossView.setText("Loss: " + reportc.getLoss());
                            notesView.setText("Notes: " + reportc.getNotes());
                            coinsView.setText("Coins: " + reportc.getCoins());
                            paybillView.setText("Paybill: " + reportc.getPaybill());
                            totalView.setText("Total: " + reportc.getTotal());
                            expensesView.setText("Expenses: " + reportc.getExpenses());
                            senderNameView.setText("Sender: " + (reportc.getSenderName() != null ? reportc.getSenderName() : "Unknown"));

                            if (reportc.getReportImageUrl() != null && !reportc.getReportImageUrl().isEmpty()) {
                                Glide.with(this).load(reportc.getReportImageUrl()).into(reportImageView);
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
}