package com.example.ros;

public class ReportC {
    public static ReportC reportc;
    private String id;
    private String date;
    private String profit;
    private String loss;
    private String notes;
    private String coins;
    private String paybill;
    private String total;
    private String expenses;
    private String reportImageUrl;
    private String senderName;
    private String title;
    private String userEmail;

    public ReportC() {}


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getProfit() { return profit; }
    public void setProfit(String profit) { this.profit = profit; }

    public String getLoss() { return loss; }
    public void setLoss(String loss) { this.loss = loss; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCoins() { return coins; }
    public void setCoins(String coins) { this.coins = coins; }

    public String getPaybill() { return paybill; }
    public void setPaybill(String paybill) { this.paybill = paybill; }

    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }

    public String getExpenses() { return expenses; }
    public void setExpenses(String expenses) { this.expenses = expenses; }

    public String getReportImageUrl() { return reportImageUrl; }
    public void setReportImageUrl(String reportImageUrl) { this.reportImageUrl = reportImageUrl; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}