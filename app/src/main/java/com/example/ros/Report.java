package com.example.ros;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class Report extends AppCompatActivity {

    private static final String TAG = "Report";
    private FirebaseFirestore db;
    private ReportAdapter reportAdapter;
    private ArrayList<ReportC> reportList;
    private ListenerRegistration reportListener;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        db = FirebaseFirestore.getInstance();

        isAdmin = FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(MainActivity.ADMIN_EMAIL);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(this, reportList);
        recyclerView.setAdapter(reportAdapter);

        FloatingActionButton addReport = findViewById(R.id.addReport);
        addReport.setOnClickListener(view -> {
            Intent intent = new Intent(Report.this, AddReport.class);
            startActivity(intent);
        });

        FloatingActionButton refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(view -> loadReports());

        loadReports();
    }

    private void loadReports() {
        if (reportListener != null) {
            reportListener.remove();
        }

        reportListener = db.collection("dailyReports")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Listen failed.", error);
                        Toast.makeText(Report.this, "Error loading reports: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        reportList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            ReportC report = doc.toObject(ReportC.class);
                            report.setId(doc.getId());
                            reportList.add(report);
                        }
                        reportAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Number of reports loaded: " + reportList.size());

                        if (reportList.isEmpty()) {
                            Toast.makeText(Report.this, "No reports found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        reportAdapter.setOnItemClickListener(reportc -> {
            Intent intent;
            if (isAdmin) {
                intent = new Intent(Report.this, ViewReportActivity.class);
            } else {
                intent = new Intent(Report.this, EditReport.class);
            }
            intent.putExtra("reportId", reportc.getId());
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reportListener != null) {
            reportListener.remove();
        }
    }
}