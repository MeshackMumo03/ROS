package com.example.ros;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Admin extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private ArrayList<ReportC> reportList;
    private FirebaseFirestore db;
    private ListenerRegistration reportListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();

        FloatingActionButton refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(v -> loadReports());

        loadReports();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(this, reportList);
        recyclerView.setAdapter(reportAdapter);
    }

    private void loadReports() {
        if (reportListener != null) {
            reportListener.remove();
        }

        reportListener = db.collection("dailyReports")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w(TAG, "Listen failed.", error);
                            Toast.makeText(Admin.this, "Error loading reports: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(Admin.this, "No reports found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        reportAdapter.setOnItemClickListener(report -> {
            Intent intent = new Intent(Admin.this, ViewReportActivity.class);
            intent.putExtra("reportId", report.getId());
            startActivity(intent);
        });
    }
}
