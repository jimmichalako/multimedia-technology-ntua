package com.example.demo;

/**
 * Η κλάση Reminder αναπαριστά μια υπενθύμιση που συνδέεται με μια εργασία.
 */
public class Reminder {
    private String taskTitle;
    private String reminderDate;
    private String reminderType;

    /**
     * Δημιουργεί μια νέα υπενθύμιση.
     *
     * @param taskTitle     Ο τίτλος της εργασίας που σχετίζεται με την υπενθύμιση.
     * @param reminderDate  Η ημερομηνία της υπενθύμισης.
     * @param reminderType  Ο τύπος της υπενθύμισης.
     */
    public Reminder(String taskTitle, String reminderDate, String reminderType) {
        this.taskTitle = taskTitle;
        this.reminderDate = reminderDate;
        this.reminderType = reminderType;
    }

    /**
     * Επιστρέφει τον τίτλο της εργασίας που σχετίζεται με την υπενθύμιση.
     *
     * @return Ο τίτλος της εργασίας.
     */
    public String getTaskTitle() {
        return taskTitle;
    }

    /**
     * Ορίζει νέο τίτλο εργασίας για την υπενθύμιση.
     *
     * @param taskTitle Ο νέος τίτλος της εργασίας.
     */
    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    /**
     * Επιστρέφει την ημερομηνία της υπενθύμισης.
     *
     * @return Η ημερομηνία της υπενθύμισης.
     */
    public String getReminderDate() {
        return reminderDate;
    }

    /**
     * Ορίζει νέα ημερομηνία για την υπενθύμιση.
     *
     * @param reminderDate Η νέα ημερομηνία της υπενθύμισης.
     */
    public void setReminderDate(String reminderDate) {
        this.reminderDate = reminderDate;
    }

    /**
     * Επιστρέφει τον τύπο της υπενθύμισης.
     *
     * @return Ο τύπος της υπενθύμισης.
     */
    public String getReminderType() {
        return reminderType;
    }

    /**
     * Ορίζει νέο τύπο για την υπενθύμιση.
     *
     * @param reminderType Ο νέος τύπος της υπενθύμισης.
     */
    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }
}
