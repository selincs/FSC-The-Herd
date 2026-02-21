package Model;

import java.util.List;

//Rename to Topic? BoardTopic?
/*  TODO: This class is :
    -Searchable
    -Has a community board
    -Can be created by a user if the Topic doesn't exist
    -Has posts in the community board -> Posts have likes and comments. Other things? Post class, member of Community Board?
    -Has members (memberIDs)
    TODO: Is a Topic *DIFFERENT* than a CommunityBoard? What is different? Do we need both classes?
*/
public class Hobby {
    private String hobbyID;
    private String hobbyName;
    private String hobbyDesc;   //hobby description
    private String creatorID;   //The user who created the Topic
    private int memberCount;
    private List<String> memberIDs; //IDs of all participating members

    public Hobby(String hobbyName) {
        this.hobbyName = hobbyName;
        this.memberCount = 0;
    }

    public String getHobbyName() {
        return hobbyName;
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
