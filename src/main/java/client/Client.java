package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Optional;

import frontEnd.ClientInterface;
import server.models.Course;
import server.models.RegistrationForm;


/** classe simulant un client qui se connecte au serveur */
public class Client implements ClientInterface{

    /**adresse du serveur */
    public static final String SERVER_ADRESS = "127.0.0.1";

    /** numéro du port */
    public static final int SERVER_PORT = 1337;

    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    /**session de cours */
    public String session = "Automne"; 


    /**
    * Récupère la liste des cours disponibles pour une session donnée.
    * @param session la session pour laquelle on veut récupérer la liste des cours
    * @return une ArrayList de Course contenant la liste des cours disponibles pour la session donnée
    * @throws IOException si une erreur d'entrée/sortie se produit pendant la communication avec le serveur
    */

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<Course> getCoursList(String session) throws IOException{
        try{
            this.connect();
            out.writeObject("CHARGER " + session);
            ArrayList<Course> coursList = (ArrayList<Course>) in.readObject();


            return coursList;

        } catch (ConnectException connectException){
            System.err.println("erreur: probleme de connexion");
        } catch (IOException ioException){
            System.err.println("erreur: ioexception");
        } catch (ClassNotFoundException classNotFoundException){
            System.err.println("erreur: classNotFoundException");
        }

        return null;
    }

    /**
    Retourne un objet Course correspondant à un code de cours donné, s'il existe dans la liste des cours.
    @param listCourses une liste d'objets Course à parcourir pour trouver le cours correspondant au code.
    @param code le code du cours que l'on recherche.
    @return un objet Course correspondant au code donné, ou null si aucun cours ne correspond.
    */
    @Override
    public Course getACourse(ArrayList<Course> listCourses, String code){

        Optional<Course> course = listCourses.stream()
                .filter(c -> c.getCode().equals(code))
                .findFirst();

        if (course.isPresent()) {
            return course.get();
        } else {
            return null;
        }
    }
    /**
    * Inscrire un étudiant à un cours spécifique.
    *
    * @param nom le nom de l'étudiant à inscrire
    * @param prenom le prénom de l'étudiant à inscrire
    * @param email l'adresse email de l'étudiant à inscrire
    * @param matricule le matricule de l'étudiant à inscrire
    * @param cours le cours auquel l'étudiant doit être inscrit
    * @return une chaîne de caractères indiquant le résultat de l'inscription (par exemple, "Inscription réussie" ou "Cours complet")
    * @throws IOException si une erreur d'entrée/sortie se produit pendant la communication avec le serveur
    */
    @Override
    public String inscription(String nom, String prenom, String email, String matricule,
    Course cours) throws IOException{

        this.connect();
        try {
            //Envoyer commande au server.
            out.writeObject("INSCRIRE");
            RegistrationForm form = new RegistrationForm(nom, prenom, email, matricule, cours);
            out.writeObject(form);

            //On affiche ce que retourne la méthode handleRegistration
            return (String) in.readObject();
            
        } catch (IOException e) {
            System.err.println("Erreur d'entrée/sortie : " + e.getMessage());
        } catch(ClassNotFoundException e){
            System.err.println("Classe non trouvée : " + e.getMessage());
        }

        return null;
    }


    private void connect() throws IOException{
        this.clientSocket = new Socket(SERVER_ADRESS, SERVER_PORT);
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
    }
}
