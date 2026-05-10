package Model;

public class GuideQuestion {
    private String questionId;
    private String questionText;
    private String username;
    private Long timestamp;
    private String topAnswer;

    public GuideQuestion() {
    }

    public GuideQuestion(String questionId, String questionText, String username, Long timestamp, String topAnswer) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.username = username;
        this.timestamp = timestamp;
        this.topAnswer = topAnswer;
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

    public String getTopAnswer() {
        return topAnswer;
    }

    public void setTopAnswer(String topAnswer) {
        this.topAnswer = topAnswer;
    }
}