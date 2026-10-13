package com.example.demo;

/**
 * Η κλάση Task αναπαριστά μια εργασία στο σύστημα διαχείρισης εργασιών.
 * Κάθε εργασία έχει τίτλο, περιγραφή, κατηγορία, προτεραιότητα, προθεσμία και κατάσταση.
 */
public class Task {
    private String title;
    private String description;
    private String category;
    private String priority;
    private String deadline; // Format: DD-MM-YYYY
    private String status; // Open, In Progress, Postponed, Completed, Delayed

    /**
     * Δημιουργεί μια νέα εργασία με προκαθορισμένη κατάσταση "Open".
     *
     * @param title       Ο τίτλος της εργασίας.
     * @param description Η περιγραφή της εργασίας.
     * @param category    Η κατηγορία στην οποία ανήκει η εργασία.
     * @param priority    Το επίπεδο προτεραιότητας της εργασίας.
     * @param deadline    Η προθεσμία ολοκλήρωσης της εργασίας (μορφή: DD-MM-YYYY).
     */
    public Task(String title, String description, String category, String priority, String deadline) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.deadline = deadline;
        this.status = "Open";
    }

    /**
     * Επιστρέφει τον τίτλο της εργασίας.
     *
     * @return Ο τίτλος της εργασίας.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Ορίζει νέο τίτλο για την εργασία.
     *
     * @param title Ο νέος τίτλος της εργασίας.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Επιστρέφει την περιγραφή της εργασίας.
     *
     * @return Η περιγραφή της εργασίας.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ορίζει νέα περιγραφή για την εργασία.
     *
     * @param description Η νέα περιγραφή της εργασίας.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Επιστρέφει την κατηγορία της εργασίας.
     *
     * @return Η κατηγορία της εργασίας.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Ορίζει νέα κατηγορία για την εργασία.
     *
     * @param category Η νέα κατηγορία της εργασίας.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Επιστρέφει την προτεραιότητα της εργασίας.
     *
     * @return Η προτεραιότητα της εργασίας.
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Ορίζει νέα προτεραιότητα για την εργασία.
     *
     * @param priority Η νέα προτεραιότητα της εργασίας.
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }

    /**
     * Επιστρέφει την προθεσμία της εργασίας.
     *
     * @return Η προθεσμία της εργασίας σε μορφή DD-MM-YYYY.
     */
    public String getDeadline() {
        return deadline;
    }

    /**
     * Ορίζει νέα προθεσμία για την εργασία.
     *
     * @param deadline Η νέα προθεσμία της εργασίας (μορφή: DD-MM-YYYY).
     */
    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    /**
     * Επιστρέφει την κατάσταση της εργασίας.
     *
     * @return Η κατάσταση της εργασίας (Open, In Progress, Postponed, Completed, Delayed).
     */
    public String getStatus() {
        return status;
    }

    /**
     * Ορίζει νέα κατάσταση για την εργασία.
     *
     * @param status Η νέα κατάσταση της εργασίας.
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
