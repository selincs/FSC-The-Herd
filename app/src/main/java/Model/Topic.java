package Model;

import java.util.List;

/*  TODO: The Topic class is :
    -Searchable (by String topicName)
    -Has a community board (List of Posts, rest is GUI? Own CommBoard class? TBD)
    -Can be created by a user if the Topic doesn't exist
    -Has posts in the community board -> Posts have likes and comments. Other things? Post class, member of Community Board?
    -Has members (memberIDs, memberCount for displaying these values)
    TODO: Is a Topic *DIFFERENT* than a CommunityBoard? What is different? Do we need both classes?
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
    private String topicID;
    private final String topicName;
    private String topicDesc;   //topic description
    private String creatorID;   //The user who created the Topic
    private int memberCount;
    private List<String> memberIDs; //IDs of all participating members
    //communityBoard - What is a community board? Just a list of Posts?

    public Topic(String topicName) {
        this.topicName = topicName;
        this.memberCount = 0;
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
}
