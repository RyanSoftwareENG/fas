package entities;


import java.time.LocalDateTime;

public class Examination {

    private Long examinationId;; // معرف الفحص (ID)[cite: 8]
    private Session session;    // معرف الجلسة المرتبطة[cite: 8]
    private String examinationName;
    private String examinationImage;
    private LocalDateTime uploadDate;
    private LocalDateTime modificationDate;
    private String notes;

    public Examination() {}

    public Examination(String examinationName, String examinationImage, String notes) {
        this.examinationName = examinationName;
        this.examinationImage = examinationImage;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getExaminationId() { return examinationId; }
    public void setExaminationId(Long examinationId) { this.examinationId = examinationId; }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getExaminationName() { return examinationName; }
    public void setExaminationName(String examinationName) { this.examinationName = examinationName; }

    public String getExaminationImage() { return examinationImage; }
    public void setExaminationImage(String examinationImage) { this.examinationImage = examinationImage; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    public LocalDateTime getModificationDate() { return modificationDate; }
    public void setModificationDate(LocalDateTime modificationDate) { this.modificationDate = modificationDate; }
}
