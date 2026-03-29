package Model;


import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

//Social Content class for the User, behaviors etc.
public class Profile {
    private String userID; //No need to store the whole User class here, just userID to find them

    private String firstName;   //Received from GUI Sign Up, used for display purposes
    private String lastName;

    private OnlineStatus onlineStatus;    //online status, for displaying to other users
    private MentorRole role;    //Enum to denote User's optional role in mentorship feature

    private String currentStatusUpdate;     //Status a user posts on their page for real time updates (not online status)
    private List<String> friendIDs; //Connect users by userID
    private List<String> topicIds; //All topics User is interested in
    private List<String> askMeAboutTopicIds; // Selected by User for display on Profile to spark convo/interest b/w users

    private String profilePictureURL;
    private String profileBio;    //User bio may be redundant with askMeAbtTIDs.. keep for now

    //postHistory, likeHistory, commentHistory, recorded by stringID -> If a user does one of these things, save it here to create a viewable history in recency order

    //sharedContext (Shared Topics liked between two users, classes, etc) (Do we ask ppl to input their actual classes?)


    //private List<String> sharedContext; //Needs to be dynamic, not permanently decided here. Don't store. Comparator?
    //Compare this.profile.topicIds with otherProfile.topicIds
    //related community boards? board participation information, like posts/comments

    public Profile(String userID, String firstName, String lastName) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;

        this.onlineStatus = OnlineStatus.OFFLINE; //How does this part work in the grand scheme of things... Account created -> Offline, turns Online on log in
        this.role = MentorRole.NONE; //On default user creation set NONE, updates later if user enrolls in feature

        this.topicIds = new ArrayList<String>();
        this.askMeAboutTopicIds = new ArrayList<String>();
        this.friendIDs = new ArrayList<String>();
    }

    //Can later associate colors with online status (bubble/text color) Online=Green, Invisible=Grey, Do Not Disturb = Red
    public enum OnlineStatus {
        ONLINE, //User is online & visible to those with permission to see this status (Friends only? Everyone? Personal pref?)
        OFFLINE, //User is offline and signed out
        INVISIBLE, //User appears offline, but can still chat & send messages
        DO_NOT_DISTURB //Disable all notifications
    }
    public enum MentorRole {
        NONE, //default user has no mentorship enrollment
        MENTOR, //user is a mentor to another user(s)
        MENTEE //user is a mentee, and has a mentor user
    }

    public MentorRole getRole() {
        return role;
    }

    public void addFriend(String friendID){
        if (friendID != null && !friendIDs.contains(friendID)){
            friendIDs.add(friendID);
        }
    }

    public void removeFriend(String friendID) {
        if (friendID != null && friendIDs.contains(friendID)){
            friendIDs.remove(friendID);
            System.out.println("Friend removed"); //Debug print to make sure this works, removable after
        }
    }

    public void addTopic(String topicID) {
        if (topicID != null && !topicIds.contains(topicID)) {
            topicIds.add(topicID);
        }
    }
    public void removeTopic(String topicID) {
        if (topicID != null && topicIds.contains(topicID)) {
            topicIds.remove(topicID);
            System.out.println("Topic removed"); //Removable after confirmation it works
        }
    }

    public List<String> getSharedTopicIds(Profile otherProfile) {
        List<String> shared = new ArrayList<>();

        if (otherProfile == null) return shared;

        Set<String> otherTopics = new HashSet<>(otherProfile.getTopicIds());

        for (String topicId : this.topicIds) {
            if (otherTopics.contains(topicId)) {
                shared.add(topicId); //This needs to store topic NAME here, not topic ID
                System.out.println("Shared topic found.");
            }
        }

        return shared;
    }

    public void addAskMeAboutTopic(String topicID) {
        if (topicID != null && !askMeAboutTopicIds.contains(topicID)) {
            askMeAboutTopicIds.add(topicID);
        }
    }

    public void removeAskMeAboutTopic(String topicID) {
        if (topicID != null && askMeAboutTopicIds.contains(topicID)) {
            askMeAboutTopicIds.remove(topicID);
        }
    }

    public String getUserID() {
        return userID;
    }

    public List<String> getFriendIDs() {
//        return friendIDs;
        return Collections.unmodifiableList(friendIDs);
    }

    public List<String> getTopicIds() {
//        return topicIds;
        return Collections.unmodifiableList(topicIds);
    }

    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(OnlineStatus onlineStatus) {
        if (onlineStatus != null) {
            this.onlineStatus = onlineStatus;
        }
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        if (profilePictureURL != null && !profilePictureURL.isEmpty()) {
            this.profilePictureURL = profilePictureURL;
        }
    }

    public void setProfileBio(String bio) {
        if (bio != null) {
            this.profileBio = bio;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfileBio() {
        return profileBio;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "firstName='" + firstName + '\'' +
                ", userID='" + userID + '\'' +
                ", lastName='" + lastName + '\'' +
                ", status=" + onlineStatus +
                '}';
    }
}
