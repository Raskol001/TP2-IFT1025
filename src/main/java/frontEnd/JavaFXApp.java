package frontEnd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import client.Client;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import server.models.Course;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class JavaFXApp extends Application {

    static ClientInterface client;
    static ArrayList<Course> list = new ArrayList<>();

    // définir la regex pour la validation du courriel et du matricule
    private final static String EMAIL_REGEX = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
    private final static String MATRICULE_REGEX = "^\\d{8}$";

    @Override
    public void start(Stage primaryStage) {
        // Left side
        Label courseListLabel = new Label("Liste des cours");
        ListView<String> courseList = new ListView<>();
        ObservableList<String> courses = FXCollections.observableArrayList();
        courseList.setItems(courses);
        courseList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        ComboBox<String> seasonComboBox = new ComboBox<>(FXCollections.observableArrayList("Automne", "Hiver", "Ete"));

        Button showCoursesButton = new Button("charger");

        HBox seasonBox = new HBox(seasonComboBox, showCoursesButton); // create an HBox for the seasonComboBox and
                                                                      // showCoursesButton
        seasonBox.setSpacing(50);

        VBox leftSide = new VBox(courseListLabel, courseList, seasonBox);
        leftSide.setSpacing(10);

        // Right side
        Label registrationFormLabel = new Label("Formulaire d'inscription");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prenom");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField matriculeField = new TextField();
        matriculeField.setPromptText("Matricule");

        Button submitButton = new Button("envoyer");
        BooleanBinding invalidFields = Bindings.createBooleanBinding(() ->
        lastNameField.getText().isEmpty() || 
        firstNameField.getText().isEmpty() || 
        emailField.getText().isEmpty() || 
        matriculeField.getText().isEmpty() ||
        !Pattern.matches(EMAIL_REGEX, emailField.getText()) ||
        !Pattern.matches(MATRICULE_REGEX, matriculeField.getText()), 
            lastNameField.textProperty(),
            firstNameField.textProperty(),
            emailField.textProperty(),
            matriculeField.textProperty()
        );
        submitButton.disableProperty().bind(invalidFields);

        VBox rightSide = new VBox(registrationFormLabel, lastNameField, firstNameField, emailField, matriculeField,
                submitButton);
        rightSide.setSpacing(10);

        // Main layout
        HBox root = new HBox(leftSide, rightSide);
        root.setSpacing(20);
        root.setPadding(new Insets(20));
        // Event handlers
        showCoursesButton.setOnAction(e -> {
            // Your action to get and display courses based on the selected session
            try {
                System.out.println(seasonComboBox.getValue());
                list = client.getCoursList(seasonComboBox.getValue());
                courses.clear();
                for (Course course : list) {
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
            if (item == null) {
                popMessage("Cours non sélectionné");
            }
            String[] parts = item.split("-");
            String code = parts[0].trim();
            Course cour = client.getACourse(list, code);
            try {
                String msg = client.inscription(lastNameField.getText(), firstNameField.getText(), emailField.getText(),
                        matriculeField.getText(), cour);
                popMessage(msg);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
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

    public void popMessage(String msg) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Avertissement");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
