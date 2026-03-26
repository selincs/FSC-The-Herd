package Model;

import com.example.theherd.R;

import java.util.List;
import java.util.UUID;

/*  TODO: The Topic class is :
    -Searchable (by String topicName)
    -Has a community board (List of Posts, rest is GUI? Own CommBoard class? TBD)
    -Can be created by a user if the Topic doesn't exist
    -Has posts in the community board -> Posts have likes and comments. Other things? Post class, member of Community Board?
    -Has members (memberIDs, memberCount for displaying these values)

    Searching Chess will display:
    -Relevant Community Board (Member count, active this week, new posts today)
    -Events (Next 0-3?)
    -Guides (If applicable)
    -Clubs (Groups? How do we do clubs, is it it's own thing?)
    -Videos? -> How to vet videos... (pre approved? maybe videos should be reserved for guides)

    People you might know through "Chess"
    -Students with similar interests, same major + in Chess community, same graduation year + Chess community
*/

public class Topic {
    private final String topicID;
    private CommunityBoard communityBoard;
    private final String topicName;
    private String topicDesc;   //topic description
    private String creatorID;   //The user who created the Topic
    private int memberCount;
    private List<String> memberIDs; //IDs of all participating members
    //communityBoard - What is a community board? Just a list of Posts?
    private int imageResId;     // <-- New field for topic image resource

    //To create a Topic, a User must provide : the name & description (Plus their ID is recorded)
    public Topic(String topicName, String creatorID, String topicDesc, int imageResId) {
        this.topicName = topicName;
        this.topicID = UUID.randomUUID().toString(); //Generate a random final ID for the new Topic
        this.topicDesc = topicDesc;
        this.communityBoard = new CommunityBoard(topicName, creatorID, topicID);
        this.memberCount = 0;
        this.imageResId = imageResId;
    }

    //To create a Topic, a User must provide : the name & description (Plus their ID is recorded)
    //Imageless constructor, currently sets to herd logo
    public Topic(String topicName, String creatorID, String topicDesc) {
        this.topicName = topicName;
        this.topicID = UUID.randomUUID().toString(); //Generate a random final ID for the new Topic
        this.topicDesc = topicDesc;
        // this.communityBoard = new CommunityBoard(topicName, creatorID, topicID);
        this.memberCount = 0;
        this.imageResId = R.drawable.marquee_logo;
    }

    // Test party merging constructor, clean these up once Firestore is working if we don't need all these fields, likely we do
    public Topic(String topicID, String topicName, String creatorID, String topicDesc, int imageResId) {
        this.topicID = topicID;
        this.topicName = topicName;
        this.creatorID = creatorID;
        this.topicDesc = topicDesc;
        this.memberCount = 1;   //Creator is the only member
        this.imageResId = imageResId;
    }

    //Constructor to load a Topic from Firestore
    public Topic(String topicID, String topicName, String creatorID, String topicDesc, int imageResId, int memberCount) {
        this.topicID = topicID;
        this.topicName = topicName;
        this.creatorID = creatorID;
        this.topicDesc = topicDesc;
        this.memberCount = memberCount;
        this.imageResId = imageResId;
    }

    public String getTopicName() {
        return topicName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void incrementMembers() {
        memberCount++;
    }

    public void decrementMembers() {
        if (memberCount > 0) {
            memberCount--;
        }
    }

    public String getTopicID() {
        return topicID;
    }

    public String getTopicDesc() {
        return topicDesc;
    }

    public void setTopicDesc(String topicDesc) {
        this.topicDesc = topicDesc;
    }

    public String getCreatorID() {
        return creatorID;
    }

    public void setCreatorID(String creatorID) {
        this.creatorID = creatorID;
    }

    public CommunityBoard getCommunityBoard() {
        return communityBoard;
    }

    public void setCommunityBoard(CommunityBoard communityBoard) {
        this.communityBoard = communityBoard;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public List<String> getMemberIDs() {
        return memberIDs;
    }

    public void setMemberIDs(List<String> memberIDs) {
        this.memberIDs = memberIDs;
    }
    // New getter/setter for image
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
}
