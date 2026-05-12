package Model;

public class GuideAnswer {
    private String answerId;
    private String answerText;
    private String username;
    private Long timestamp;
    private int upvotes;
    private int downvotes;
    private String currentUserVote;

    public GuideAnswer() {
    }

    public GuideAnswer(String answerId, String answerText, String username, Long timestamp, int upvotes, int downvotes, String currentUserVote) {
        this.answerId = answerId;
        this.answerText = answerText;
        this.username = username;
        this.timestamp = timestamp;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.currentUserVote = currentUserVote;
    }

    public String getAnswerId() {
        return answerId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public String getUsername() {
        return username;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public String getCurrentUserVote() {
        return currentUserVote;
    }

    public void setCurrentUserVote(String currentUserVote) {
        this.currentUserVote = currentUserVote;
    }
}