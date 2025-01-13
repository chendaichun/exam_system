package DAO;


import model.Question;
import Util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    // 添加题目
    public boolean addQuestion(Question question) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "INSERT INTO question (category_id, question_text, image_url, question_type, answer, difficulty, score, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, question.getCategoryId());
            pstmt.setString(2, question.getQuestionText());
            pstmt.setString(3, question.getImageUrl());
            pstmt.setString(4, question.getQuestionType());
            pstmt.setString(5, question.getAnswer());
            pstmt.setString(6, question.getDifficulty());
            pstmt.setDouble(7, question.getScore());
            pstmt.setInt(8, question.getCreatedBy());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.close(conn, pstmt, null);
        }
    }

    // 根据分类 ID 查询题目
    public List<Question> getQuestionsByCategoryId(int categoryId) {
        List<Question> questions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "SELECT * FROM question WHERE category_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Question question = new Question();
                question.setQuestionId(rs.getInt("question_id"));
                question.setCategoryId(rs.getInt("category_id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setImageUrl(rs.getString("image_url"));
                question.setQuestionType(rs.getString("question_type"));
                question.setAnswer(rs.getString("answer"));
                question.setDifficulty(rs.getString("difficulty"));
                question.setScore(rs.getDouble("score"));
                question.setCreatedBy(rs.getInt("created_by"));
                question.setCreatedAt(rs.getTimestamp("created_at"));
                questions.add(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(conn, pstmt, rs);
        }

        return questions;
    }


    /**
     * 获取所有题目
     * @return 题目列表
     */
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "SELECT * FROM question ORDER BY created_at DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Question question = new Question();
                question.setQuestionId(rs.getInt("question_id"));
                question.setCategoryId(rs.getInt("category_id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setImageUrl(rs.getString("image_url"));
                question.setQuestionType(rs.getString("question_type"));
                question.setAnswer(rs.getString("answer"));
                question.setDifficulty(rs.getString("difficulty"));
                question.setScore(rs.getDouble("score"));
                question.setCreatedBy(rs.getInt("created_by"));
                question.setCreatedAt(rs.getTimestamp("created_at"));
                questions.add(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(conn, pstmt, rs);
        }

        return questions;
    }

    /**
     * 删除题目
     * @param questionId 题目ID
     * @return 是否删除成功
     */
    public boolean deleteQuestion(int questionId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            // 首先检查题目是否已被使用在试卷中
            String checkSql = "SELECT COUNT(*) FROM exam_question WHERE question_id = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // 题目已被使用在试卷中，不能删除
                return false;
            }

            // 如果题目未被使用，则执行删除
            String deleteSql = "DELETE FROM question WHERE question_id = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setInt(1, questionId);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.close(conn, pstmt, null);
        }
    }
    /**
     * 根据ID查询题目
     * @param questionId 题目ID
     * @return Question对象，如果未找到则返回null
     */
    public Question getQuestionById(int questionId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "SELECT * FROM question WHERE question_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, questionId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Question question = new Question();
                question.setQuestionId(rs.getInt("question_id"));
                question.setCategoryId(rs.getInt("category_id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setImageUrl(rs.getString("image_url"));
                question.setQuestionType(rs.getString("question_type"));
                question.setAnswer(rs.getString("answer"));
                question.setAnswerImageUrl(rs.getString("answer_image_url"));
                question.setDifficulty(rs.getString("difficulty"));
                question.setScore(rs.getDouble("score"));
                question.setCreatedBy(rs.getInt("created_by"));
                question.setCreatedAt(rs.getTimestamp("created_at"));
                return question;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(conn, pstmt, rs);
        }

        return null;
    }
}