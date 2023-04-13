package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;


import java.util.List;


public class Server {

    public final static String REGISTER_COMMAND = "INSCRIRE";
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     */
    public void handleLoadCourses(String arg){

        try (BufferedReader reader = new BufferedReader(new FileReader("./data/cours.txt"))) {

            // /!\ le chemin du fichier cours.txt peut causer une erreur 
            //Lecture du fichier cours.txt

            //Création de la liste d'objets qui va contenir tous les cours.
            List<Course> allCourses = new ArrayList<>();

            //Création de la liste qui va contenir les cours de la session demandée
            List<Course> filteredCourses = new ArrayList<>();

            String line;
            while(( line = reader.readLine()) != null ) {

                //On créé un tableau contenant les éléments de chaque ligne
                String [] elements = line.split("\t");

                String nom = elements[0];
                String code = elements[1];
                String session = elements[2];

                //On créé un cours pour chaque ligne puis on l'ajoute à la liste
                //allCourses
                Course course = new Course(nom, code, session);
                allCourses.add(course);
                
            }

            for(Course course : allCourses){
                //On parcours la liste de tous le cours et on filtre selon la session
                if (course.getSession().equals(arg)){
                    filteredCourses.add(course);
                }
            }
            this.objectOutputStream.writeObject(filteredCourses);

        }catch (FileNotFoundException e){
            System.out.println("File not found: " + e.getMessage());

        }
        catch(IOException e){
            System.out.println("Error reading or writing the file: " + e.getMessage());
        }

    }


    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     La méthode gére les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    @SuppressWarnings("unchecked")
    public void handleRegistration() {
        try {

            // Récupération de l'objet RegistrationForm envoyé par le client
            RegistrationForm form = (RegistrationForm) objectInputStream.readObject();

            String sessionCours = form.getCourse().getSession();
            String nomCours = form.getCourse().getName();
            String codeCours = form.getCourse().getCode();

            //On créé une instance du cours
            Course coursDemande = new Course(nomCours, codeCours, sessionCours);

            //On récupére la liste de cours disponible à cette session
            objectOutputStream.writeObject("CHARGER " + sessionCours);
            objectOutputStream.flush();

            List<Course> coursDisponibles = (List<Course>) objectInputStream.readObject();

            //On vérifie si le cours existe à cette session
            if (coursDisponibles.contains(coursDemande)){

                //On récupère la liste des cours inscrit de l'étudiant
                ArrayList<String> listCodeCoursInscrits = extraireCoursDejaInscrits(sessionCours);

                Boolean estDejaInscrit = false;

                //On  vérifie si l'étudiant n'est pas déja inscrit au cours
                for(String code : listCodeCoursInscrits){
                    if(code.equals(codeCours)){
                        estDejaInscrit = true;
                        break;
                    }
                }
                
                if(estDejaInscrit){
                    objectOutputStream.writeObject("erreur: cours déjà inscrit");
                }
                else{
                    //On inscrit le cours à la liste des cours inscrits de l'étudiant
                    inscriptionCours(form);
                }
            }
            else{
                objectOutputStream.writeObject("Cours indisponible à la session demandée");
            }
    
        } catch (IOException e) {
            System.out.println("Erreur lors de l'envoi du message de confirmation : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Erreur lors de la récupération de l'objet RegistrationForm : " + e.getMessage());
        }
    }

    private void inscriptionCours(RegistrationForm form) throws IOException {
        FileWriter inscription = new FileWriter("./data/inscription.txt", true);
        BufferedWriter writer = new BufferedWriter(inscription);
        inscription.write(form.toString());
        objectOutputStream.writeChars("inscription réussie");

        inscription.close();
        writer.close();
    }

    private ArrayList<String> extraireCoursDejaInscrits(String sessionCours) throws FileNotFoundException, IOException {
        try{
            FileReader fileReader = new FileReader("./data/inscription.txt");
            BufferedReader reader = new BufferedReader(fileReader);

            ArrayList<String> listCodeCoursInscrits = new ArrayList<>();
            String coursInscrits = reader.readLine();
            while(coursInscrits != null){

                String [] elements = coursInscrits.split("\t");
                String session = elements[0];
                String code = elements[1];

                //On filtre pour obtenir les cours de la meme session
                //que celle du cours auquel l'étudiant veut s'inscrire
                if(session == sessionCours){
                    listCodeCoursInscrits.add(code);
                }

                //On passe à la ligne suivante
                coursInscrits = reader.readLine();
            }
            reader.close();

            return listCodeCoursInscrits;

        }catch(IOException e){
            System.out.println("Erreur lors de la lecture du fichier inscription.txt : " + e.getMessage());
            return null;
        }
    }
}