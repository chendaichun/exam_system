package DAO;


import model.Exam;
import model.ExamQuestion;
import Util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    // 添加试卷
    public boolean addExam(Exam exam) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "INSERT INTO exam (exam_name, total_score, duration, created_by) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, exam.getExamName());
            pstmt.setBigDecimal(2, exam.getTotalScore());
            pstmt.setInt(3, exam.getDuration());
            pstmt.setInt(4, exam.getCreatedBy());

            int rows = pstmt.executeUpdate();
            if (rows > 0 && exam.getExamQuestions() != null) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int examId = rs.getInt(1);
                    exam.setExamId(examId);
                    // 添加试卷题目关联
                    ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
                    for (ExamQuestion question : exam.getExamQuestions()) {
                        question.setExamId(examId);
                        examQuestionDAO.addExamQuestion(question);
                    }
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.close(conn, pstmt, rs);
        }
    }

    // 更新试卷
    public boolean updateExam(Exam exam) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "UPDATE exam SET exam_name = ?, total_score = ?, duration = ? WHERE exam_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, exam.getExamName());
            pstmt.setBigDecimal(2, exam.getTotalScore());
            pstmt.setInt(3, exam.getDuration());
            pstmt.setInt(4, exam.getExamId());

            int rows = pstmt.executeUpdate();
            if (rows > 0 && exam.getExamQuestions() != null) {
                // 更新试卷题目关联
                ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
                examQuestionDAO.deleteByExamId(exam.getExamId()); // 删除旧关联
                for (ExamQuestion question : exam.getExamQuestions()) {
                    examQuestionDAO.addExamQuestion(question); // 添加新关联
                }
            }
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseUtil.close(conn, pstmt, null);
        }
    }

    // 删除试卷
    public boolean deleteExam(int examId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseUtil.getConnection();
            // 首先删除试卷题目关联
            ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
            examQuestionDAO.deleteByExamId(examId);

            // 然后删除试卷
            String sql = "DELETE FROM exam WHERE exam_id = ?";
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

    // 根据ID获取试卷
    public Exam getExamById(int examId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Exam exam = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "SELECT * FROM exam WHERE exam_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, examId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                exam = new Exam();
                exam.setExamId(rs.getInt("exam_id"));
                exam.setExamName(rs.getString("exam_name"));
                exam.setTotalScore(rs.getBigDecimal("total_score"));
                exam.setDuration(rs.getInt("duration"));
                exam.setCreatedBy(rs.getInt("created_by"));
                exam.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                // 获取试卷题目
                ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
                exam.setExamQuestions(examQuestionDAO.getQuestionsByExamId(examId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(conn, pstmt, rs);
        }

        return exam;
    }

    // 获取教师创建的所有试卷
    public List<Exam> getExamsByTeacher(int teacherId) {
        List<Exam> exams = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            String sql = "SELECT * FROM exam WHERE created_by = ? ORDER BY created_at DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teacherId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Exam exam = new Exam();
                exam.setExamId(rs.getInt("exam_id"));
                exam.setExamName(rs.getString("exam_name"));
                exam.setTotalScore(rs.getBigDecimal("total_score"));
                exam.setDuration(rs.getInt("duration"));
                exam.setCreatedBy(rs.getInt("created_by"));
                exam.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                exams.add(exam);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(conn, pstmt, rs);
        }

        return exams;
    }
}
