package frontEnd;

import java.io.IOException;
import java.util.ArrayList;

import client.Client;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.models.Course;

public class JavaFXApp extends Application {
    static ClientInterface client; 
    @Override
    public void start(Stage primaryStage) {
        // Left side
        ListView<String> courseList = new ListView<>();
        ObservableList<String> courses = FXCollections.observableArrayList();
        courseList.setItems(courses);
        courseList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ComboBox<String> seasonComboBox = new ComboBox<>(FXCollections.observableArrayList("Automne", "Hiver", "Ete"));

        Button showCoursesButton = new Button("Show Courses");

        VBox leftSide = new VBox(courseList, seasonComboBox, showCoursesButton);
        leftSide.setSpacing(10);

        // Right side
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prenom");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField matriculeField = new TextField();
        matriculeField.setPromptText("Matricule");

        Button submitButton = new Button("Submit");
        submitButton.disableProperty().bind(
                Bindings.isEmpty(lastNameField.textProperty())
                        .or(Bindings.isEmpty(firstNameField.textProperty()))
                        .or(Bindings.isEmpty(emailField.textProperty()))
                        .or(Bindings.isEmpty(matriculeField.textProperty()))
        );

        VBox rightSide = new VBox(lastNameField, firstNameField, emailField, matriculeField, submitButton);
        rightSide.setSpacing(10);

        // Main layout
        HBox root = new HBox(leftSide, rightSide);
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        ArrayList<Course> list = ArrayList<>;
        // Event handlers
        showCoursesButton.setOnAction(e -> {
            // Your action to get and display courses based on the selected session
            try {
                list = client.getCoursList(seasonComboBox.getValue());
                courses.clear();
                for(Course course : list){
                    String nomCours = course.getName();
                    String code = course.getCode();
                    String finalForme = "" + code + " - " + nomCours;
                    courses.add(finalForme);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        submitButton.setOnAction(e -> {
            String item = courseList.getSelectionModel().getSelectedItem();
            String[] parts = item.split("-");
            String code = parts[0].trim();
            Course cour = client.getACourse(list, code);
            client.inscription(lastNameField.getText(), firstNameField.getText(), emailField.getText(), matriculeField.getText(), cour);
        });

        // Scene setup and stage display
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Course Registration");
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        client = new Client();
        launch(args);
    }
}
