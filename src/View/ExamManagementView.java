package View;

import DAO.ExamDAO;
import DAO.QuestionDAO;
import model.Exam;
import model.ExamQuestion;
import model.Question;
import model.User;
import Util.WordExportUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ExamManagementView extends JFrame {
    private final Color PRIMARY_COLOR = new Color(51, 122, 183);
    private final Color BACKGROUND_COLOR = new Color(240, 242, 245);
    private final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 16);
    private final Font DEFAULT_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private final Font TABLE_FONT = new Font("微软雅黑", Font.PLAIN, 13);

    private JTable examTable; // 试卷列表
    private JTable questionTable; // 题目列表
    private DefaultTableModel examTableModel;
    private DefaultTableModel questionTableModel;
    private ExamDAO examDAO;
    private QuestionDAO questionDAO;
    private Frame parentFrame;
    private User currentUser;
    private List<Question> selectedQuestions; // 已选择的题目

    public ExamManagementView(Frame parentFrame, User user) {
        this.parentFrame = parentFrame;
        this.currentUser = user;
        this.examDAO = new ExamDAO();
        this.questionDAO = new QuestionDAO();
        this.selectedQuestions = new ArrayList<>();
        initComponents();
        loadExamData();
        loadQuestionData();
    }

    private void initComponents() {
        setTitle("试卷管理");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                }
            }
        });

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建工具栏
        JToolBar toolBar = createToolBar();
        mainPanel.add(toolBar, BorderLayout.NORTH);

        // 创建分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);

        // 左侧面板 - 试卷列表
        JPanel leftPanel = createExamListPanel();
        splitPane.setLeftComponent(leftPanel);

        // 右侧面板 - 题目列表
        JPanel rightPanel = createQuestionListPanel();
        splitPane.setRightComponent(rightPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(BACKGROUND_COLOR);

        JButton createExamButton = createStyledButton("新建试卷");
        JButton editExamButton = createStyledButton("编辑试卷");
        JButton deleteExamButton = createStyledButton("删除试卷");
        JButton previewExamButton = createStyledButton("预览试卷");

        createExamButton.addActionListener(e -> showCreateExamDialog());
        editExamButton.addActionListener(e -> showEditExamDialog());
        deleteExamButton.addActionListener(e -> deleteSelectedExam());
        previewExamButton.addActionListener(e -> previewExam());

        toolBar.add(createExamButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(editExamButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteExamButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(previewExamButton);

        return toolBar;
    }

    private JPanel createExamListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 创建试卷表格
        String[] examColumns = {"ID", "试卷名称", "总分", "时长(分钟)", "创建时间"};
        examTableModel = new DefaultTableModel(examColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        examTable = new JTable(examTableModel);
        setupTableStyle(examTable);

        JScrollPane scrollPane = new JScrollPane(examTable);
        panel.add(new JLabel("试卷列表", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createQuestionListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 创建题目表格
        String[] questionColumns = {"ID", "题目内容", "题型", "难度", "分值"};
        questionTableModel = new DefaultTableModel(questionColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        questionTable = new JTable(questionTableModel);
        setupTableStyle(questionTable);

        JScrollPane scrollPane = new JScrollPane(questionTable);
        panel.add(new JLabel("题目列表", SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = createStyledButton("添加到试卷");
        addButton.addActionListener(e -> addSelectedQuestionToExam());
        buttonPanel.add(addButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void setupTableStyle(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(232, 241, 249));
        table.setSelectionForeground(Color.BLACK);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(DEFAULT_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }

    private void loadExamData() {
        examTableModel.setRowCount(0);
        List<Exam> exams = examDAO.getExamsByTeacher(currentUser.getUserId());
        for (Exam exam : exams) {
            Object[] rowData = {
                    exam.getExamId(),
                    exam.getExamName(),
                    exam.getTotalScore(),
                    exam.getDuration(),
                    exam.getCreatedAt()
            };
            examTableModel.addRow(rowData);
        }
    }

    private void loadQuestionData() {
        questionTableModel.setRowCount(0);
        List<Question> questions = questionDAO.getAllQuestions();
        for (Question question : questions) {
            Object[] rowData = {
                    question.getQuestionId(),
                    question.getQuestionText(),
                    question.getQuestionType(),
                    question.getDifficulty(),
                    question.getScore()
            };
            questionTableModel.addRow(rowData);
        }
    }

    private void showCreateExamDialog() {
        JDialog dialog = new JDialog(this, "新建试卷", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 添加表单组件
        JTextField nameField = new JTextField(20);
        JTextField durationField = new JTextField(20);
        JTextField totalScoreField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("试卷名称:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("考试时长(分钟):"), gbc);
        gbc.gridx = 1;
        panel.add(durationField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("总分:"), gbc);
        gbc.gridx = 1;
        panel.add(totalScoreField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                int duration = Integer.parseInt(durationField.getText().trim());
                BigDecimal totalScore = new BigDecimal(totalScoreField.getText().trim());

                Exam exam = new Exam();
                exam.setExamName(name);
                exam.setDuration(duration);
                exam.setTotalScore(totalScore);
                exam.setCreatedBy(currentUser.getUserId());

                if (examDAO.addExam(exam)) {
                    JOptionPane.showMessageDialog(dialog, "创建成功");
                    loadExamData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "创建失败");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "请输入有效的数字");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showEditExamDialog() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的试卷");
            return;
        }

        int examId = (int) examTable.getValueAt(selectedRow, 0);
        Exam exam = examDAO.getExamById(examId);
        if (exam == null) {
            JOptionPane.showMessageDialog(this, "获取试卷信息失败");
            return;
        }

        EditExamDialog dialog = new EditExamDialog(this, exam);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                loadExamData(); // 刷新试卷列表
            }
        });
        dialog.setVisible(true);
    }

    private void deleteSelectedExam() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的试卷");
            return;
        }

        int examId = (int) examTable.getValueAt(selectedRow, 0);
        int option = JOptionPane.showConfirmDialog(this,
                "确定要删除这份试卷吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            if (examDAO.deleteExam(examId)) {
                JOptionPane.showMessageDialog(this, "删除成功");
                loadExamData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败");
            }
        }
    }

    private void addSelectedQuestionToExam() {
        int selectedExamRow = examTable.getSelectedRow();
        int selectedQuestionRow = questionTable.getSelectedRow();

        if (selectedExamRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的试卷");
            return;
        }

        if (selectedQuestionRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要添加的题目");
            return;
        }

        int examId = (int) examTable.getValueAt(selectedExamRow, 0);
        int questionId = (int) questionTable.getValueAt(selectedQuestionRow, 0);

        // 获取试卷信息
        Exam exam = examDAO.getExamById(examId);
        Question question = questionDAO.getQuestionById(questionId);

        if (exam == null || question == null) {
            JOptionPane.showMessageDialog(this, "获取信息失败");
            return;
        }

        // 创建试题关联对象
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExamId(examId);
        examQuestion.setQuestionId(questionId);
        examQuestion.setOrder(exam.getExamQuestions() != null ? exam.getExamQuestions().size() + 1 : 1);
        examQuestion.setScore(BigDecimal.valueOf(question.getScore()));

        // 添加到试卷中
        if (exam.getExamQuestions() == null) {
            exam.setExamQuestions(new ArrayList<>());
        }
        exam.getExamQuestions().add(examQuestion);

        // 更新试卷
        if (examDAO.updateExam(exam)) {
            JOptionPane.showMessageDialog(this, "添加成功");
            loadExamData();
        } else {
            JOptionPane.showMessageDialog(this, "添加失败");
        }
    }

    private void previewExam() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要预览的试卷");
            return;
        }

        int examId = (int) examTable.getValueAt(selectedRow, 0);
        Exam exam = examDAO.getExamById(examId);

        if (exam == null) {
            JOptionPane.showMessageDialog(this, "获取试卷信息失败");
            return;
        }

        // 创建预览对话框
        JDialog previewDialog = new JDialog(this, "试卷预览", true);
        previewDialog.setSize(800, 600);
        previewDialog.setLocationRelativeTo(this);
        previewDialog.setLayout(new BorderLayout()); // 设置布局

        // 创建内容面板
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 添加试卷标题
        JLabel titleLabel = new JLabel(exam.getExamName());
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));

        // 添加试卷信息
        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel("总分：" + exam.getTotalScore() + "分"));
        infoPanel.add(Box.createHorizontalStrut(30));
        infoPanel.add(new JLabel("时长：" + exam.getDuration() + "分钟"));
        contentPanel.add(infoPanel);
        contentPanel.add(Box.createVerticalStrut(20));

        // 添加试题
        String currentType = null;
        BigDecimal typeTotal = BigDecimal.ZERO;

        if (exam.getExamQuestions() != null) {
            for (int i = 0; i < exam.getExamQuestions().size(); i++) {
                ExamQuestion eq = exam.getExamQuestions().get(i);
                Question q = eq.getQuestion();

                // 如果是新题型，添加标题
                if (currentType == null || !currentType.equals(q.getQuestionType())) {
                    // 添加上一个题型的总分
                    if (currentType != null) {
                        contentPanel.add(new JLabel(currentType + "总分：" + typeTotal + "分"));
                        contentPanel.add(Box.createVerticalStrut(10));
                        typeTotal = BigDecimal.ZERO;
                    }

                    currentType = q.getQuestionType();
                    JLabel typeLabel = new JLabel("【" + currentType + "】");
                    typeLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
                    contentPanel.add(typeLabel);
                    contentPanel.add(Box.createVerticalStrut(10));
                }

                // 添加题目
                JLabel questionLabel = new JLabel((i + 1) + ". " + q.getQuestionText() +
                        " (" + eq.getScore() + "分)");
                contentPanel.add(questionLabel);
                contentPanel.add(Box.createVerticalStrut(5));

                // 累加当前题型总分
                typeTotal = typeTotal.add(eq.getScore());
            }

            // 添加最后一个题型的总分
            if (currentType != null) {
                contentPanel.add(new JLabel(currentType + "总分：" + typeTotal + "分"));
            }
        }

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("保存为Word");
        saveButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("保存试卷");
            fileChooser.setFileFilter(new FileNameExtensionFilter("RTF文档(*.rtf)", "rtf"));

            if (fileChooser.showSaveDialog(previewDialog) == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".rtf")) {
                    filePath += ".rtf";
                }

                try {
                    WordExportUtil.exportExamToRTF(exam, filePath);
                    JOptionPane.showMessageDialog(previewDialog,
                            "试卷已成功保存为RTF文档！\n可使用Word打开编辑",
                            "保存成功",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(previewDialog,
                            "保存失败：" + ex.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        buttonPanel.add(saveButton);

        // 添加组件到对话框
        previewDialog.add(scrollPane, BorderLayout.CENTER);
        previewDialog.add(buttonPanel, BorderLayout.SOUTH);

        // 最后才显示对话框
        previewDialog.setVisible(true);
    }
}