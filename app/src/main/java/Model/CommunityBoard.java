package Model;

import java.util.List;

public class CommunityBoard {
    //This does need to be its own class. A user does not own a CommBoard, it is its own entity where 2 users connect.
    //A topic HAS a community board among other things(guides, events, etc) -> Where does this list of Topics live?
    private List<Post> boardPosts;  //A community board has : Posts (posts have likes & comments)

    // -DateTimes on all above
    //UserID of user who did the action
    //If user wants a post/comment/like history (ListView of recent posts, etc.)

    //Pictures? Videos? In Post class. Does a CommB need a picture?

    private List<String> moderatorIDs;    //Community moderators (Self managed?)
}
