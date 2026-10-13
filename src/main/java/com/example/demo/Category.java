package com.example.demo;

/**
 * Η κλάση Category αναπαριστά μια κατηγορία εργασιών.
 */
public class Category {
    private String name;

    /**
     * Δημιουργεί μια νέα κατηγορία.
     *
     * @param name Το όνομα της κατηγορίας.
     */
    public Category(String name) {
        this.name = name;
    }

    /**
     * Επιστρέφει το όνομα της κατηγορίας.
     *
     * @return Το όνομα της κατηγορίας.
     */
    public String getName() {
        return name;
    }

    /**
     * Ορίζει νέο όνομα για την κατηγορία.
     *
     * @param name Το νέο όνομα της κατηγορίας.
     */
    public void setName(String name) {
        this.name = name;
    }
}
