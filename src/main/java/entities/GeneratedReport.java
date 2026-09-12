package entities;

import java.time.LocalDateTime;

public class GeneratedReport {
    private long reportId;
    private long clientId;
    private String clientName;
    private String filePath;
    private LocalDateTime generatedDate;

    public GeneratedReport() {}

    public GeneratedReport(long reportId, long clientId, String clientName, String filePath, LocalDateTime generatedDate) {
        this.reportId = reportId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.filePath = filePath;
        this.generatedDate = generatedDate;
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }
}