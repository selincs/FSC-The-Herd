package Model;

//Should the friends list be its own class? How do we show online/offline users in the friends list?

//Friend request class? We gotta develop this idea a bit more :)
public class Friendship {
    //Friends class -> Enables users to manage own list, good for storing friends ppl have
    //Implement a friends list class, so a user can manage their own friends list
    //could be broadened to include not just friends, but maybe other things (pending Friend Requests)
    //Relationships? Friendships, Pending Relationships?

    private int requesterId;
    private int receiverId;
    private FriendshipStatus status;

    // when a friendship request is created
    //Friend Request status
    public Friendship(int requesterId, int receiverId) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = FriendshipStatus.PENDING;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    // when request accepted
    public void accept() {
        status = FriendshipStatus.ACCEPTED;
    }

    // when request rejected
    public void reject() {
        status = FriendshipStatus.REJECTED;
    }

    public void block(){ status = FriendshipStatus.BLOCKED;}

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        BLOCKED
    }
}
