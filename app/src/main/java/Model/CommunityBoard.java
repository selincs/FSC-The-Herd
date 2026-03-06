package Model;

import android.os.Build;

import com.example.theherd.FakeUserDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommunityBoard {
    //TODO: Verify CommunityBoard hierarchy flows correctly
    //TODO: Does a CommunityBoard need pictures or videos?
    //A user does not own a CommBoard, it is its own entity where 2 users connect.
    //A topic HAS a community board among other things(guides, events, etc) -> Where does this list of Topics live?
    private List<Post> boardPosts;  //A Topic's community board has : Posts (Posts have likes & comments)
    private String cbName;
    private final String cbID;
    private final String topicId;// The Topic this board belongs to (used for backend lookup / Firestore path)
    //A Post contains its own comment chains in Post class

    //Tracking UID of User who did the action should enable a post/comment/like history (ListView of recent posts, etc.) in Profile class

    private int memberCt; //Could additionally display online members who are a part of the Board?
    private List<String> memberIDs; //UIDs of all Users who have joined this Community Board
    private List<String> moderatorIDs;    //Community moderators (Self managed?)
    private String createdByUID; //The UID of the User who created the Community Board of a Topic. Might be needed for reporting feature?
    //What does being a moderator do? How to implement these powers?

    //Can a User filter posts in a specific CommBoard post? By name or user most likely... Needs own search bar in CB GUI


    public CommunityBoard(String cbName, String createdByUID, String topicId) {
        this.cbName = cbName;
        this.createdByUID = createdByUID;
        this.memberCt = 1;
        this.cbID = UUID.randomUUID().toString();
        this.topicId = topicId;

        this.boardPosts = new ArrayList<>();
        this.memberIDs = new ArrayList<>();
        memberIDs.add(createdByUID);
        this.moderatorIDs = new ArrayList<>();
    }

    //Join a User to a Community Board
    public void joinCommunityBoard(String userID) {
        this.memberIDs.add(userID);
        this.memberCt++;
    }

    public void leaveCommunityBoard(String userID) {
        this.memberIDs.remove(userID);
        this.memberCt--;
    }

    public int showOnlineMembers() {
        int onlineCt = 0;

        for (String memberID : memberIDs) {
            //Need a way to search the User object by ID for this function to work
//            if ( /*this member.getOnlineStatus of members in CommBoard == true */) {
//                onlineCt++;
//            }
        }
        System.out.println("Online status currently only returns 0, logic needed");
        return onlineCt;
    }

    //Returns a Filtered List of the Posts that contain the keyword, case-insensitive (Post Title & Contents)
    //Needs testing, unverified
    public List<Post> searchPosts(String keyword) {
        return boardPosts.stream()
                .filter(post ->
                        post.getPostTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                post.getPostContents().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
    }

    //Merging with Jada CommBoard fncs below
    // TODO: Later return an unmodifiable view for better encapsulation - Jada
    public List<Post> getPosts() { // Returns all posts in this board
        return boardPosts;
    }

    public String getTopicId() { // Returns the topic this board belongs to
        return topicId;
    }

    // Creates and stores a new post in this board
    // Validates input, generates a simulated ID, and returns the created Post
    //Post(String postedByUID, String postTitle, String postContents)
    public Post createPost(String postedByUID, String postTitle, String postContents) {
//TODO: Merging Jada's fncs --We definitely need this but not necessarily this way. Check later, doesn't break anything rn.
        // Validate required fields -- All of below doesn't break but is idk if it covers all Post build logic.
        if (postTitle == null || postTitle.isBlank()) {
            throw new IllegalArgumentException("Title of Post cannot be null or blank");
        }

        if (postContents == null || postContents.isBlank()) {
            throw new IllegalArgumentException("Post contents cannot be null or blank");
        }

        // Create and store the new Post
        //Post(String postedByUID, String postTitle, String postContents)
        Post newPost = new Post(postedByUID, postTitle, postContents);
        boardPosts.add(newPost);

        return newPost;
    }

    //Needs testing, unverified and uses fake user db, not sustainable..
    /*
    public List<Post> filterByAuthorName(String name) {
        return boardPosts.stream()
                .filter(post -> {
                    Profile profile = FakeUserDatabase.getProfileByUserId(post.getPostedByUID());
                    return profile != null &&
                            profile.getFirstName().toLowerCase().contains(name.toLowerCase());
                })
                .collect(Collectors.toList());
    }
    */

}
