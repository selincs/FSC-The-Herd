package Model;

public class Hobby {

    private String hobbyName;
    private int memberCount;

    public Hobby(String hobbyName) {
        this.hobbyName = hobbyName;
        this.memberCount = 0;
    }

    public String getHobbyName() {
        return hobbyName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void incrementMembers() {
        memberCount++;
    }

    public void decrementMembers() {
        if (memberCount > 0) {
            memberCount--;
        }
    }
}
