package Model;

import java.util.UUID;

//Verification, Authentication + Identity class
public class User {
    //unique userID, do we use this or ram ID for software ID needs. Firebase UUID = String, maybe strings over int data type
    private final String userID; //Links Profile-> User, internal primary key. Plus ID usage areas:
    // friendIDs, Post.authorID, comment.authorID, Message.senderID, event.CreatorID,Mentor/Mentee ID rels.
    //to record the user's unique FSC email address required for sign up
    private String fscEmail;  //there are rare instances where fscEmail could change...
    //the user's unique student ramID, which identifier should we use? we probably don't want to expose email addys to randos
    //private final String ramID; //Not sure if we need this. If real app, probably necessary for student identity for college

    //What database are we using? Firebase? Or SQL? How do we store passwords? For now, here
    private String password; //the user's password, do we need to hash this later? how to do security...


    public User(String fscEmail, String password) {
        this.userID = UUID.randomUUID().toString(); //Should userID be FSC Email instead?
        this.fscEmail = fscEmail;
        this.password = password;
    }

    public String getUserID(){
        return this.userID;
    }

    public String getFscEmail(){
        return fscEmail;
    }

    //Should be removed when time comes
    public String getPassword(){
        return password;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", fscEmail='" + fscEmail + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
