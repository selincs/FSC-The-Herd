package Model;

import java.time.LocalDateTime;
import java.util.List;

public class Comment {
    private String commentedByUserID;
    private String commentID;  //id of comment -> Used for comment history of a User, building post/comments in CB

    private LocalDateTime commentDateTime;
    private String commContents;
    private int likeCt;
    private List<String> commentChain; //Save comment chain by ID?
    //How to do reporting comments?
    //Do we care about replies?
    //pictures? videos?

}
