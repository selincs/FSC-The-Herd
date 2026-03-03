package Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Post {
    //TODO: Questions - Do posts ever expire? Do CommBoards expire? Do things time out? Stretch goal maybe. Reports & Deletes? Pics/Vids?
    private String postedByUID; //UID of User who made the Post
    private String postID;  //ID of the Post -> Used for post history of a User, building post/comment chains in CommBoard

    private final LocalDateTime postDateTime; //Date + Time of the Post
    private String postContents;    //Post contents (String)
    private int likeCt; //Total # of likes
    private List<Comment> commentChain; //Save comment chain by ID?

    public Post(String postedByUID, String postID, String postContents) {
        this.postedByUID = postedByUID;
        this.postID = UUID.randomUUID().toString(); //Need to generate this randomly & consistently for all Posts/Comments
        this.postDateTime = LocalDateTime.now();
        this.postContents = postContents;
        this.commentChain = new ArrayList<>();
        this.likeCt = 0;
    }

    public void addComment(Comment comment) {
        //if !isReply? //Need to think about this some more
        System.out.println("Comment added to a Post");
        this.commentChain.add(comment);
    }

    public String getPostedByUID() {
        return postedByUID;
    }

    public void setPostedByUID(String postedByUID) {
        this.postedByUID = postedByUID;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
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

    public void setLikeCt(int likeCt) {
        this.likeCt = likeCt;
    }

    public List<Comment> getCommentChain() {
        return commentChain;
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
}
