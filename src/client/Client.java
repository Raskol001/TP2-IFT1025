package client;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.*;

public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket clientSocket;

    public static void main(String[] args) {
        try {

            // Connect to the server
            Socket clientSocket = new Socket("127.0.0.1", 1338);
            
            // Create input and output streams
            //OutPutStreamWriter ?? Le server envoie des objets
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            
            //Envoie au server les lignes tapées sur la console.
            Scanner scanner = new Scanner(System.in);

            //On récupère les commandes entrées par l'utilisateur
            //faire boucler le scanner afin que le client entre ses choix successivement.
            String command = scanner.nextLine();
            scanner.close();

            // Send event command to the server
            //envoyer les commandes au server 
            out.writeObject("CHARGER");
            out.writeObject("INSCRIRE");
            out.flush();
            //in.readObject().
            
            // Receive response from the server
            String response = (String)in.readObject();
            System.out.println("Response from server: " + response);
            
            // Close the socket and streams
            out.close();
            in.close();
            clientSocket.close();

            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
    }
    public void inscription(String codeCours,String nomCours,String session, 
                            String nom, String prenom, String email, String matricule){

        Course course = new Course(codeCours, nomCours, session);
        try {
            out.writeObject("CHARGER " + session);
            out.flush();
            if(in.readObject().equals(course )){
                out.writeObject("INSCRIRE ");
                RegistrationForm form = new RegistrationForm(nom, prenom, email, matricule, course);
                out.writeObject(form);
                out.flush();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

