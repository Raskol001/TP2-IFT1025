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

    public static void main(String[] args) throws IOException {

        Client client = new Client();

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

            switch (choixSession) {
                case "1":
                sessionCourseLoad = getCLoad(client, COURS_AUTOMNE, "Automne");
                   
                case "2":
                sessionCourseLoad = getCLoad(client, COURS_HIVER, "Hiver");
                   
                case "3":
                sessionCourseLoad = getCLoad(client, COURS_ETE, "Ete");     
                default:
                    System.out.println(COURS_INVALIDE);
            }

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

                System.out.println( client.inscription(nom, prenom, email, matricule, cours) ) ;
                finished = true;
            } else {
                System.out.println("Wring choice!");
            }
        }
    }

    private static ArrayList<Course> getCLoad(Client client, String MSG, String session) {
        ArrayList<Course> sessionCourseLoad;
        System.out.println(MSG);
        sessionCourseLoad = client.getCoursList(session);
        printListCourses(sessionCourseLoad);
        return sessionCourseLoad;
    }

    public static ArrayList<Course> getCourseLoad(String choix, Client client) {

        ArrayList<Course> sessionCourseLoad;

        switch (choix) {
            case "1":

                System.out.println(COURS_AUTOMNE);
                sessionCourseLoad = client.getCoursList("Automne");
                printListCourses(sessionCourseLoad);
                return sessionCourseLoad;
            case "2":

                System.out.println(COURS_HIVER);
                sessionCourseLoad = client.getCoursList("Hiver");
                printListCourses(sessionCourseLoad);
                return sessionCourseLoad;
            case "3":

                System.out.println(COURS_ETE);
                sessionCourseLoad = client.getCoursList("Ete");
                printListCourses(sessionCourseLoad);
                return sessionCourseLoad;
            default:

                System.out.println("erreur: choix invalide, veuillez choisir entre choix existants");
                return null;
        }
    }

    private static void printListCourses(ArrayList<Course> list) {
        int indice = 0;
        for (Course course : list) {
            indice++;
            System.out.println(indice + ". " + course.getCode() + "     " + course.getName());
        }
    }

}
