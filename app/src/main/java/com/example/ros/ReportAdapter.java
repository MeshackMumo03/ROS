package com.example.ros;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ReportC> reportList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(ReportC report);
    }

    public ReportAdapter(Context context, ArrayList<ReportC> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_report_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportC report = reportList.get(position);
        holder.dateTextView.setText("Date: " + report.getDate());
        holder.profitTextView.setText("Profit: " + report.getProfit());
        holder.senderNameTextView.setText("Sender: " + (report.getSenderName() != null ? report.getSenderName() : "Unknown"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(report);
            }
            });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView profitTextView;
        TextView senderNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            profitTextView = itemView.findViewById(R.id.profitTextView);
            senderNameTextView = itemView.findViewById(R.id.senderNameTextView);
        }
    }
}