package com.loanmanagement.analyst;

import java.util.List;

public class AnalystDashboardDTO {
    
    private String approvalRatio;
    private String defaultRate;
    private String avgLoanSize;
    private String revenueGrowth;

    private List<ApprovalData> approvalAnalysis;
    private List<DefaultCategoryData> defaultsByCategory;
    private List<RevenueData> revenueVsTarget;

    public AnalystDashboardDTO() {}

    public String getApprovalRatio() { return approvalRatio; }
    public void setApprovalRatio(String approvalRatio) { this.approvalRatio = approvalRatio; }

    public String getDefaultRate() { return defaultRate; }
    public void setDefaultRate(String defaultRate) { this.defaultRate = defaultRate; }

    public String getAvgLoanSize() { return avgLoanSize; }
    public void setAvgLoanSize(String avgLoanSize) { this.avgLoanSize = avgLoanSize; }

    public String getRevenueGrowth() { return revenueGrowth; }
    public void setRevenueGrowth(String revenueGrowth) { this.revenueGrowth = revenueGrowth; }

    public List<ApprovalData> getApprovalAnalysis() { return approvalAnalysis; }
    public void setApprovalAnalysis(List<ApprovalData> approvalAnalysis) { this.approvalAnalysis = approvalAnalysis; }

    public List<DefaultCategoryData> getDefaultsByCategory() { return defaultsByCategory; }
    public void setDefaultsByCategory(List<DefaultCategoryData> defaultsByCategory) { this.defaultsByCategory = defaultsByCategory; }

    public List<RevenueData> getRevenueVsTarget() { return revenueVsTarget; }
    public void setRevenueVsTarget(List<RevenueData> revenueVsTarget) { this.revenueVsTarget = revenueVsTarget; }

    public static class ApprovalData {
        private String name;
        private int value;

        public ApprovalData(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class DefaultCategoryData {
        private String category;
        private int defaults;

        public DefaultCategoryData(String category, int defaults) {
            this.category = category;
            this.defaults = defaults;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public int getDefaults() { return defaults; }
        public void setDefaults(int defaults) { this.defaults = defaults; }
    }

    public static class RevenueData {
        private String month;
        private double revenue;
        private double target;

        public RevenueData(String month, double revenue, double target) {
            this.month = month;
            this.revenue = revenue;
            this.target = target;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public double getRevenue() { return revenue; }
        public void setRevenue(double revenue) { this.revenue = revenue; }

        public double getTarget() { return target; }
        public void setTarget(double target) { this.target = target; }
    }
}