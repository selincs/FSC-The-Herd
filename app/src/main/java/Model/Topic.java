package Model;

import java.util.List;

/*  TODO: This class is :
    -Searchable
    -Has a community board
    -Can be created by a user if the Topic doesn't exist
    -Has posts in the community board -> Posts have likes and comments. Other things? Post class, member of Community Board?
    -Has members (memberIDs)
    TODO: Is a Topic *DIFFERENT* than a CommunityBoard? What is different? Do we need both classes?
    -Relevant CommBoard topics
    -Events
    -Guides
    -Clubs (Groups?)
    -Videos? -> How to vet videos... (pre approved? maybe videos should be reserved for guides)
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
