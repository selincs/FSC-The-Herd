package Model;

public class MentorRelationship {
    private int mentorID;
    private int menteeId;

    private RelationshipStatus status;

    public int getMenteeId() {
        return menteeId;
    }

    public int getMentorID() {
        return mentorID;
    }

    public enum RelationshipStatus{
        PENDING,
        ACTIVE,
        COMPLETED,


    }
}
