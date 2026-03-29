package Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Comparator;

public class Post {
    //TODO: Questions - Do posts ever expire? Do CommBoards expire? Do things time out? Stretch goal maybe. Reports & Deletes? Pics/Vids?
    private final String postedByUID; //UID of User who made the Post
    private final String postID;  //ID of the Post -> Used for post history of a User, building post/comment chains in CommBoard

    private final LocalDateTime postDateTime; //Date + Time of the Post
    private final String postTitle;
    private String postContents;    //Post contents (String) - not final incase we allow "Edits" of posts
    private int likeCt; //Total # of likes
    private Set<String> likedUserIds;     // Stores user IDs who liked the post (prevents duplicates, cant be final)

    private List<Comment> commentChain; // List of comments under this post - Save comment chain by ID?

    //Posts will be organized by date, with more active Posts showing at the top.

    //To create a Post, a User must provide a Post Title & Contents (Like their message), + their User ID is recorded
    public Post(
            String postedByUID,

            String postTitle,
            String postContents

    ) {
        this.postedByUID = postedByUID; //User ID always exists beforehand
        this.postID = UUID.randomUUID().toString(); //Need to generate this randomly & consistently for all Posts/Comments
        this.postTitle = postTitle;
        this.postContents = postContents;

        this.postDateTime = LocalDateTime.now();
        this.commentChain = new ArrayList<>();
        this.likeCt = 0;
    }

    // secind constructor adding for rebuilding a firestore loadded post
    public Post(
            String postedByUID,
            String postID,
            String postTitle,
            String postContents,
            int likeCt
    ) {
        this.postedByUID = postedByUID;
        this.postID = postID;
        this.postTitle = postTitle;
        this.postContents = postContents;
        this.postDateTime = LocalDateTime.now();
        this.commentChain = new ArrayList<>();
        this.likeCt = likeCt;
    }

    public void addComment(Comment comment) {
        //if !isReply? //Need to think about this some more

        if (comment == null) { //Just added when merging Jadas classes, not tested but seems right.. Check comment fields tho
            throw new IllegalArgumentException("Comment cannot be null");  // Prevent invalid comments
        }

        System.out.println("Comment added to a Post in Post class");
        this.commentChain.add(comment);
    }

    //Comparators for Filtering Posts
    //Sort Posts by newest first
    //Needs testing, unverified
    public static Comparator<Post> sortByNewest =
            Comparator.comparing(Post::getPostDateTime).reversed();
    //Sort Posts by oldest first
    public static final Comparator<Post> sortByOldest =
            Comparator.comparing(Post::getPostDateTime);
    //Sort by Most Likes
    public static final Comparator<Post> sortByMostLikes =
            Comparator.comparingInt(Post::getLikeCt).reversed();
    //Sort by Most Comments
    public static final Comparator<Post> sortByMostComments =
            Comparator.comparingInt(Post::commentCount).reversed();

    public String getPostTitle() {
        return postTitle;
    }

    public String getPostedByUID() {
        return postedByUID;
    }

    public String getPostID() {
        return postID;
    }

    public LocalDateTime getPostDateTime() {
        return postDateTime;
    }

    public String getPostContents() {
        return postContents;
    }

    public void setPostContents(String postContents) {
        this.postContents = postContents;
    }

    public int getLikeCt() {
        return likeCt;
    }
    //set likeCt unnecessary

    public boolean like(String userId){ //Jada fnc, added likect incr/decr
        this.likeCt++;
        return likedUserIds.add(userId);        // Adds like (returns false if already liked)
    }

    public boolean unlike(String userId){ //Jada fnc, added likect incr/decr
        this.likeCt--;
        return likedUserIds.remove(userId);     // Removes like (returns false if not present)
    }

    public List<Comment> getCommentChain() {
        return commentChain;
    }

    public int commentCount() {
        return this.commentChain.size();
    }

    public void setCommentChain(List<Comment> commentChain) {
        this.commentChain = commentChain;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postedByUID='" + postedByUID + '\'' +
                ", postID='" + postID + '\'' +
                ", postDateTime=" + postDateTime +
                ", postContents='" + postContents + '\'' +
                ", likeCt=" + likeCt +
                ", numComments=" + commentChain.size() +
                '}';
    }

    //More Jada fncs -- Not verified or tested atm but look good
    public String getContent() {
        return postContents;                         // Returns post content
    }

    public void setContent(String content) {
        this.postContents = content;                 // Allows editing post content
    }

    public Set<String> getLikedUserIds() {
        return likedUserIds;                    // Returns users who liked the post
    }



}
