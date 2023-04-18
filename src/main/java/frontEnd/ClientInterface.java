package frontEnd;

import java.io.IOException;
import java.util.ArrayList;

import server.models.Course;
import server.models.RegistrationForm;

/**

Interface décrivant les méthodes disponibles pour communiquer avec le serveur de gestion des inscriptions.
*/
public interface ClientInterface {

    /**
    * Récupère la liste des cours disponibles pour une session donnée.
    * @param session la session pour laquelle on veut récupérer la liste des cours
    * @return une ArrayList de Course contenant la liste des cours disponibles pour la session donnée
    * @throws IOException si une erreur d'entrée/sortie se produit pendant la communication avec le serveur
    */
    public ArrayList<Course> getCoursList(String session) throws IOException;


    /**
    Retourne un objet Course correspondant à un code de cours donné, s'il existe dans la liste des cours.
    @param listCourses une liste d'objets Course à parcourir pour trouver le cours correspondant au code.
    @param code le code du cours que l'on recherche.
    @return un objet Course correspondant au code donné, ou null si aucun cours ne correspond.
    */
    public Course getACourse(ArrayList<Course> listCourses, String code);


    /**
    * Inscrire un étudiant à un cours spécifique.
    * @param nom le nom de l'étudiant à inscrire
    * @param prenom le prénom de l'étudiant à inscrire
    * @param email l'adresse email de l'étudiant à inscrire
    * @param matricule le matricule de l'étudiant à inscrire
    * @param cours le cours auquel l'étudiant doit être inscrit
    * @return une chaîne de caractères indiquant le résultat de l'inscription (par exemple, "Inscription réussie" ou "Cours complet")
    * @throws IOException si une erreur d'entrée/sortie se produit pendant la communication avec le serveur
    */
    public String inscription(String nom, String prenom, String email, String matricule,
    Course cours) throws IOException;
}
