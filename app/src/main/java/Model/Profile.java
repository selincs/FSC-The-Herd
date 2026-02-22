package Model;


import java.util.List;
import java.util.ArrayList;

//Social Content class for the User, behaviors etc.
public class Profile {
    //private User user;
    private String userID; //No need to store the whole User class here, just userID to find them
    private List<Topic> topics; //We don't need both Topic + Interest. Let's combine into 1 + rename -> Topic?

    //private List<Integer> friends;
    private List<String> friendIDs;
    private onlineStatus status;    //online status
    //profile picture, how do we store an image? revisit after deciding
    private String profilePictureURL;
    private String currentStatusUpdate;


    private String profileBio;    //User bio -- Ask me about XYZ -> Spark convo + interest b/w ppl
    //askMeAbout (Topics go Here) -> List of their selected topics sent to GUI
    private List<Topic> askMeAbout; //Is this just a string of topicNames?
    //sharedContext (Shared Topics liked between two users, classes, etc) (Do we ask ppl to input their actual classes?)
    private List<String> sharedContext; //topic name
    //related community boards? board participation information, like posts/comments

    public Profile(User user ) {
        //this.user = user;
        this.userID = user.getUserID();

        topics = new ArrayList<Topic>();

        this.status = status; //How does this part work in the grand scheme of things...
//        friends = new ArrayList<Integer>();
        this.friendIDs = new ArrayList<String>();

        //profilePictureURL is empty at start, do we declare it as null?
    }

    //Can later associate colors with online status (maybe in text color) Online=Green, Offline=Grey, Away=Yellow
    //Invisible=Grey, Do Not Disturb = Red
    public enum onlineStatus {
        ONLINE, //User is online & visible to those with permission to see this status (Friends only? Everyone? Personal pref?)
        OFFLINE, //User is offline and signed out
        AWAY, //User is logged in but inactive (AFK) - Mobile app might have no use for this
        INVISIBLE, //User appears offline, but can still chat & send messages
        DO_NOT_DISTURB //Disable all notifications
    }

    public void addTopic(Topic addedTopic){
        if ((addedTopic != null ) && !topics.contains(addedTopic)){
            topics.add(addedTopic);
        }
    }



//    public void addFriends(int friend){
//        if ( !friends.contains(friend)){
//            friends.add(friend);
//        }
//    }
    public void addFriends(String friendID){
        if ( !friendIDs.contains(friendID)){
            friendIDs.add(friendID);
        }
    }

//    public User getUser() { return user; }


    public String getUserID() {
        return userID;
    }

    public onlineStatus getStatus() {
        return status;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public List<String> getFriendIDs() {
        return friendIDs;
    }

    public List<Topic> getTopics() {
        return topics;
    }



//    public List<Integer> getFriends() {    return friends;    }
}
