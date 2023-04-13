package client;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Optional;

import server.models.Course;
import server.models.RegistrationForm;



public class Client {
    public static final String SERVER_ADRESS = "127.0.0.1";
    public static final int SERVER_PORT = 1337;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    public String session = "Automne"; 


    public Client(){
        try {
            this.clientSocket = new Socket(SERVER_ADRESS, SERVER_PORT);

            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.in = new ObjectInputStream(clientSocket.getInputStream());


        } catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException  e) {
            e.printStackTrace();
        }

        if (out == null ) System.err.println("Out null");
        
    }
    public String postForm(RegistrationForm form){
        try {

            //Envoyer commande au server.
            out.writeObject("INSCRIRE");
            out.writeObject(form);
            out.flush();

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

    public Course getACourse(ArrayList<Course> listCourses, String code){

        String codeToFind = "IFT1025";

        Optional<Course> course = listCourses.stream()
                .filter(c -> c.getCode().equals(codeToFind))
                .findFirst();

        if (course.isPresent()) {
            return course.get();
        } else {
            return null;
        }
    }

    
    public String inscription(String nom, String prenom, String email, String matricule,
    Course cours){
        
        try {
            //Envoyer commande au server.
            out.writeObject("INSCRIRE");
            RegistrationForm form = new RegistrationForm(nom, prenom, email, matricule, cours);
            out.writeObject(form);
            out.flush();

            //On affiche ce que retourne la méthode handleRegistration
            return (String) in.readObject();
            
        } catch (IOException e) {
            System.err.println("Erreur d'entrée/sortie : " + e.getMessage());
        } catch(ClassNotFoundException e){
            System.err.println("Classe non trouvée : " + e.getMessage());
        }

        return null;
    }
}
