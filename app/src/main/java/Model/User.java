package Model;

public class User {

    private int userID;
    private String fscEmail;
    private String password;
    private MentorRole role;

    public enum MentorRole {
        MENTOR,
        MENTEE
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
