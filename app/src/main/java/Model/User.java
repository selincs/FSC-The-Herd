package Model;

//Verification, Authentication + Identity class
public class User {
    //unique userID, do we use this or ram ID for software ID needs
    private int userID;
    //to record the user's unique FSC email address required for sign up
    private String fscEmail;
    //the user's unique student ramID, which identifier should we use? we probably don't want to expose email addys to randos
    private String ramID;
    //the user's password, do we need to hash this later? how to do security...
    //What database are we using? Firebase? Or SQL? How do we store passwords?
    private String password;
    //Enum to denote User's optional role in mentorship feature -> Move this to Profile I think
    private MentorRole role;

    public enum MentorRole {
        NONE, //default user has no mentorship enrollment
        MENTOR, //user is a mentor to another user(s)
        MENTEE //user is a mentee, and has a mentor user
    }


    public User(int userID, String fscEmail, String password, MentorRole role) {
        this.userID = userID;
        this.fscEmail = fscEmail;
        this.password = password;
        this.role = role;
    }

    public int getUserID(){
        return userID;
    }

    public String getFscEmail(){
        return fscEmail;
    }

    public String getPassword(){
        return password;
    }

    public MentorRole getRole() {
        return role;
    }
}
