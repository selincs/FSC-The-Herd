package Model;

public class GuideQuestion {
    private String questionId;
    private String questionText;
    private String username;
    private Long timestamp;

    public GuideQuestion() {
    }

    public GuideQuestion(String questionId, String questionText, String username, Long timestamp) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.username = username;
        this.timestamp = timestamp;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getUsername() {
        return username;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}