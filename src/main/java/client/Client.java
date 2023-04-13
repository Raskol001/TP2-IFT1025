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



public class Client implements ClientInterface{
    public static final String SERVER_ADRESS = "127.0.0.1";
    public static final int SERVER_PORT = 1337;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    public String session = "Automne"; 


    public Client() throws IOException{
        this.clientSocket = new Socket(SERVER_ADRESS, SERVER_PORT);
        this.out = new ObjectOutputStream(clientSocket.getOutputStream());
        this.in = new ObjectInputStream(clientSocket.getInputStream());
    }



    @Override
    public String postForm(RegistrationForm form){
        try {
            this.connect();

            //Envoyer commande au server.
            out.writeObject("INSCRIRE");
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

    //une première fonctionnalité qui permet au client de récupérer la liste des
    //cours disponibles pour une session donnée. Le client envoie une requête charger
    //au serveur. Le serveur doit récupérer la liste des cours du fichier cours.txt et
    //l’envoie au client. Le client récupère les cours et les affiche.
    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<Course> getCoursList(String session){
        try{
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