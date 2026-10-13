package com.example.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Η κύρια κλάση της εφαρμογής που διαχειρίζεται το UI και την εκκίνηση του προγράμματος.
 * Χρησιμοποιεί JavaFX για την εμφάνιση και διαχείριση των εργασιών, κατηγοριών, προτεραιοτήτων και υπενθυμίσεων.
 */
public class MainApp extends Application {

    private TaskManager taskManager;

    /**
     * Εκκινεί την εφαρμογή και αρχικοποιεί το κύριο παράθυρο.
     *
     * @param primaryStage Το κύριο στάδιο (παράθυρο) της εφαρμογής.
     */
    @Override
    public void start(Stage primaryStage) {
        System.out.println("Loading data...");
        taskManager = new TaskManager();
        taskManager.updateDelayedTasks();
        System.out.println("Data loaded successfully!");

        primaryStage.setTitle("MediaLab Assistant");

        BorderPane root = new BorderPane();
        root.setTop(createTopPane());
        root.setCenter(createTabs());

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        long delayedTaskCount = taskManager.getTasks().stream().filter(t -> "Delayed".equals(t.getStatus())).count();

        if (delayedTaskCount > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Delayed Tasks Alert");
            alert.setHeaderText("Some tasks are overdue!");
            alert.setContentText("You have " + delayedTaskCount + " delayed task(s). Please check them.");

            alert.showAndWait();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Saving data...");
            taskManager.saveAllData();
            System.out.println("Data saved successfully!");
        }));
    }

    /**
     * Δημιουργεί και επιστρέφει το πάνω μέρος του UI που εμφανίζει συγκεντρωτικά στατιστικά.
     *
     * @return Το πάνω μέρος του UI (HBox) με τις πληροφορίες των εργασιών.
     */
    private HBox createTopPane() {
        HBox topPane = new HBox(10);
        topPane.setStyle("-fx-padding: 10; -fx-background-color: #f0f0f0;");

        Label totalTasks = new Label();
        Label completedTasks = new Label();
        Label delayedTasks = new Label();
        Label upcomingTasks = new Label();

        Runnable updateStats = () -> {
            totalTasks.setText("Total Tasks: " + taskManager.getTasks().size());
            completedTasks.setText("Completed Tasks: " + taskManager.getTasks().stream().filter(t -> "Completed".equals(t.getStatus())).count());
            delayedTasks.setText("Delayed Tasks: " + taskManager.getTasks().stream().filter(t -> "Delayed".equals(t.getStatus())).count());
            upcomingTasks.setText("Upcoming Tasks: " + taskManager.getTasks().stream().filter(t -> !"Completed".equals(t.getStatus()) && !"Delayed".equals(t.getStatus()) && taskManager.isUpcoming(t.getDeadline())).count());
        };
        updateStats.run();

        taskManager.getTasks().addListener((ListChangeListener<Task>) _ -> updateStats.run());
        topPane.getChildren().addAll(totalTasks, completedTasks, delayedTasks, upcomingTasks);
        return topPane;
    }

    /**
     * Δημιουργεί και επιστρέφει την κύρια καρτέλα της εφαρμογής.
     *
     * @return Ένα TabPane με τις διάφορες ενότητες της εφαρμογής.
     */
    private TabPane createTabs() {
        TabPane tabPane = new TabPane();

        tabPane.getTabs().addAll(createTasksTab(), createCategoriesTab(), createPrioritiesTab(), createRemindersTab());
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        return tabPane;
    }

    /**
     * Δημιουργεί και επιστρέφει την καρτέλα διαχείρισης εργασιών.
     *
     * @return Ένα Tab που περιέχει τη λίστα και τις λειτουργίες των εργασιών.
     */
    private Tab createTasksTab() {
        Tab tasksTab = new Tab("Tasks");
        BorderPane tasksPane = new BorderPane();
        TableView<Task> tasksTable = createTasksTable();

        FilteredList<Task> filteredTasks = new FilteredList<>(tasksTable.getItems(), _ -> true);
        tasksTable.setItems(filteredTasks);

        TextField titleFilterField = new TextField();
        titleFilterField.setPromptText("Filter by Title");

        TextField categoryFilterField = new TextField();
        categoryFilterField.setPromptText("Filter by Category");

        TextField priorityFilterField = new TextField();
        priorityFilterField.setPromptText("Filter by Priority");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(_ -> applyFilters(filteredTasks, titleFilterField.getText(), categoryFilterField.getText(), priorityFilterField.getText()));

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(_ -> {
            titleFilterField.clear();
            categoryFilterField.clear();
            priorityFilterField.clear();
            applyFilters(filteredTasks, "", "", "");
        });

        HBox filterBox = new HBox(10, titleFilterField, categoryFilterField, priorityFilterField, searchButton, clearButton);
        filterBox.setStyle("-fx-padding: 5;");

        VBox layout = new VBox(10, filterBox, tasksTable);
        layout.setStyle("-fx-padding: 10;");

        tasksPane.setCenter(layout);
        tasksPane.setBottom(createTaskForm(tasksTable));
        tasksTab.setContent(tasksPane);
        return tasksTab;
    }

    /**
     * Δημιουργεί και επιστρέφει έναν πίνακα με τις διαθέσιμες εργασίες.
     *
     * @return Ένα TableView<Task> που περιέχει τις εργασίες.
     */
    private TableView<Task> createTasksTable() {
        TableView<Task> tasksTable = new TableView<>();
        tasksTable.setPlaceholder(new Label("No tasks available"));

        tasksTable.getColumns().addAll(createTableColumn("Title", "title"), createTableColumn("Description", "description"), createTableColumn("Category", "category"), createTableColumn("Priority", "priority"), createTableColumn("Deadline", "deadline"), createTableColumn("Status", "status"));

        tasksTable.setItems(taskManager.getTasks());

        return tasksTable;
    }

    /**
     * Δημιουργεί μια στήλη για τον πίνακα εργασιών με το αντίστοιχο όνομα και ιδιότητα.
     *
     * @param header   Το όνομα της στήλης που θα εμφανίζεται στον πίνακα.
     * @param property Η ιδιότητα της κλάσης `Task` που θα συνδέεται με τη στήλη.
     * @return Ένα TableColumn<Task, String> που χρησιμοποιείται στον πίνακα εργασιών.
     */
    private TableColumn<Task, String> createTableColumn(String header, String property) {
        TableColumn<Task, String> column = new TableColumn<>(header);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    /**
     * Δημιουργεί μια φόρμα για την προσθήκη, τροποποίηση και διαγραφή εργασιών.
     *
     * @param tasksTable Ο πίνακας που περιέχει τις εργασίες.
     * @return Ένα VBox που περιέχει τη φόρμα διαχείρισης εργασιών.
     */
    private VBox createTaskForm(TableView<Task> tasksTable) {
        VBox formPane = new VBox(10);
        formPane.setStyle("-fx-padding: 10; -fx-background-color: #e8e8e8;");

        TextField titleField = new TextField();
        titleField.setPromptText("Title");

        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        Label categoryLabel = new Label("Category:");
        ChoiceBox<String> categoryField = new ChoiceBox<>(FXCollections.observableArrayList(taskManager.getCategories().stream().map(Category::getName).toList()));
        taskManager.getCategories().addListener((ListChangeListener<Category>) _ -> categoryField.setItems(FXCollections.observableArrayList(taskManager.getCategories().stream().map(Category::getName).toList())));
        HBox categoryBox = new HBox(10, categoryLabel, categoryField);

        Label priorityLabel = new Label("Priority:");
        ChoiceBox<String> priorityField = new ChoiceBox<>(FXCollections.observableArrayList(taskManager.getPriorities().stream().map(Priority::getName).toList()));
        taskManager.getPriorities().addListener((ListChangeListener<Priority>) _ -> priorityField.setItems(FXCollections.observableArrayList(taskManager.getPriorities().stream().map(Priority::getName).toList())));
        HBox priorityBox = new HBox(10, priorityLabel, priorityField);

        TextField deadlineField = new TextField();
        deadlineField.setPromptText("Deadline (DD-MM-YYYY)");

        Label statusLabel = new Label("Status:");
        ChoiceBox<String> statusField = new ChoiceBox<>(FXCollections.observableArrayList("Open", "In Progress", "Postponed", "Completed", "Delayed"));
        HBox statusBox = new HBox(10, statusLabel, statusField);

        Button addButton = new Button("Add Task");
        Button editButton = new Button("Edit Task");
        Button deleteButton = new Button("Delete Task");
        Button cancelButton = new Button("Cancel");

        editButton.setDisable(true);
        deleteButton.setDisable(true);

        tasksTable.setOnMouseClicked(_ -> {
            Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                titleField.setText(selectedTask.getTitle());
                descriptionField.setText(selectedTask.getDescription());
                categoryField.setValue(selectedTask.getCategory());
                priorityField.setValue(selectedTask.getPriority());
                deadlineField.setText(selectedTask.getDeadline());
                statusField.setValue(selectedTask.getStatus());

                addButton.setDisable(true);
                editButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                titleField.clear();
                descriptionField.clear();
                categoryField.setValue(null);
                priorityField.setValue(null);
                deadlineField.clear();
                statusField.setValue(null);

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        addButton.setOnAction(_ -> {
            if (titleField.getText().isEmpty()) {
                showAlert("Title cannot be empty.");
                return;
            }
            if (categoryField.getValue() == null) {
                showAlert("Category must be selected.");
                return;
            }
            if (priorityField.getValue() == null) {
                showAlert("Priority must be selected.");
                return;
            }
            if (deadlineField.getText().isEmpty()) {
                showAlert("Deadline cannot be empty.");
                return;
            }
            if (taskManager.isInvalidDate(deadlineField.getText())) {
                showAlert("Deadline must be in DD-MM-YYYY format.");
                return;
            }

            Task newTask = new Task(titleField.getText(), descriptionField.getText(), categoryField.getValue(), priorityField.getValue(), deadlineField.getText());
            taskManager.addTask(newTask);

            titleField.clear();
            descriptionField.clear();
            categoryField.setValue(null);
            priorityField.setValue(null);
            deadlineField.clear();
            statusField.setValue(null);
        });

        editButton.setOnAction(_ -> {
            Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                if (titleField.getText().isEmpty()) {
                    showAlert("Title cannot be empty.");
                    return;
                }
                if (deadlineField.getText().isEmpty()) {
                    showAlert("Deadline cannot be empty.");
                    return;
                }
                if (taskManager.isInvalidDate(deadlineField.getText())) {
                    showAlert("Deadline must be in DD-MM-YYYY format.");
                    return;
                }
                taskManager.updateTask(selectedTask, titleField.getText(), descriptionField.getText(), categoryField.getValue(), priorityField.getValue(), deadlineField.getText(), statusField.getValue());

                titleField.clear();
                descriptionField.clear();
                categoryField.setValue(null);
                priorityField.setValue(null);
                deadlineField.clear();
                statusField.setValue(null);
                tasksTable.getSelectionModel().clearSelection();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        deleteButton.setOnAction(_ -> {
            Task selectedTask = tasksTable.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                taskManager.removeTask(selectedTask);

                titleField.clear();
                descriptionField.clear();
                categoryField.setValue(null);
                priorityField.setValue(null);
                deadlineField.clear();
                statusField.setValue(null);
                tasksTable.getSelectionModel().clearSelection();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        cancelButton.setOnAction(_ -> {
            titleField.clear();
            descriptionField.clear();
            categoryField.setValue(null);
            priorityField.setValue(null);
            deadlineField.clear();
            statusField.setValue(null);
            tasksTable.getSelectionModel().clearSelection();

            addButton.setDisable(false);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        });

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton, cancelButton);
        formPane.getChildren().addAll(titleField, descriptionField, categoryBox, priorityBox, deadlineField, statusBox, buttonBox);
        return formPane;
    }

    /**
     * Δημιουργεί και επιστρέφει την καρτέλα διαχείρισης κατηγοριών.
     *
     * @return Ένα Tab που περιέχει τη λίστα και τις λειτουργίες των κατηγοριών.
     */
    private Tab createCategoriesTab() {
        Tab categoriesTab = new Tab("Categories");

        BorderPane categoriesPane = new BorderPane();
        TableView<Category> categoriesTable = createCategoriesTable();
        VBox formPane = createCategoryForm(categoriesTable);

        categoriesPane.setCenter(categoriesTable);
        categoriesPane.setBottom(formPane);
        categoriesTab.setContent(categoriesPane);
        return categoriesTab;
    }

    /**
     * Δημιουργεί και επιστρέφει έναν πίνακα με τις διαθέσιμες κατηγορίες.
     *
     * @return Ένα TableView<Category> που περιέχει τις κατηγορίες.
     */
    private TableView<Category> createCategoriesTable() {
        TableView<Category> categoriesTable = new TableView<>();
        categoriesTable.setPlaceholder(new Label("No categories available"));

        TableColumn<Category, String> nameColumn = new TableColumn<>("Category Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        categoriesTable.getColumns().add(nameColumn);
        categoriesTable.getColumns().forEach(column -> column.setSortable(false));

        categoriesTable.setItems(taskManager.getCategories());

        return categoriesTable;
    }

    /**
     * Δημιουργεί μια φόρμα για την προσθήκη, τροποποίηση και διαγραφή κατηγοριών.
     *
     * @param categoriesTable Ο πίνακας που περιέχει τις κατηγορίες.
     * @return Ένα VBox που περιέχει τη φόρμα διαχείρισης κατηγοριών.
     */
    private VBox createCategoryForm(TableView<Category> categoriesTable) {
        VBox formPane = new VBox(10);
        formPane.setStyle("-fx-padding: 10; -fx-background-color: #e8e8e8;");

        TextField nameField = new TextField();
        nameField.setPromptText("Category Name");

        Button addButton = new Button("Add Category");
        Button editButton = new Button("Edit Category");
        Button cancelButton = new Button("Cancel");
        Button deleteButton = new Button("Delete Category");
        Button sortButton = new Button("Sort Alphabetically");

        editButton.setDisable(true);
        deleteButton.setDisable(true);

        categoriesTable.setOnMouseClicked(_ -> {
            Category selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                nameField.setText(selectedCategory.getName());

                addButton.setDisable(true);
                editButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                nameField.clear();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        addButton.setOnAction(_ -> {
            String name = nameField.getText();
            if (name.isEmpty()) {
                showAlert("Category name cannot be empty.");
                return;
            }
            if (categoriesTable.getItems().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name))) {
                showAlert("Category name already exists.");
                return;
            }
            taskManager.addCategory(new Category(name));
            nameField.clear();
        });

        editButton.setOnAction(_ -> {
            Category selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                String newName = nameField.getText();
                if (newName.isEmpty()) {
                    showAlert("Category name cannot be empty.");
                    return;
                }
                if (categoriesTable.getItems().stream().anyMatch(c -> c.getName().equalsIgnoreCase(newName))) {
                    showAlert("Category name already exists.");
                    return;
                }
                taskManager.updateCategory(selectedCategory, newName);

                nameField.clear();
                categoriesTable.getSelectionModel().clearSelection();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        deleteButton.setOnAction(_ -> {
            Category selectedCategory = categoriesTable.getSelectionModel().getSelectedItem();
            if (selectedCategory != null) {
                taskManager.removeCategory(selectedCategory);

                nameField.clear();
                categoriesTable.getSelectionModel().clearSelection();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        cancelButton.setOnAction(_ -> {
            nameField.clear();
            categoriesTable.getSelectionModel().clearSelection();
            addButton.setDisable(false);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        });

        sortButton.setOnAction(_ -> taskManager.sortCategoriesAlphabetically());

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton, cancelButton, sortButton);
        formPane.getChildren().addAll(nameField, buttonBox);
        return formPane;
    }

    /**
     * Δημιουργεί και επιστρέφει την καρτέλα διαχείρισης επιπέδων προτεραιότητας.
     *
     * @return Ένα Tab που περιέχει τη λίστα και τις λειτουργίες των επιπέδων προτεραιότητας.
     */
    private Tab createPrioritiesTab() {
        Tab prioritiesTab = new Tab("Priorities");

        BorderPane prioritiesPane = new BorderPane();
        TableView<Priority> prioritiesTable = createPrioritiesTable();
        VBox formPane = createPriorityForm(prioritiesTable);

        prioritiesPane.setCenter(prioritiesTable);
        prioritiesPane.setBottom(formPane);
        prioritiesTab.setContent(prioritiesPane);
        return prioritiesTab;
    }

    /**
     * Δημιουργεί και επιστρέφει έναν πίνακα με τις διαθέσιμες προτεραιότητες.
     *
     * @return Ένα TableView<Priority> που περιέχει τις προτεραιότητες.
     */
    private TableView<Priority> createPrioritiesTable() {
        TableView<Priority> prioritiesTable = new TableView<>();
        prioritiesTable.setPlaceholder(new Label("No priorities available"));

        TableColumn<Priority, String> nameColumn = new TableColumn<>("Priority Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        prioritiesTable.getColumns().add(nameColumn);
        prioritiesTable.getColumns().forEach(column -> column.setSortable(false));

        prioritiesTable.setItems(taskManager.getPriorities());

        return prioritiesTable;
    }

    /**
     * Δημιουργεί μια φόρμα για την προσθήκη, τροποποίηση και διαγραφή επιπέδων προτεραιότητας.
     *
     * @param prioritiesTable Ο πίνακας που περιέχει τις προτεραιότητες.
     * @return Ένα VBox που περιέχει τη φόρμα διαχείρισης προτεραιοτήτων.
     */
    private VBox createPriorityForm(TableView<Priority> prioritiesTable) {
        VBox formPane = new VBox(10);
        formPane.setStyle("-fx-padding: 10; -fx-background-color: #e8e8e8;");

        TextField nameField = new TextField();
        nameField.setPromptText("Priority Name");

        Button addButton = new Button("Add Priority");
        Button editButton = new Button("Edit Priority");
        Button deleteButton = new Button("Delete Priority");
        Button cancelButton = new Button("Cancel");
        Button moveUpButton = new Button("Move Up");
        Button moveDownButton = new Button("Move Down");

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);

        prioritiesTable.setOnMouseClicked(_ -> {
            Priority selectedPriority = prioritiesTable.getSelectionModel().getSelectedItem();
            if (selectedPriority != null && !selectedPriority.getName().equals("Default")) {
                nameField.setText(selectedPriority.getName());

                addButton.setDisable(true);
                editButton.setDisable(false);
                deleteButton.setDisable(false);
                moveUpButton.setDisable(false);
                moveDownButton.setDisable(false);
            } else {
                nameField.clear();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                moveUpButton.setDisable(true);
                moveDownButton.setDisable(true);
            }
        });

        addButton.setOnAction(_ -> {
            String name = nameField.getText();
            if (name.isEmpty()) {
                showAlert("Priority name cannot be empty.");
                return;
            }
            if (prioritiesTable.getItems().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))) {
                showAlert("Priority name already exists.");
                return;
            }
            taskManager.addPriority(new Priority(name));

            nameField.clear();
        });

        editButton.setOnAction(_ -> {
            Priority selectedPriority = prioritiesTable.getSelectionModel().getSelectedItem();
            if (selectedPriority != null) {
                String name = nameField.getText();
                if (name.isEmpty()) {
                    showAlert("Priority name cannot be empty.");
                    return;
                }
                if (prioritiesTable.getItems().stream().anyMatch(p -> p.getName().equalsIgnoreCase(name))) {
                    showAlert("Priority name already exists.");
                    return;
                }
                taskManager.updatePriority(selectedPriority, name);

                nameField.clear();
                prioritiesTable.getSelectionModel().clearSelection();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                moveUpButton.setDisable(true);
                moveDownButton.setDisable(true);
            }
        });

        deleteButton.setOnAction(_ -> {
            Priority selectedPriority = prioritiesTable.getSelectionModel().getSelectedItem();
            if (selectedPriority != null) {
                taskManager.removePriority(selectedPriority);

                nameField.clear();
                prioritiesTable.getSelectionModel().clearSelection();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                moveUpButton.setDisable(true);
                moveDownButton.setDisable(true);
            }
        });

        cancelButton.setOnAction(_ -> {
            nameField.clear();
            prioritiesTable.getSelectionModel().clearSelection();
            addButton.setDisable(false);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            moveUpButton.setDisable(true);
            moveDownButton.setDisable(true);
        });

        moveUpButton.setOnAction(_ -> {
            int selectedIndex = prioritiesTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex > 0) { // Ensure the item is not already at the top
                Priority selectedPriority = prioritiesTable.getItems().get(selectedIndex);
                taskManager.movePriorityUp(selectedPriority);
                prioritiesTable.getSelectionModel().select(selectedIndex - 1);
            }
        });

        moveDownButton.setOnAction(_ -> {
            int selectedIndex = prioritiesTable.getSelectionModel().getSelectedIndex();
            if (selectedIndex < prioritiesTable.getItems().size() - 1) { // Ensure the item is not already at the bottom
                Priority selectedPriority = prioritiesTable.getItems().get(selectedIndex);
                taskManager.movePriorityDown(selectedPriority);
                prioritiesTable.getSelectionModel().select(selectedIndex + 1);
            }
        });

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton, cancelButton, moveUpButton, moveDownButton);
        formPane.getChildren().addAll(nameField, buttonBox);
        return formPane;
    }

    /**
     * Δημιουργεί και επιστρέφει την καρτέλα διαχείρισης υπενθυμίσεων.
     *
     * @return Ένα Tab που περιέχει τη λίστα και τις λειτουργίες των υπενθυμίσεων.
     */
    private Tab createRemindersTab() {
        Tab remindersTab = new Tab("Reminders");

        BorderPane remindersPane = new BorderPane();
        TableView<Reminder> remindersTable = createRemindersTable();
        VBox formPane = createReminderForm(remindersTable);

        remindersPane.setCenter(remindersTable);
        remindersPane.setBottom(formPane);
        remindersTab.setContent(remindersPane);
        return remindersTab;
    }

    /**
     * Δημιουργεί και επιστρέφει έναν πίνακα με τις διαθέσιμες υπενθυμίσεις.
     *
     * @return Ένα TableView<Reminder> που περιέχει τις υπενθυμίσεις.
     */
    private TableView<Reminder> createRemindersTable() {
        TableView<Reminder> remindersTable = new TableView<>();
        remindersTable.setPlaceholder(new Label("No reminders available"));

        TableColumn<Reminder, String> taskTitleColumn = new TableColumn<>("Task Title");
        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));

        TableColumn<Reminder, String> reminderDateColumn = new TableColumn<>("Reminder Date");
        reminderDateColumn.setCellValueFactory(new PropertyValueFactory<>("reminderDate"));

        remindersTable.getColumns().addAll(taskTitleColumn, reminderDateColumn);
        remindersTable.getColumns().forEach(column -> column.setSortable(false));

        remindersTable.setItems(taskManager.getReminders());

        return remindersTable;
    }

    /**
     * Δημιουργεί μια φόρμα για την προσθήκη, τροποποίηση και διαγραφή υπενθυμίσεων.
     *
     * @param remindersTable Ο πίνακας που περιέχει τις υπενθυμίσεις.
     * @return Ένα VBox που περιέχει τη φόρμα διαχείρισης υπενθυμίσεων.
     */
    private VBox createReminderForm(TableView<Reminder> remindersTable) {
        VBox formPane = new VBox(10);
        formPane.setStyle("-fx-padding: 10; -fx-background-color: #e8e8e8;");

        Label selectLabel = new Label("Select Task:");
        ChoiceBox<String> taskField = new ChoiceBox<>(FXCollections.observableArrayList(taskManager.getTasks().stream().filter(task -> !"Completed".equals(task.getStatus())).map(Task::getTitle).toList()));
        taskManager.getTasks().addListener((ListChangeListener<Task>) _ -> taskField.setItems(FXCollections.observableArrayList(taskManager.getTasks().stream().filter(task -> !"Completed".equals(task.getStatus())).map(Task::getTitle).toList())));
        HBox selectBox = new HBox(10, selectLabel, taskField);

        Label reminderTypeLabel = new Label("Reminder Type:");
        ChoiceBox<String> reminderTypeField = new ChoiceBox<>(FXCollections.observableArrayList("1 Day Before", "1 Week Before", "1 Month Before", "Specific Date"));
        HBox reminderTypeBox = new HBox(10, reminderTypeLabel, reminderTypeField);

        TextField specificDateField = new TextField();
        specificDateField.setPromptText("Specific Date (DD-MM-YYYY)");
        specificDateField.setDisable(true);

        reminderTypeField.setOnAction(_ -> {
            if ("Specific Date".equals(reminderTypeField.getValue())) {
                specificDateField.setDisable(false);
            } else {
                specificDateField.setDisable(true);
                specificDateField.clear();
            }
        });

        Button addButton = new Button("Add Reminder");
        Button editButton = new Button("Edit Reminder");
        Button deleteButton = new Button("Delete Reminder");
        Button cancelButton = new Button("Cancel");

        editButton.setDisable(true);
        deleteButton.setDisable(true);

        remindersTable.setOnMouseClicked(_ -> {
            Reminder selectedReminder = remindersTable.getSelectionModel().getSelectedItem();
            if (selectedReminder != null) {
                taskField.setValue(selectedReminder.getTaskTitle());
                if ("Specific Date".equals(selectedReminder.getReminderType())) {
                    reminderTypeField.setValue(selectedReminder.getReminderType());
                    specificDateField.setText(selectedReminder.getReminderDate());
                } else {
                    reminderTypeField.setValue(selectedReminder.getReminderType());
                }
                addButton.setDisable(true);
                editButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                taskField.setValue(null);
                reminderTypeField.setValue(null);
                specificDateField.clear();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        addButton.setOnAction(_ -> {
            String taskTitle = taskField.getValue();
            String reminderType = reminderTypeField.getValue();
            String specificDate = specificDateField.getText();

            if (taskTitle == null || reminderType == null) {
                showAlert("Task and reminder type must be selected.");
                return;
            }
            if ("Specific Date".equals(reminderType)) {
                if (specificDate.isEmpty()) {
                    showAlert("Specific date cannot be empty.");
                    return;
                }
                if (taskManager.isInvalidDate(specificDate)) {
                    showAlert("Invalid specific date format. Use DD-MM-YYYY.");
                    return;
                }
            }

            String reminderDate = taskManager.calculateReminderDate(reminderType, specificDate, taskTitle);
            if (reminderDate == null) {
                showAlert("Reminder date is invalid based on the task's deadline.");
                return;
            }
            if (taskManager.getReminders().stream().anyMatch(r -> r.getTaskTitle().equals(taskTitle) && r.getReminderDate().equals(reminderDate))) {
                showAlert("A reminder already exists for this task on the selected date.");
                return;
            }

            taskManager.addReminder(new Reminder(taskTitle, reminderDate, reminderType));

            taskField.setValue(null);
            reminderTypeField.setValue(null);
            specificDateField.clear();
        });

        editButton.setOnAction(_ -> {
            Reminder selectedReminder = remindersTable.getSelectionModel().getSelectedItem();
            if (selectedReminder != null) {
                String taskTitle = taskField.getValue();
                String reminderType = reminderTypeField.getValue();
                String specificDate = specificDateField.getText();

                if ("Specific Date".equals(reminderType)) {
                    if (specificDate.isEmpty()) {
                        showAlert("Specific date cannot be empty.");
                        return;
                    }
                    if (taskManager.isInvalidDate(specificDate)) {
                        showAlert("Invalid specific date format. Use DD-MM-YYYY.");
                        return;
                    }
                }
                String newReminderDate = taskManager.calculateReminderDate(reminderType, specificDate, taskTitle);
                if (newReminderDate == null) {
                    showAlert("Reminder date is invalid based on the task's deadline.");
                    return;
                }
                if (taskManager.getReminders().stream().anyMatch(r -> r.getTaskTitle().equals(taskTitle) && r.getReminderDate().equals(newReminderDate))) {
                    showAlert("A reminder already exists for this task on the selected date.");
                    return;
                }

                taskManager.updateReminder(selectedReminder, taskTitle, newReminderDate, reminderType);

                taskField.setValue(null);
                reminderTypeField.setValue(null);
                specificDateField.clear();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        deleteButton.setOnAction(_ -> {
            Reminder selectedReminder = remindersTable.getSelectionModel().getSelectedItem();
            if (selectedReminder != null) {
                taskManager.removeReminder(selectedReminder);

                taskField.setValue(null);
                reminderTypeField.setValue(null);
                specificDateField.clear();

                addButton.setDisable(false);
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        cancelButton.setOnAction(_ -> {
            taskField.setValue(null);
            reminderTypeField.setValue(null);
            specificDateField.clear();
            remindersTable.getSelectionModel().clearSelection();
            addButton.setDisable(false);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        });

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton, cancelButton);
        formPane.getChildren().addAll(selectBox, reminderTypeBox, specificDateField, buttonBox);
        return formPane;
    }

    /**
     * Προβάλλει ένα μήνυμα λάθους μέσω ενός pop-up παραθύρου.
     *
     * @param message Το μήνυμα λάθους που θα εμφανιστεί.
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Φιλτράρει τις εργασίες με βάση τον τίτλο, την κατηγορία και την προτεραιότητα.
     *
     * @param filteredTasks  Η λίστα των εργασιών που θα φιλτραριστεί.
     * @param titleFilter    Το φίλτρο με βάση τον τίτλο.
     * @param categoryFilter Το φίλτρο με βάση την κατηγορία.
     * @param priorityFilter Το φίλτρο με βάση την προτεραιότητα.
     */
    private void applyFilters(FilteredList<Task> filteredTasks, String titleFilter, String categoryFilter, String priorityFilter) {
        filteredTasks.setPredicate(task -> {

            boolean matchesTitle = (titleFilter == null || titleFilter.isEmpty()) || task.getTitle().toLowerCase().contains(titleFilter.toLowerCase());

            boolean matchesCategory = (categoryFilter == null || categoryFilter.isEmpty()) || (task.getCategory() != null && task.getCategory().equalsIgnoreCase(categoryFilter));

            boolean matchesPriority = (priorityFilter == null || priorityFilter.isEmpty()) || (task.getPriority() != null && task.getPriority().equalsIgnoreCase(priorityFilter));

            return matchesTitle && matchesCategory && matchesPriority;
        });
    }

    /**
     * Η κύρια μέθοδος που εκκινεί την εφαρμογή.
     *
     * @param args Τα ορίσματα γραμμής εντολών.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
