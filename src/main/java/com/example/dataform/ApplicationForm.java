package com.example.dataform;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationForm extends Application {

    private static final String FILE_NAME = "records.txt";

    // Fields
    private TextField fullNameField;
    private TextField idField;
    private TextField provinceField;
    private DatePicker dobPicker;
    private ToggleGroup genderGroup;

    private Button findButton;
    private Button deleteButton;

    private List<String[]> records = new ArrayList<>();
    private int currentRecordIndex = -1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Application Form");

        VBox leftPane = createInputFields();


        VBox rightPane = createButtons();


        HBox mainLayout = new HBox(20, leftPane, rightPane);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #2E2E2E; -fx-text-fill: white;");

        HBox.setHgrow(leftPane, Priority.ALWAYS);
        HBox.setHgrow(rightPane, Priority.NEVER);

        Scene scene = new Scene(mainLayout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createInputFields() {
        fullNameField = new TextField();
        idField = new TextField();
        provinceField = new TextField();
        dobPicker = new DatePicker();


        genderGroup = new ToggleGroup();
        RadioButton maleButton = new RadioButton("Male");
        RadioButton femaleButton = new RadioButton("Female");
        maleButton.setToggleGroup(genderGroup);
        femaleButton.setToggleGroup(genderGroup);
        maleButton.setStyle("-fx-text-fill: white;");
        femaleButton.setStyle("-fx-text-fill: white;");
        HBox genderBox = new HBox(10, maleButton, femaleButton);


        fullNameField.setPromptText("Full Name");
        idField.setPromptText("ID");
        provinceField.setPromptText("Home Province");
        fullNameField.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        idField.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        provinceField.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");
        dobPicker.setStyle("-fx-background-color: #333333; -fx-text-fill: white;");

        fullNameField.setMaxWidth(Double.MAX_VALUE);
        idField.setMaxWidth(Double.MAX_VALUE);
        provinceField.setMaxWidth(Double.MAX_VALUE);
        dobPicker.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(15, new Label("Full Name"), fullNameField,
                new Label("ID"), idField,
                new Label("Gender"), genderBox,
                new Label("Home Province"), provinceField,
                new Label("Date of Birth"), dobPicker);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setStyle("-fx-background-color: #1E1E1E; -fx-text-fill: white;");

        return vbox;
    }

    private VBox createButtons() {
        Button newButton = new Button("New");
        deleteButton = new Button("Delete");
        Button restoreButton = new Button("Restore");
        findButton = new Button("Find");
        Button closeButton = new Button("Close");

        String buttonStyle = "-fx-background-color: #444; -fx-text-fill: white;";
        newButton.setStyle(buttonStyle);
        deleteButton.setStyle(buttonStyle);
        restoreButton.setStyle(buttonStyle);
        findButton.setStyle(buttonStyle);
        closeButton.setStyle(buttonStyle);


        newButton.setOnAction(e -> saveRecord());
        closeButton.setOnAction(e -> closeApplication());
        findButton.setOnAction(e -> findRecord());
        deleteButton.setOnAction(e -> deleteRecord());
        restoreButton.setOnAction(e -> restoreRecord());


        deleteButton.setDisable(true);

        VBox vbox = new VBox(15, newButton, deleteButton, restoreButton, findButton, closeButton);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER_RIGHT);
        return vbox;
    }

    private void saveRecord() {
        String fullName = fullNameField.getText();
        String id = idField.getText();
        String province = provinceField.getText();
        String dob = (dobPicker.getValue() != null) ? dobPicker.getValue().toString() : "";
        RadioButton selectedGender = (RadioButton) genderGroup.getSelectedToggle();
        String gender = (selectedGender != null) ? selectedGender.getText() : "";

        if (fullName.isEmpty() || id.isEmpty() || province.isEmpty() || dob.isEmpty() || gender.isEmpty()) {
            showAlert("Validation Error", "All fields must be filled out.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(String.join(",", fullName, id, gender, province, dob));
            writer.newLine();
            showAlert("Success", "Record saved successfully.");
            clearFields();
            loadRecords();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void findRecord() {
        String searchId = idField.getText();
        if (searchId.isEmpty()) {
            showAlert("Validation Error", "Please enter an ID to search.");
            return;
        }

        if (records.isEmpty()) {
            showAlert("No Records", "No records found.");
            return;
        }

        boolean found = false;
        for (int i = 0; i < records.size(); i++) {
            String[] record = records.get(i);
            if (record[1].equals(searchId)) {
                currentRecordIndex = i;
                displayRecord(i);
                found = true;
                break;
            }
        }

        if (!found) {
            showAlert("Record Not Found", "No record found with ID: " + searchId);
        }
    }

    private void displayRecord(int index) {
        String[] record = records.get(index);
        fullNameField.setText(record[0]);
        idField.setText(record[1]);
        provinceField.setText(record[3]);
        dobPicker.setValue(java.time.LocalDate.parse(record[4]));
        selectGender(record[2]);

        deleteButton.setDisable(false);
    }

    private void selectGender(String gender) {
        for (Toggle toggle : genderGroup.getToggles()) {
            RadioButton button = (RadioButton) toggle;
            if (button.getText().equalsIgnoreCase(gender)) {
                button.setSelected(true);
                break;
            }
        }
    }

    private void deleteRecord() {
        if (currentRecordIndex >= 0 && currentRecordIndex < records.size()) {
            records.remove(currentRecordIndex);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
                for (String[] record : records) {
                    writer.write(String.join(",", record));
                    writer.newLine();
                }
                showAlert("Success", "Record deleted successfully.");
                loadRecords();
                clearFields();
                deleteButton.setDisable(true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void restoreRecord() {
        clearFields(); // Clear all fields
        deleteButton.setDisable(true);
    }

    private void loadRecords() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            records.clear();
            while ((line = reader.readLine()) != null) {
                String[] record = line.split(",");
                records.add(record);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void closeApplication() {
        System.exit(0);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        fullNameField.clear();
        idField.clear();
        provinceField.clear();
        dobPicker.setValue(null);
        genderGroup.selectToggle(null);
        deleteButton.setDisable(true);
    }
}
