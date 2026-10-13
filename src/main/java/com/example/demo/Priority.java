package com.example.demo;

/**
 * Η κλάση Priority αναπαριστά ένα επίπεδο προτεραιότητας για μια εργασία.
 */
public class Priority {
    private String name;

    /**
     * Δημιουργεί ένα νέο επίπεδο προτεραιότητας.
     *
     * @param name Το όνομα του επιπέδου προτεραιότητας.
     */
    public Priority(String name) {
        this.name = name;
    }

    /**
     * Επιστρέφει το όνομα του επιπέδου προτεραιότητας.
     *
     * @return Το όνομα του επιπέδου προτεραιότητας.
     */
    public String getName() {
        return name;
    }

    /**
     * Ορίζει νέο όνομα για το επίπεδο προτεραιότητας.
     *
     * @param name Το νέο όνομα του επιπέδου προτεραιότητας.
     */
    public void setName(String name) {
        this.name = name;
    }
}
