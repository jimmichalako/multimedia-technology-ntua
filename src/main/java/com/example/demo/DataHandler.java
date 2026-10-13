package com.example.demo;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Η κλάση DataHandler παρέχει μεθόδους για την αποθήκευση και ανάκτηση δεδομένων από αρχεία JSON.
 */
public class DataHandler {

    /**
     * Φορτώνει δεδομένα από ένα αρχείο JSON και τα μετατρέπει σε λίστα αντικειμένων.
     *
     * @param filePath Η διαδρομή του αρχείου JSON.
     * @param type     Ο τύπος των δεδομένων που πρέπει να φορτωθούν.
     * @param <T>      Ο τύπος των αντικειμένων στη λίστα.
     * @return Μια λίστα αντικειμένων από το αρχείο JSON.
     */
    public static <T> List<T> loadData(String filePath, Type type) {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.err.println("Error loading data from " + filePath + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Αποθηκεύει μια λίστα αντικειμένων σε ένα αρχείο JSON.
     *
     * @param filePath Η διαδρομή του αρχείου JSON.
     * @param data     Η λίστα των αντικειμένων που θα αποθηκευτούν.
     * @param <T>      Ο τύπος των αντικειμένων στη λίστα.
     */
    public static <T> void saveData(String filePath, List<T> data) {
        Gson gson = new Gson();
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            System.err.println("Error saving data to " + filePath + ": " + e.getMessage());
        }
    }
}
