package Model;

public class Friendship {

    private int requesterId;
    private int receiverId;
    private FriendshipStatus status;

    // when a friendship request is created
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

    public enum FriendshipStatus {
        PENDING,
        ACCEPTED,
        REJECTED
    }
}
