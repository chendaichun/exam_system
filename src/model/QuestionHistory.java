package model;


import java.sql.Timestamp;

public class QuestionHistory {
    private int historyId;
    private int questionId;
    private int userId;
    private String operationType;  // 'create', 'update', 'delete'
    private Timestamp operationTime;

    // 可选的关联信息，用于显示
    private String username;       // 操作用户名
    private String questionText;   // 题目内容

    // 构造函数
    public QuestionHistory() {
    }

    public QuestionHistory(int questionId, int userId, String operationType) {
        this.questionId = questionId;
        this.userId = userId;
        this.operationType = operationType;
    }

    // Getters and Setters
    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Timestamp getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Timestamp operationTime) {
        this.operationTime = operationTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    // 获取操作类型的显示文本
    public String getOperationTypeDisplay() {
        switch (operationType) {
            case "create": return "创建";
            case "update": return "修改";
            case "delete": return "删除";
            default: return operationType;
        }
    }

    @Override
    public String toString() {
        return "QuestionHistory{" +
                "historyId=" + historyId +
                ", questionId=" + questionId +
                ", userId=" + userId +
                ", operationType='" + operationType + '\'' +
                ", operationTime=" + operationTime +
                ", username='" + username + '\'' +
                ", questionText='" + questionText + '\'' +
                '}';
    }
}
