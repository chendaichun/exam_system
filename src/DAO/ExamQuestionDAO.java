package DAO;
import Util.DatabaseUtil;
import model.ExamQuestion;
import model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExamQuestionDAO {

    // 添加试卷题目关联
    public boolean addExamQuestion(ExamQuestion examQuestion) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "INSERT INTO exam_question (exam_id, question_id, `order`, score) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, examQuestion.getExamId());
            pstmt.setInt(2, examQuestion.getQuestionId());
            pstmt.setInt(3, examQuestion.getOrder());
            pstmt.setBigDecimal(4, examQuestion.getScore());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.close(conn, pstmt, null);
        }
    }

    // 批量添加试卷题目关联
    public boolean addExamQuestions(List<ExamQuestion> examQuestions) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false); // 开启事务

            String sql = "INSERT INTO exam_question (exam_id, question_id, `order`, score) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);

            for (ExamQuestion eq : examQuestions) {
                pstmt.setInt(1, eq.getExamId());
                pstmt.setInt(2, eq.getQuestionId());
                pstmt.setInt(3, eq.getOrder());
                pstmt.setBigDecimal(4, eq.getScore());
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            conn.commit(); // 提交事务

            return results.length == examQuestions.size();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // 发生异常时回滚
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 恢复自动提交
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DatabaseUtil.close(conn, pstmt, null);
        }
    }

    // 获取试卷的所有题目
    public List<ExamQuestion> getQuestionsByExamId(int examId) {
        List<ExamQuestion> examQuestions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "SELECT eq.*, q.* FROM exam_question eq " +
                    "JOIN question q ON eq.question_id = q.question_id " +
                    "WHERE eq.exam_id = ? ORDER BY eq.`order`";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, examId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ExamQuestion eq = new ExamQuestion();
                eq.setExamId(rs.getInt("exam_id"));
                eq.setQuestionId(rs.getInt("question_id"));
                eq.setOrder(rs.getInt("order"));
                eq.setScore(rs.getBigDecimal("score"));

                // 设置题目详细信息
                Question question = new Question();
                question.setQuestionId(rs.getInt("question_id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setQuestionType(rs.getString("question_type"));
                question.setDifficulty(rs.getString("difficulty"));
                // ... 设置其他题目属性

                eq.setQuestion(question);
                examQuestions.add(eq);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(conn, pstmt, rs);
        }

        return examQuestions;
    }

    // 删除试卷的所有题目关联
    public boolean deleteByExamId(int examId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "DELETE FROM exam_question WHERE exam_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, examId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.close(conn, pstmt, null);
        }
    }

    // 更新题目顺序和分数
    public boolean updateExamQuestion(ExamQuestion examQuestion) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "UPDATE exam_question SET `order` = ?, score = ? WHERE exam_id = ? AND question_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, examQuestion.getOrder());
            pstmt.setBigDecimal(2, examQuestion.getScore());
            pstmt.setInt(3, examQuestion.getExamId());
            pstmt.setInt(4, examQuestion.getQuestionId());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.close(conn, pstmt, null);
        }
    }
}