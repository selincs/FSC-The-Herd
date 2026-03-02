package Model;

import java.time.LocalDateTime;
import java.util.List;

public class Post {
    //TODO: Questions - Do posts ever expire? Do CommBoards expire? Do things time out? Stretch goal maybe.
    private String postedByUserID;
    private String postID;  //id of post -> Used for post history of a User, building post/comments in CB

    private LocalDateTime postDateTime;
    private String postContents;
    private int likeCt;
    private List<String> commentChain; //Save comment chain by ID?
    //How to do reporting posts?
    //pictures? videos?

}
