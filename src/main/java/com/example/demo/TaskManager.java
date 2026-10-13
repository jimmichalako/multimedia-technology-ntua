package com.example.demo;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Η κλάση TaskManager διαχειρίζεται τις εργασίες, τις κατηγορίες, τις προτεραιότητες και τις υπενθυμίσεις.
 * Υποστηρίζει την προσθήκη, διαγραφή, ενημέρωση και αποθήκευση των δεδομένων σε αρχεία JSON.
 */
public class TaskManager {
    private static final String DIRECTORY = "medialab";
    private static final String TASKS_FILE = DIRECTORY + "/tasks.json";
    private static final String CATEGORIES_FILE = DIRECTORY + "/categories.json";
    private static final String PRIORITIES_FILE = DIRECTORY + "/priorities.json";
    private static final String REMINDERS_FILE = DIRECTORY + "/reminders.json";

    private final ObservableList<Task> tasks;
    private final ObservableList<Category> categories;
    private final ObservableList<Priority> priorities;
    private final ObservableList<Reminder> reminders;

    /**
     * Αρχικοποιεί τα αρχεία αποθήκευσης και φορτώνει τα δεδομένα από JSON αρχεία.
     */
    public TaskManager() {
        initializeFiles();

        tasks = FXCollections.observableArrayList(DataHandler.loadData(TASKS_FILE, new TypeToken<List<Task>>() {
        }.getType()));
        categories = FXCollections.observableArrayList(DataHandler.loadData(CATEGORIES_FILE, new TypeToken<List<Category>>() {
        }.getType()));
        priorities = FXCollections.observableArrayList(DataHandler.loadData(PRIORITIES_FILE, new TypeToken<List<Priority>>() {
        }.getType()));
        reminders = FXCollections.observableArrayList(DataHandler.loadData(REMINDERS_FILE, new TypeToken<List<Reminder>>() {
        }.getType()));

        if (priorities.stream().noneMatch(p -> p.getName().equals("Default"))) {
            priorities.add(new Priority("Default"));
        }
    }

    /**
     * Δημιουργεί τον φάκελο αποθήκευσης αν δεν υπάρχει και τα αντίστοιχα αρχεία JSON.
     */
    private void initializeFiles() {
        File dir = new File(DIRECTORY);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directory 'medialab' created successfully!");
            } else {
                System.err.println("Failed to create directory 'medialab'.");
            }
        }
        createFileIfNotExists(TASKS_FILE);
        createFileIfNotExists(CATEGORIES_FILE);
        createFileIfNotExists(PRIORITIES_FILE);
        createFileIfNotExists(REMINDERS_FILE);
    }

    /**
     * Δημιουργεί ένα νέο αρχείο JSON αν δεν υπάρχει ήδη.
     *
     * @param filePath Η διαδρομή του αρχείου που θα δημιουργηθεί.
     */
    private void createFileIfNotExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("[]"); // Γράφει μια κενή λίστα JSON
                System.out.println("File " + filePath + " created successfully!");
            } catch (IOException e) {
                System.err.println("Failed to create file " + filePath + ": " + e.getMessage());
            }
        }
    }

    /**
     * Επιστρέφει τη λίστα με όλες τις εργασίες.
     *
     * @return Λίστα εργασιών.
     */
    public ObservableList<Task> getTasks() {
        return tasks;
    }

    /**
     * Προσθέτει μια νέα εργασία στη λίστα.
     *
     * @param task Η εργασία που θα προστεθεί.
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Ενημερώνει τα στοιχεία μιας εργασίας.
     *
     * @param task        Η εργασία προς ενημέρωση.
     * @param title       Νέος τίτλος.
     * @param description Νέα περιγραφή.
     * @param category    Νέα κατηγορία.
     * @param priority    Νέα προτεραιότητα.
     * @param deadline    Νέα προθεσμία.
     * @param status      Νέα κατάσταση.
     */
    public void updateTask(Task task, String title, String description, String category, String priority, String deadline, String status) {
        task.setTitle(title);
        task.setDescription(description);
        task.setCategory(category);
        task.setPriority(priority);
        task.setDeadline(deadline);
        if (!"Completed".equals(task.getStatus()) && "Completed".equals(status)) {
            removeRemindersForTask(task);
        }
        task.setStatus(status);
        updateDelayedTasks();
        tasks.set(tasks.indexOf(task), task);
    }

    /**
     * Διαγράφει μια εργασία από τη λίστα και αφαιρεί τις σχετικές υπενθυμίσεις.
     *
     * @param task Η εργασία που θα διαγραφεί.
     */
    public void removeTask(Task task) {
        removeRemindersForTask(task);
        tasks.remove(task);
    }

    /**
     * Ενημερώνει τις εργασίες που έχουν ξεπεράσει την προθεσμία και αλλάζει την κατάστασή τους σε "Delayed".
     */
    public void updateDelayedTasks() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (Task task : tasks) {
            if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
                LocalDate taskDeadline = LocalDate.parse(task.getDeadline(), formatter);
                if (taskDeadline.isBefore(currentDate) && !"Completed".equals(task.getStatus())) {
                    task.setStatus("Delayed");
                }
            }
        }
    }

    /**
     * Επιστρέφει τη λίστα των κατηγοριών.
     *
     * @return Λίστα κατηγοριών.
     */
    public ObservableList<Category> getCategories() {
        return categories;
    }

    /**
     * Προσθέτει μια νέα κατηγορία.
     *
     * @param category Η νέα κατηγορία.
     */
    public void addCategory(Category category) {
        categories.add(category);
    }

    /**
     * Ενημερώνει το όνομα μιας κατηγορίας και αλλάζει αντίστοιχα τις εργασίες που ανήκουν σε αυτή.
     *
     * @param category Η κατηγορία προς ενημέρωση.
     * @param newName  Το νέο όνομα της κατηγορίας.
     */
    public void updateCategory(Category category, String newName) {
        String oldName = category.getName();
        category.setName(newName);
        categories.set(categories.indexOf(category), category);
        tasks.forEach(task -> {
            if (oldName.equals(task.getCategory())) {
                task.setCategory(newName);
                tasks.set(tasks.indexOf(task), task);
            }
        });
    }

    /**
     * Διαγράφει μια κατηγορία και όλες τις σχετικές εργασίες.
     *
     * @param category Η κατηγορία που θα διαγραφεί.
     */
    public void removeCategory(Category category) {
        List<Task> tasksToRemove = tasks.stream().filter(task -> task.getCategory().equals(category.getName())).toList();
        tasksToRemove.forEach(this::removeRemindersForTask);
        tasks.removeAll(tasksToRemove);
        categories.remove(category);
    }

    /**
     * Ταξινομεί τις κατηγορίες αλφαβητικά.
     */
    public void sortCategoriesAlphabetically() {
        categories.sort((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()));
    }

    /**
     * Επιστρέφει τη λίστα των προτεραιοτήτων.
     *
     * @return Λίστα προτεραιοτήτων.
     */
    public ObservableList<Priority> getPriorities() {
        return priorities;
    }

    /**
     * Προσθέτει μια νέα προτεραιότητα.
     *
     * @param priority Η νέα προτεραιότητα.
     */
    public void addPriority(Priority priority) {
        priorities.add(priority);
    }

    /**
     * Ενημερώνει το όνομα μιας προτεραιότητας και αλλάζει αντίστοιχα τις εργασίες που ανήκουν σε αυτή.
     *
     * @param priority Η προτεραιότητα προς ενημέρωση.
     * @param newName  Το νέο όνομα της προτεραιότητας.
     */
    public void updatePriority(Priority priority, String newName) {
        String oldName = priority.getName();
        priority.setName(newName);
        priorities.set(priorities.indexOf(priority), priority);
        tasks.forEach(task -> {
            if (oldName.equals(task.getPriority())) {
                task.setPriority(newName);
                tasks.set(tasks.indexOf(task), task);
            }
        });
    }

    /**
     * Διαγράφει μια προτεραιότητα και αναθέτει στις σχετικές εργασίες την προκαθορισμένη ("Default").
     *
     * @param priority Η προτεραιότητα που θα διαγραφεί.
     */
    public void removePriority(Priority priority) {
        if (!priority.getName().equals("Default")) {
            tasks.forEach(task -> {
                if (task.getPriority().equals(priority.getName())) {
                    task.setPriority("Default");
                    tasks.set(tasks.indexOf(task), task);
                }
            });
            priorities.remove(priority);
        }
    }

    /**
     * Μετακινεί μια προτεραιότητα μία θέση πάνω στη λίστα.
     *
     * @param priority Η προτεραιότητα που θα μετακινηθεί.
     */
    public void movePriorityUp(Priority priority) {
        int index = priorities.indexOf(priority);
        if (index > 0) {
            priorities.remove(index);
            priorities.add(index - 1, priority);
        }
    }

    /**
     * Μετακινεί μια προτεραιότητα μία θέση κάτω στη λίστα.
     *
     * @param priority Η προτεραιότητα που θα μετακινηθεί.
     */
    public void movePriorityDown(Priority priority) {
        int index = priorities.indexOf(priority);
        if (index < priorities.size() - 1) {
            priorities.remove(index);
            priorities.add(index + 1, priority);
        }
    }

    /**
     * Επιστρέφει τη λίστα των υπενθυμίσεων.
     *
     * @return Λίστα υπενθυμίσεων.
     */
    public ObservableList<Reminder> getReminders() {
        return reminders;
    }

    /**
     * Προσθέτει μια νέα υπενθύμιση.
     *
     * @param reminder Η υπενθύμιση που θα προστεθεί.
     */
    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
    }

    /**
     * Ενημερώνει μια υπενθύμιση.
     *
     * @param reminder       Η υπενθύμιση προς ενημέρωση.
     * @param newTaskTitle   Νέος τίτλος εργασίας.
     * @param newReminderDate Νέα ημερομηνία υπενθύμισης.
     * @param newReminderType Νέος τύπος υπενθύμισης.
     */
    public void updateReminder(Reminder reminder, String newTaskTitle, String newReminderDate, String newReminderType) {
        reminder.setTaskTitle(newTaskTitle);
        reminder.setReminderDate(newReminderDate);
        reminder.setReminderType(newReminderType);
        reminders.set(reminders.indexOf(reminder), reminder);
    }

    /**
     * Διαγράφει μια υπενθύμιση.
     *
     * @param reminder Η υπενθύμιση που θα διαγραφεί.
     */
    public void removeReminder(Reminder reminder) {
        reminders.remove(reminder);
    }

    /**
     * Διαγράφει όλες τις υπενθυμίσεις που σχετίζονται με μια εργασία.
     *
     * @param task Η εργασία της οποίας θα διαγραφούν οι υπενθυμίσεις.
     */
    public void removeRemindersForTask(Task task) {
        List<Reminder> remindersToRemove = reminders.stream().filter(reminder -> reminder.getTaskTitle().equals(task.getTitle())).toList();
        remindersToRemove.forEach(this::removeReminder);
    }

    /**
     * Υπολογίζει την ημερομηνία υπενθύμισης με βάση τον τύπο της.
     *
     * @param reminderType  Τύπος υπενθύμισης.
     * @param specificDate  Συγκεκριμένη ημερομηνία (αν υπάρχει).
     * @param taskTitle     Τίτλος εργασίας.
     * @return Η υπολογισμένη ημερομηνία υπενθύμισης ή null αν δεν είναι έγκυρη.
     */
    public String calculateReminderDate(String reminderType, String specificDate, String taskTitle) {
        Task task = tasks.stream().filter(t -> t.getTitle().equals(taskTitle)).findFirst().orElse(null);

        if (task == null || task.getDeadline() == null || task.getDeadline().isEmpty()) {
            return null;
        }

        LocalDate deadline = LocalDate.parse(task.getDeadline(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDate reminderDate;

        switch (reminderType) {
            case "1 Day Before":
                reminderDate = deadline.minusDays(1);
                break;
            case "1 Week Before":
                reminderDate = deadline.minusWeeks(1);
                break;
            case "1 Month Before":
                reminderDate = deadline.minusMonths(1);
                break;
            case "Specific Date":
                reminderDate = LocalDate.parse(specificDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                if (reminderDate.isAfter(deadline)) {
                    return null;
                }
                break;
            default:
                return null;
        }
        if (reminderDate.isBefore(LocalDate.now())) {
            return null;
        }
        return reminderDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    /**
     * Ελέγχει αν μια ημερομηνία έχει έγκυρη μορφή (DD-MM-YYYY).
     *
     * @param date Η ημερομηνία προς έλεγχο.
     * @return true αν η ημερομηνία δεν είναι έγκυρη, αλλιώς false.
     */
    public boolean isInvalidDate(String date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate.parse(date, formatter);
            return false;
        } catch (DateTimeParseException e) {
            return true;
        }
    }

    /**
     * Ελέγχει αν μια εργασία έχει προθεσμία εντός των επόμενων 7 ημερών.
     *
     * @param deadline Η προθεσμία της εργασίας.
     * @return true αν η προθεσμία είναι εντός 7 ημερών, αλλιώς false.
     */
    public boolean isUpcoming(String deadline) {
        if (deadline == null || deadline.isEmpty()) return false;
        LocalDate currentDate = LocalDate.now();
        LocalDate deadlineDate = LocalDate.parse(deadline, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return !deadlineDate.isBefore(currentDate) && deadlineDate.isBefore(currentDate.plusDays(7));
    }

    /**
     * Αποθηκεύει όλα τα δεδομένα (εργασίες, κατηγορίες, προτεραιότητες, υπενθυμίσεις) στα JSON αρχεία.
     */
    public void saveAllData() {
        DataHandler.saveData(TASKS_FILE, tasks);
        DataHandler.saveData(CATEGORIES_FILE, categories);
        DataHandler.saveData(PRIORITIES_FILE, priorities);
        DataHandler.saveData(REMINDERS_FILE, reminders);
    }
}
