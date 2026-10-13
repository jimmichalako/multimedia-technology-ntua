# NTUA ECE Multimedia Technology Course

This repository showcases the architectural design, graphical interface, and object-oriented implementation of MediaLab Assistant, a desktop task management application developed for the Multimedia Technology course at the National Technical University of Athens (NTUA). In this course, I focused on desktop GUI engineering and modular software design using the Model-View-Controller (MVC) pattern, implementing dynamic task lifecycles, automated deadline reminders, multi-attribute filtering, and JSON state persistence using JavaFX and Google Gson.

## Project Modules & System Implementation

### Application Architecture & Data Model (Model Layer)
*Object-oriented domain modeling, state management, and JSON file persistence.*

* **Key Tasks:**
  * **Domain Entities (`Task.java`, `Category.java`, `Priority.java`, `Reminder.java`):** Defined the foundational business objects representing tasks, categories, priorities, and time-based reminders. Maintained strict task lifecycles across five operational states (`Open`, `In Progress`, `Postponed`, `Completed`, `Delayed`).
  * **JSON Persistence Engine (`DataHandler.java`):** Designed an automated serialization and deserialization layer using Google Gson to persist runtime state across structured JSON storage files (`tasks.json`, `categories.json`, `priorities.json`, `reminders.json`) within the application directory.
  * **Graceful Shutdown & Data Safety:** Configured JVM runtime shutdown hooks to guarantee that dynamic modifications across all collections are automatically flushed to disk upon application exit.
* **Tech Stack:** Java (JDK 23), Google Gson, JSON Serialization.

### Graphical User Interface & User Interaction (View Layer)
*Development of a responsive desktop interface using JavaFX controls, real-time dashboard analytics, and input validation.*

* **Key Tasks:**
  * **Main Application Shell (`MainApp.java`):** Built a tabbed navigation interface (`TabPane`) dividing the workflow into dedicated management workspaces for Tasks, Categories, Priorities, and Reminders.
  * **Real-Time Analytics Dashboard:** Implemented a persistent status header dynamically reflecting total task counts, completed assignments, delayed deadlines, and upcoming priorities using observable collection listeners (`ListChangeListener`).
  * **Interactive Tabular Views & Forms:** Structured data table views (`TableView`) paired with dedicated input forms, supporting inline record selection, contextual entry modification, and input clearing.
  * **Contextual Alerts & Notification Dialogs:** Added defensive user feedback mechanisms, alerting users on launch when overdue tasks are detected (`Alert.AlertType.WARNING`) and prompting confirmation on destructive actions.
* **Tech Stack:** JavaFX 17 (Controls, Collections, Layouts), Event-Driven UI.

### Business Logic & Orchestration (Controller Layer)
*Central mediation of system workflows, search filtering, and cross-entity relational constraints.*

* **Key Tasks:**
  * **Central State Orchestration (`TaskManager.java`):** Mediated application state using JavaFX observable collections (`ObservableList`), synchronizing changes across the user interface and the underlying JSON storage.
  * **Automated Overdue Detection:** Implemented date comparison workflows (`LocalDate.now()`) during initialization and updates, automatically transitioning expired active tasks into the `Delayed` state.
  * **Multi-Criteria Search Engine:** Engineered real-time query filtering over `FilteredList<Task>`, enabling simultaneous multi-attribute searches by title substring, category taxonomy, and priority level.
  * **Relational Integrity & Dynamic Reminders:** Enforced cascading constraints—automatically purging orphan reminders when parent tasks are marked as completed or deleted, and reassigning tasks to a default priority tier upon category removal.
* **Tech Stack:** Java 23, JavaFX Observables, Predicate Filtering.

## Technologies Used

* **Languages:** Java (JDK 23)
* **GUI Frameworks:** JavaFX 17 (Controls, FXML)
* **Build & Dependency Management:** Apache Maven
* **Libraries:** Google Gson (JSON Data Binding), JUnit Jupiter 5
* **Tools & Environments:** IntelliJ IDEA, Git
* **Core Concepts:** Model-View-Controller (MVC) Architecture, Object-Oriented Design (OOD), Event-Driven Programming, Desktop GUI Development, Data Persistence, Observable Pattern
