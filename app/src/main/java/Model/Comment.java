package Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Comment {
    //TODO: Ensure comments are not double-posted between commentChains of Post class + replyChain of Comment class (Hopefully using isReply boolean)
    //TODO: Reporting / Deleting comments? Pictures or Videos?
    //TODO: Does a comment need to track its parent Post/Comment?
    private String commentedByUID;  //UID of User who made the comment
    private String commentID;  //id of comment -> Used for comment history of a User, building post/comments in CB
    private String parentCommentID; //The parent of this comment, for implementing reply chains
    private final LocalDateTime createdAt;  //Date & Time of comment
    private String commContents;    //Contents of the comment
    private int likeCt; //Total # of likes

    // This needs finesse if it's going to happen, or else it will double post from Post>commChain & here
//    private List<Comment> replyChain; //Save reply chain of comments by ID?
//    private boolean isReply; //if the comment is a reply to another COMMENT, this flag is set

    //To create a comment, a User must provide just the comment contents. User ID is recorded, boolean flag set based on if they
    //are replying to another comment or not via GUI.
    public Comment(String commentedByUID, String commContents, boolean isReply) {
        this.commContents = commContents; // The user's message
        this.commentedByUID = commentedByUID; //User ID always exists beforehand
        this.commentID = UUID.randomUUID().toString(); //Need to generate this randomly & consistently for all Posts/Comments
        this.createdAt = LocalDateTime.now();
//        this.replyChain = new ArrayList<>();
        this.likeCt = 0;
//        this.isReply = isReply;
    }

//    public void addReply(Comment comment) {
//        //if isReply
//        this.replyChain.add(comment);
//    }

    public String getCommentedByUID() {
        return commentedByUID;
    }

    public String getCommentID() {
        return commentID;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //    public long getTimestamp() {
    //        return timestamp;     // Returns creation time
    //    }

    public String getCommContents() {
        return commContents;
    }

    //Editing comments?
    public void setCommContents(String commContents) {
        this.commContents = commContents;
    }

    public int getLikeCt() {
        return likeCt;
    }

    public void setLikeCt(int likeCt) {
        this.likeCt = likeCt;
    }

//    public List<Comment> getReplyChain() {
//        return replyChain;
//    }
//
//    public boolean isReply() {
//        return isReply;
//    }
//
//    public void setReply(boolean reply) {
//        isReply = reply;
//    }

    public String getParentCommentID() {
        return parentCommentID;
    }

    public void setParentCommentID(String parentCommentID) {
        this.parentCommentID = parentCommentID;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentedByUID='" + commentedByUID + '\'' +
                ", commentID='" + commentID + '\'' +
                ", commentDateTime=" + createdAt +
                ", commContents='" + commContents + '\'' +
                ", likeCt=" + likeCt +
//                ", isReply=" + isReply +
//                ", replyCt=" + replyChain.size() +
                '}';
    }
}
