package frontEnd;

import client.Client;
import server.models.Course;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TerminalApp {

    private final static String BIENVENUE = "*** Bienvenue au portail d'inscription de cours de l'UDEM *** \n";
    private final static String CHOIX_SESSION = "Veuillez choisir la session pour laquelle vous voulez consulter la liste des cours: \n"
            +
            "1. Automne \n" +
            "2. Hiver\n" +
            "3. Ete\n" +
            "> Choix: ";
    private final static String OPTIONS = "> Choix: " +
            "1. Consulter les cours offerts pour une autre session \n" +
            "2. Inscription a un cours\n";
    private final static String DEMANDE_PRENOM = "Veuillez saisir votre prénom: ";
    private final static String DEMANDE_NOM = "Veuillez saisir votre nom: ";
    private final static String DEMANDE_EMAIL = "Veuillez saisir votre email: ";
    private final static String DEMANDE_MATRICULE = "Veuillez saisir votre matricule: ";
    private final static String DEMANDE_CODE = "Veuillez saisir le code du cours: ";
    private final static String COURS_AUTOMNE = "Les cours offerts pendant la session d'automne sont: \n";
    private final static String COURS_HIVER = "Les cours offerts pendant la session d'hiver sont: \n";
    private final static String COURS_ETE = "Les cours offerts pendant la session d'été sont: \n";
    private final static String COURS_INVALIDE = "erreur: choix invalide, veuillez choisir entre choix existants\n";


    ClientInterface client = null;

    public TerminalApp(ClientInterface client) {
        this.client = client;
    }

    public void run(String[] args) throws IOException {
        // Envoie au server les lignes tapées sur la console.
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        // accueil bienvenue au portal
        System.out.println(BIENVENUE);

        Boolean finished = false;
        while (!finished) {
            // MENU 1
            System.out.println(CHOIX_SESSION);
            String choixSession = keyboard.readLine();
            ArrayList<Course> sessionCourseLoad = null;
            sessionCourseLoad = handleSessionChoice(choixSession, sessionCourseLoad);

            // MENU 2
            System.out.println(OPTIONS);
            String choix_menu2 = keyboard.readLine();

            if (choix_menu2.equals("1")) {
               continue;

            } else if(choix_menu2.equals("2")) {
                System.out.println(DEMANDE_PRENOM);
                String prenom = keyboard.readLine();

                System.out.println(DEMANDE_NOM);
                String nom = keyboard.readLine();

                System.out.println(DEMANDE_EMAIL);
                String email = keyboard.readLine();

                System.out.println(DEMANDE_MATRICULE);
                String matricule = keyboard.readLine();

                System.out.println(DEMANDE_CODE);
                String code = keyboard.readLine();

                Course cours = client.getACourse(sessionCourseLoad, code);
                
                String server_response = client.inscription(
                    nom, prenom, email, matricule, cours);

                System.out.println(server_response);
                finished = true;
            } else {
                System.out.println("Wrong choice!");
            }
        }
    }

    private ArrayList<Course> handleSessionChoice(
            String choixSession,
            ArrayList<Course> sessionCourseLoad) throws IOException {

        switch (choixSession) {
            case "1":
            System.out.println(COURS_AUTOMNE);
            sessionCourseLoad = getCourseLoad("Automne");
            break;
               
            case "2":
            System.out.println(COURS_HIVER);
            sessionCourseLoad = getCourseLoad( "Hiver");
            break;
               
            case "3":
            System.out.println(COURS_ETE);
            sessionCourseLoad = getCourseLoad( "Ete"); 
            break;   

            default:
                System.out.println(COURS_INVALIDE);
        }
        return sessionCourseLoad;
    }

    private ArrayList<Course> getCourseLoad(String session) throws IOException {
        ArrayList<Course> sessionCourseLoad = client.getCoursList(session);
        printCourses(sessionCourseLoad);
        return sessionCourseLoad;
    }

    private void printCourses(ArrayList<Course> list) {
        int indice = 0;
        for (Course course : list) {
            indice++;
            System.out.println(indice + ". " + course.getCode() + "     " + course.getName());
        }
    }

}
