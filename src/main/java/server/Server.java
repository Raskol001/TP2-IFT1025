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

/**
La classe Server représente un serveur de socket qui écoute les connexions 
client entrantes et gère les événements.
*/
public class Server {

    /** commande pour s'inscrire */
    public final static String REGISTER_COMMAND = "INSCRIRE"; 
    
    /** Commande pour charger les cours d'une session donnée */
    public final static String LOAD_COMMAND = "CHARGER"; 

    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    /**
    Créé une nouvelle instance de Server et initialise ServerSocket avec le port spécifié.
    @param port Numéro de port sur lequel le serveur va écouter les connexions entrantes des clients.
    @throws IOException Si une erreur I/O arrive lors de l'ouverture de ServerSocket.
    */
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    /**
    Ajoute un gestionnaire d'événements à la liste des gestionnaires d'événements du serveur.
    @param h le gestionnaire d'événements à ajouter
    */
    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    /**
    Alerte tous les gestionnaires d'événements enregistrés avec une commande et un argument spécifiés.
    @param cmd la commande à envoyer aux gestionnaires d'événements
    @param arg l'argument à envoyer aux gestionnaires d'événements
    */
    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    /**
    Écoute les connexions entrantes des clients et les traite en conséquence.
    Tant que le serveur est actif, il accepte les connexions entrantes des clients
    et crée des flux d'entrée et de sortie pour communiquer avec eux. Il écoute ensuite les messages
    entrants et gère les événements en conséquence. 
    Enfin, il ferme la connexion lorsque le client se déconnecte.
    */
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

    /**
    Écoute les messages envoyés par le client sur le flux entrant de données,
     les traite et les transmet aux gestionnaires d'événements enregistrés.
    Si une ligne est lue depuis le flux, 
    elle est traitée en utilisant la méthode "processCommandLine" pour extraire les commandes et les arguments de la ligne, et transmise aux gestionnaires d'événements en utilisant la méthode "alertHandlers".
    @throws IOException si une erreur survient lors de la lecture ou de l'écriture sur le flux d'entrée ou de sortie.
    @throws ClassNotFoundException si la classe d'un objet reçu n'est pas trouvée dans le système de chargement de classes.
    */
    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {

            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            System.out.println("cmd : " + cmd + " ; arg : " + arg);
            this.alertHandlers(cmd, arg);
        }
    }

    /**
    Méthode pour traiter une ligne de commande en séparant la commande et ses arguments.
    @param line La ligne de commande à traiter.
    @return Un objet Pair contenant la commande en tant que clé et ses arguments en tant que valeur.
    */
    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    /**
    Ferme les flux de données et la connexion avec le client.
    @throws IOException si une erreur se produit lors de la fermeture des flux ou de la connexion.
    */
    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    /**
    Gère les événements du serveur en fonction de la commande et de l'argument reçus.
    Si la commande est "register", appelle la méthode handleRegistration().
    Si la commande est "load", appelle la méthode handleLoadCourses() avec l'argument fourni.
    @param cmd la commande reçue
    @param arg l'argument associé à la commande
    */
    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**

    Cette méthode prend en paramètre une chaîne de caractères correspondant à une session de cours, charge la liste des cours
    depuis un fichier texte et renvoie une liste de cours filtrée pour cette session.
    @param arg la session de cours pour laquelle on veut récupérer la liste de cours associée
    */
    public void handleLoadCourses(String arg) {
        try {
            this.objectOutputStream.writeObject(getSessionCourseList(arg));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Course> getSessionCourseList(String arg) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/server/data/cours.txt"))) {

            // Création de la liste qui va contenir les cours de la session demandée
            List<Course> filteredCourses = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {

                // On créé un tableau contenant les éléments de chaque ligne
                String[] elements = line.split("\t");

                String session = elements[2];

                if (session.equals(arg)) {
                    String nom = elements[1];
                    String code = elements[0];
                    Course course = new Course(nom, code, session);
                    filteredCourses.add(course);
                }
            }

            return filteredCourses;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    /**
    Gère l'inscription d'un étudiant à un cours.
    Cette méthode récupère un objet RegistrationForm envoyé par le client et vérifie si le cours demandé est disponible
    à la session indiquée. Si le cours est disponible, on extrait la liste des cours auxquels l'étudiant est déjà inscrit
    et on vérifie si l'étudiant n'est pas déjà inscrit à ce cours. Si l'étudiant n'est pas déjà inscrit, on ajoute le
    cours à la liste des cours inscrits de l'étudiant et on envoie un message de confirmation au client. Si le cours
    n'est pas disponible, on envoie un message d'erreur au client.
    */
    public void handleRegistration() {
        try {
            // Récupération de l'objet RegistrationForm envoyé par le client
            RegistrationForm form = (RegistrationForm) objectInputStream.readObject();

            List<Course> coursDisponibles = getSessionCourseList(form.getCourse().getSession());

            // On vérifie si le cours existe à cette session
            if (coursDisponibles.contains(form.getCourse())) {
                // On récupère la liste des cours inscrit de l'étudiant
                ArrayList<String> listCodeCoursInscrits = extraireCoursDejaInscrits(
                        form.getCourse());

                Boolean estDejaInscrit = false;

                // On vérifie si l'étudiant n'est pas déja inscrit au cours
                for (String code : listCodeCoursInscrits) {
                    if (code.equals(form.getCourse().getCode())) {
                        estDejaInscrit = true;
                        break;
                    }
                }

                if (estDejaInscrit) {
                    inscriptionRefuser();
                } else {
                    // On inscrit le cours à la liste des cours inscrits de l'étudiant
                    inscriptionCours(form);
                }
            } else {
                objectOutputStream.writeObject("Cours indisponible à la session demandée");
            }

        } catch (IOException e) {
            System.out.println("Erreur lors de l'envoi du message de confirmation : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Erreur lors de la récupération de l'objet RegistrationForm : " + e.getMessage());
        }
    }

    private void inscriptionRefuser() throws IOException {
        objectOutputStream.writeObject("erreur: cours déjà inscrit");
    }

    private void inscriptionCours(RegistrationForm form) throws IOException {
        FileWriter inscription = new FileWriter("src/main/java/server/data/inscription.txt", true);
        BufferedWriter writer = new BufferedWriter(inscription);
        inscription.write(form.toString());
        objectOutputStream.writeObject("inscription réussie");

        inscription.close();
        writer.close();
    }

    private ArrayList<String> extraireCoursDejaInscrits(Course cours) throws FileNotFoundException, IOException {
        try {
            FileReader fileReader = new FileReader("src/main/java/server/data/inscription.txt");
            BufferedReader reader = new BufferedReader(fileReader);

            ArrayList<String> listCodeCoursInscrits = new ArrayList<>();
            String coursInscrits = reader.readLine();
            while (coursInscrits != null) {

                String[] elements = coursInscrits.split("\t");
                String session = elements[0];
                String code = elements[1];

                // On filtre pour obtenir les cours de la meme session
                // que celle du cours auquel l'étudiant veut s'inscrire
                if (session.equals(cours.getSession()) && code.equals(cours.getCode())) {
                    listCodeCoursInscrits.add(code);
                }

                // On passe à la ligne suivante
                coursInscrits = reader.readLine();
            }
            reader.close();

            return listCodeCoursInscrits;

        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier inscription.txt : " + e.getMessage());
            return null;
        }
    }
}