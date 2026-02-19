package Model;

public class Interest {

    private String interestName;
    private int memberCount;

    public Interest(String interestName) {
        this.interestName = interestName;
        this.memberCount = 0;
    }

    public String getInterestName() {
        return interestName;
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
