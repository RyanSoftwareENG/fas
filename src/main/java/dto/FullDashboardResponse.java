package dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FullDashboardResponse {

    private Dashboard overview;
    private List<SessionTrend> sessionsTrend;
    private List<RevenueTrend> revenueTrend;
    private List<PlanStatus> planStatuses;
    private List<ChronicDisease> chronicDiseases;
    private List<Allergy> allergies;
    private List<RecentSession> recentSessions;
    private List<ReportAlert> alerts;

    public FullDashboardResponse() {
    }

    public Dashboard getOverview() {
        return overview;
    }

    public void setOverview(Dashboard overview) {
        this.overview = overview;
    }

    public List<SessionTrend> getSessionsTrend() {
        return sessionsTrend;
    }

    public void setSessionsTrend(List<SessionTrend> sessionsTrend) {
        this.sessionsTrend = sessionsTrend;
    }

    public List<RevenueTrend> getRevenueTrend() {
        return revenueTrend;
    }

    public void setRevenueTrend(List<RevenueTrend> revenueTrend) {
        this.revenueTrend = revenueTrend;
    }

    public List<PlanStatus> getPlanStatuses() {
        return planStatuses;
    }

    public void setPlanStatuses(List<PlanStatus> planStatuses) {
        this.planStatuses = planStatuses;
    }

    public List<ChronicDisease> getChronicDiseases() {
        return chronicDiseases;
    }

    public void setChronicDiseases(List<ChronicDisease> chronicDiseases) {
        this.chronicDiseases = chronicDiseases;
    }

    public List<Allergy> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<Allergy> allergies) {
        this.allergies = allergies;
    }

    public List<RecentSession> getRecentSessions() {
        return recentSessions;
    }

    public void setRecentSessions(List<RecentSession> recentSessions) {
        this.recentSessions = recentSessions;
    }

    public List<ReportAlert> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<ReportAlert> alerts) {
        this.alerts = alerts;
    }

    // =====================================================
    // Overview
    // =====================================================

    public static class Dashboard {

        private long totalClients;
        private long newClients;
        private long totalSessions;
        private long activePlans;
        private double periodRevenue;

        private double clientsGrowth;
        private double sessionsGrowth;
        private double plansGrowth;

        private long completedPlans;
        private long cancelledPlans;

        private double averageSessionDuration;

        public Dashboard() {
        }

        public long getTotalClients() {
            return totalClients;
        }

        public void setTotalClients(long totalClients) {
            this.totalClients = totalClients;
        }

        public long getNewClients() {
            return newClients;
        }

        public void setNewClients(long newClients) {
            this.newClients = newClients;
        }

        public long getTotalSessions() {
            return totalSessions;
        }

        public void setTotalSessions(long totalSessions) {
            this.totalSessions = totalSessions;
        }

        public long getActivePlans() {
            return activePlans;
        }

        public void setActivePlans(long activePlans) {
            this.activePlans = activePlans;
        }

        public double getPeriodRevenue() {
            return periodRevenue;
        }

        public void setPeriodRevenue(double periodRevenue) {
            this.periodRevenue = periodRevenue;
        }

        public double getClientsGrowth() {
            return clientsGrowth;
        }

        public void setClientsGrowth(double clientsGrowth) {
            this.clientsGrowth = clientsGrowth;
        }

        public double getSessionsGrowth() {
            return sessionsGrowth;
        }

        public void setSessionsGrowth(double sessionsGrowth) {
            this.sessionsGrowth = sessionsGrowth;
        }

        public double getPlansGrowth() {
            return plansGrowth;
        }

        public void setPlansGrowth(double plansGrowth) {
            this.plansGrowth = plansGrowth;
        }

        public long getCompletedPlans() {
            return completedPlans;
        }

        public void setCompletedPlans(long completedPlans) {
            this.completedPlans = completedPlans;
        }

        public long getCancelledPlans() {
            return cancelledPlans;
        }

        public void setCancelledPlans(long cancelledPlans) {
            this.cancelledPlans = cancelledPlans;
        }

        public double getAverageSessionDuration() {
            return averageSessionDuration;
        }

        public void setAverageSessionDuration(
                double averageSessionDuration
        ) {
            this.averageSessionDuration =
                    averageSessionDuration;
        }
    }

    // =====================================================
    // Sessions Trend
    // =====================================================

    public static class SessionTrend {

        private LocalDate date;
        private long count;

        public SessionTrend() {
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    // =====================================================
    // Revenue Trend
    // =====================================================

    public static class RevenueTrend {

        private LocalDate date;
        private double revenue;

        public RevenueTrend() {
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public double getRevenue() {
            return revenue;
        }

        public void setRevenue(double revenue) {
            this.revenue = revenue;
        }
    }

    // =====================================================
    // Plans
    // =====================================================

    public static class PlanStatus {

        private String status;
        private long count;

        public PlanStatus() {
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    // =====================================================
    // Chronic Diseases
    // =====================================================

    public static class ChronicDisease {

        private String diseaseName;
        private long patientCount;

        public ChronicDisease() {
        }

        public String getDiseaseName() {
            return diseaseName;
        }

        public void setDiseaseName(String diseaseName) {
            this.diseaseName = diseaseName;
        }

        public long getPatientCount() {
            return patientCount;
        }

        public void setPatientCount(long patientCount) {
            this.patientCount = patientCount;
        }
    }

    // =====================================================
    // Allergies
    // =====================================================

    public static class Allergy {

        private String allergyName;
        private long patientCount;

        public Allergy() {
        }

        public String getAllergyName() {
            return allergyName;
        }

        public void setAllergyName(String allergyName) {
            this.allergyName = allergyName;
        }

        public long getPatientCount() {
            return patientCount;
        }

        public void setPatientCount(long patientCount) {
            this.patientCount = patientCount;
        }
    }

    // =====================================================
    // Recent Sessions
    // =====================================================

    public static class RecentSession {

        private Long sessionId;
        private Long clientId;
        private String clientName;
        private LocalDateTime sessionDate;
        private String duration;
        private Double price;
        private String notes;

        public RecentSession() {
        }

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public Long getClientId() {
            return clientId;
        }

        public void setClientId(Long clientId) {
            this.clientId = clientId;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }

        public LocalDateTime getSessionDate() {
            return sessionDate;
        }

        public void setSessionDate(LocalDateTime sessionDate) {
            this.sessionDate = sessionDate;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    // =====================================================
    // Alerts
    // =====================================================

    public static class ReportAlert {

        private String type;
        private String severity;
        private String title;
        private String message;
        private long count;

        public ReportAlert() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}