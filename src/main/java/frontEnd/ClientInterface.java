package frontEnd;

import java.io.IOException;
import java.util.ArrayList;

import server.models.Course;
import server.models.RegistrationForm;

public interface ClientInterface {
    public String postForm(RegistrationForm form) throws IOException;
    public ArrayList<Course> getCoursList(String session) throws IOException;
    public Course getACourse(ArrayList<Course> listCourses, String code);
    public String inscription(String nom, String prenom, String email, String matricule,
    Course cours) throws IOException;
}
