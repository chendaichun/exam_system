package DAO;

import model.QuestionHistory;
import Util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionHistoryDAO {
    // 添加操作记录
    public boolean addHistory(QuestionHistory history) {
        String sql = "INSERT INTO question_history (question_id, user_id, operation_type) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, history.getQuestionId());
            pstmt.setInt(2, history.getUserId());
            pstmt.setString(3, history.getOperationType());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取指定题目的操作历史
    public List<QuestionHistory> getHistoryByQuestionId(int questionId) {
        List<QuestionHistory> historyList = new ArrayList<>();
        String sql = "SELECT h.*, u.username FROM question_history h " +
                "JOIN user u ON h.user_id = u.user_id " +
                "WHERE h.question_id = ? " +
                "ORDER BY h.operation_time DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                QuestionHistory history = new QuestionHistory();
                history.setHistoryId(rs.getInt("history_id"));
                history.setQuestionId(rs.getInt("question_id"));
                history.setUserId(rs.getInt("user_id"));
                history.setUsername(rs.getString("username"));
                history.setOperationType(rs.getString("operation_type"));
                history.setOperationTime(rs.getTimestamp("operation_time"));
                historyList.add(history);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyList;
    }

    // 获取用户的操作历史
    public List<QuestionHistory> getHistoryByUserId(int userId) {
        List<QuestionHistory> historyList = new ArrayList<>();
        String sql = "SELECT h.*, q.question_text FROM question_history h " +
                "JOIN question q ON h.question_id = q.question_id " +
                "WHERE h.user_id = ? " +
                "ORDER BY h.operation_time DESC";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                QuestionHistory history = new QuestionHistory();
                history.setHistoryId(rs.getInt("history_id"));
                history.setQuestionId(rs.getInt("question_id"));
                history.setQuestionText(rs.getString("question_text"));
                history.setUserId(userId);
                history.setOperationType(rs.getString("operation_type"));
                history.setOperationTime(rs.getTimestamp("operation_time"));
                historyList.add(history);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyList;
    }

    // 获取单条历史记录
    public QuestionHistory getHistoryById(int historyId) {
        String sql = "SELECT h.*, u.username, q.question_text FROM question_history h " +
                "JOIN user u ON h.user_id = u.user_id " +
                "JOIN question q ON h.question_id = q.question_id " +
                "WHERE h.history_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, historyId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                QuestionHistory history = new QuestionHistory();
                history.setHistoryId(rs.getInt("history_id"));
                history.setQuestionId(rs.getInt("question_id"));
                history.setUserId(rs.getInt("user_id"));
                history.setUsername(rs.getString("username"));
                history.setQuestionText(rs.getString("question_text"));
                history.setOperationType(rs.getString("operation_type"));
                history.setOperationTime(rs.getTimestamp("operation_time"));
                return history;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}