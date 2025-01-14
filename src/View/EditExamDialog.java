package View;

import DAO.ExamDAO;
import DAO.QuestionDAO;
import model.Exam;
import model.ExamQuestion;
import model.Question;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class EditExamDialog extends JDialog {
    private final Color PRIMARY_COLOR = new Color(51, 122, 183);
    private final Font DEFAULT_FONT = new Font("微软雅黑", Font.PLAIN, 14);

    private Exam currentExam;
    private ExamDAO examDAO;
    private QuestionDAO questionDAO;
    private DefaultTableModel selectedQuestionsModel;
    private DefaultTableModel availableQuestionsModel;
    private JTable selectedQuestionsTable;
    private JTable availableQuestionsTable;
    private JTextField examNameField;
    private JTextField durationField;
    private JTextField totalScoreField;
    private List<ExamQuestion> examQuestions;
    private JLabel currentTotalScoreLabel;

    // 添加整理状态标识
    private boolean isOrganized = false;
    private JButton organizeButton;

    public EditExamDialog(Frame owner, Exam exam) {
        super(owner, "编辑试卷", true);
        this.currentExam = exam;
        this.examDAO = new ExamDAO();
        this.questionDAO = new QuestionDAO();
        this.examQuestions = new ArrayList<>();
        if (exam.getExamQuestions() != null) {
            this.examQuestions.addAll(exam.getExamQuestions());
        }
        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(1000, 800);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        // 创建基本信息面板
        JPanel infoPanel = createInfoPanel();
        add(infoPanel, BorderLayout.NORTH);

        // 创建主内容面板（题目选择区域）
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);

        // 左侧已选题目面板
        JPanel selectedQuestionsPanel = createSelectedQuestionsPanel();
        splitPane.setLeftComponent(selectedQuestionsPanel);

        // 右侧可选题目面板
        JPanel availableQuestionsPanel = createAvailableQuestionsPanel();
        splitPane.setRightComponent(availableQuestionsPanel);

        add(splitPane, BorderLayout.CENTER);

        // 创建底部按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 试卷名称
        examNameField = new JTextField(currentExam.getExamName(), 20);
        addFormField(panel, "试卷名称:", examNameField, gbc, 0);

        // 考试时长
        durationField = new JTextField(String.valueOf(currentExam.getDuration()), 10);
        addFormField(panel, "考试时长(分钟):", durationField, gbc, 1);

        // 总分
        totalScoreField = new JTextField(String.valueOf(currentExam.getTotalScore()), 10);
        addFormField(panel, "总分:", totalScoreField, gbc, 2);

        // 当前总分显示
        currentTotalScoreLabel = new JLabel("当前题目总分: 0");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(currentTotalScoreLabel, gbc);

        return panel;
    }

    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(field, gbc);
    }

    private JPanel createSelectedQuestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("已选题目"));

        // 创建表格模型
        String[] columns = {"序号", "题目内容", "题型", "分值"};
        selectedQuestionsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3; // 只允许编辑分值列
            }
        };

        selectedQuestionsTable = new JTable(selectedQuestionsModel);
        setupTableStyle(selectedQuestionsTable);

        // 添加工具按钮
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton removeButton = createStyledButton("删除选中题目");
        JButton moveUpButton = createStyledButton("上移");
        JButton moveDownButton = createStyledButton("下移");

        removeButton.addActionListener(e -> removeSelectedQuestion());
        moveUpButton.addActionListener(e -> moveQuestionUp());
        moveDownButton.addActionListener(e -> moveQuestionDown());

        toolPanel.add(removeButton);
        toolPanel.add(moveUpButton);
        toolPanel.add(moveDownButton);

        panel.add(toolPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(selectedQuestionsTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAvailableQuestionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("可选题目"));

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = createStyledButton("搜索");
        searchPanel.add(new JLabel("关键词:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 创建表格
        String[] columns = {"ID", "题目内容", "题型", "难度", "分值"};
        availableQuestionsModel = new DefaultTableModel(columns, 0);
        availableQuestionsTable = new JTable(availableQuestionsModel);
        setupTableStyle(availableQuestionsTable);

        // 添加题目按钮
        JButton addButton = createStyledButton("添加选中题目");
        addButton.addActionListener(e -> addSelectedQuestion());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(addButton);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(availableQuestionsTable), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    // 在createButtonPanel中添加整理按钮
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        organizeButton = createStyledButton("一键整理");
        JButton saveButton = createStyledButton("保存");
        JButton cancelButton = createStyledButton("取消");


        organizeButton.addActionListener(e -> organizeQuestions());
        //printButton.addActionListener(e -> previewExam());
        saveButton.addActionListener(e -> saveExam());
        cancelButton.addActionListener(e -> dispose());

        panel.add(organizeButton);
        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void setupTableStyle(JTable table) {
        table.setFont(DEFAULT_FONT);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
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

    private void loadData() {
        // 加载已选题目
        updateSelectedQuestionsTable();

        // 加载可选题目
        List<Question> allQuestions = questionDAO.getAllQuestions();
        for (Question question : allQuestions) {
            // 如果题目未被选中，则添加到可选题目列表
            if (!isQuestionSelected(question)) {
                Object[] rowData = {
                        question.getQuestionId(),
                        question.getQuestionText(),
                        question.getQuestionType(),
                        question.getDifficulty(),
                        question.getScore()
                };
                availableQuestionsModel.addRow(rowData);
            }
        }

        updateTotalScore();
    }

    private void updateSelectedQuestionsTable() {
        selectedQuestionsModel.setRowCount(0);
        for (int i = 0; i < examQuestions.size(); i++) {
            ExamQuestion eq = examQuestions.get(i);
            Question q = eq.getQuestion();
            Object[] rowData = {
                    i + 1,
                    q.getQuestionText(),
                    q.getQuestionType(),
                    eq.getScore()
            };
            selectedQuestionsModel.addRow(rowData);
        }
    }

    private boolean isQuestionSelected(Question question) {
        return examQuestions.stream()
                .anyMatch(eq -> eq.getQuestionId() == question.getQuestionId());
    }

    private void addSelectedQuestion() {
        int selectedRow = availableQuestionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要添加的题目");
            return;
        }

        int questionId = (int) availableQuestionsTable.getValueAt(selectedRow, 0);
        Question question = questionDAO.getQuestionById(questionId);

        // 创建新的试题关联对象
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setExamId(currentExam.getExamId());
        examQuestion.setQuestionId(questionId);
        examQuestion.setOrder(examQuestions.size() + 1);
        examQuestion.setScore(BigDecimal.valueOf(question.getScore()));
        examQuestion.setQuestion(question);

        examQuestions.add(examQuestion);
        updateSelectedQuestionsTable();
        availableQuestionsModel.removeRow(selectedRow);
        updateTotalScore();
    }

    private void removeSelectedQuestion() {
        int selectedRow = selectedQuestionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的题目");
            return;
        }

        ExamQuestion removedQuestion = examQuestions.remove(selectedRow);
        Question question = removedQuestion.getQuestion();

        // 将题目添加回可选题目列表
        Object[] rowData = {
                question.getQuestionId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getDifficulty(),
                question.getScore()
        };
        availableQuestionsModel.addRow(rowData);

        updateSelectedQuestionsTable();
        updateTotalScore();
    }

    private void moveQuestionUp() {
        int selectedRow = selectedQuestionsTable.getSelectedRow();
        if (selectedRow <= 0) return;

        swapQuestions(selectedRow, selectedRow - 1);
        updateSelectedQuestionsTable();
        selectedQuestionsTable.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
    }

    private void moveQuestionDown() {
        int selectedRow = selectedQuestionsTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= examQuestions.size() - 1) return;

        swapQuestions(selectedRow, selectedRow + 1);
        updateSelectedQuestionsTable();
        selectedQuestionsTable.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
    }

    private void swapQuestions(int index1, int index2) {
        ExamQuestion temp = examQuestions.get(index1);
        examQuestions.set(index1, examQuestions.get(index2));
        examQuestions.set(index2, temp);
    }

    private void updateTotalScore() {
        BigDecimal total = BigDecimal.ZERO;
        for (ExamQuestion eq : examQuestions) {
            total = total.add(eq.getScore());
        }
        currentTotalScoreLabel.setText("当前题目总分: " + total);
    }

    private void saveExam() {
        if (!isOrganized) {
            int option = JOptionPane.showConfirmDialog(this,
                    "试卷尚未整理，是否先进行整理？",
                    "提示",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                organizeQuestions();
                return;
            } else if (option == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        try {
            String examName = examNameField.getText().trim();
            int duration = Integer.parseInt(durationField.getText().trim());
            BigDecimal totalScore = new BigDecimal(totalScoreField.getText().trim());

            if (examName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "试卷名称不能为空");
                return;
            }

            currentExam.setExamName(examName);
            currentExam.setDuration(duration);
            currentExam.setTotalScore(totalScore);
            currentExam.setExamQuestions(examQuestions);

            if (examDAO.updateExam(currentExam)) {
                JOptionPane.showMessageDialog(this, "保存成功");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "保存失败");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字");
        }
    }

    // 添加整理试题的方法
    private void organizeQuestions() {
        if (examQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "当前试卷没有题目，无需整理");
            return;
        }

        // 按题型和难度排序
        examQuestions.sort((eq1, eq2) -> {
            Question q1 = eq1.getQuestion();
            Question q2 = eq2.getQuestion();

            // 首先按题型排序
            String[] typeOrder = {"选择题", "填空题", "简答题", "计算题"};
            int type1Index = getTypeIndex(q1.getQuestionType(), typeOrder);
            int type2Index = getTypeIndex(q2.getQuestionType(), typeOrder);

            if (type1Index != type2Index) {
                return type1Index - type2Index;
            }

            // 同类型题目按难度排序
            String[] difficultyOrder = {"easy", "medium", "hard"};
            int diff1Index = getDifficultyIndex(q1.getDifficulty(), difficultyOrder);
            int diff2Index = getDifficultyIndex(q2.getDifficulty(), difficultyOrder);

            return diff1Index - diff2Index;
        });

        // 更新题目序号
        for (int i = 0; i < examQuestions.size(); i++) {
            examQuestions.get(i).setOrder(i + 1);
        }

        // 重新显示题目，包括分类标题和分数统计
        updateSelectedQuestionsTableWithCategories();

        isOrganized = true;

        JOptionPane.showMessageDialog(this, "试卷整理完成！");
    }
    // 获取题型索引
    private int getTypeIndex(String type, String[] typeOrder) {
        for (int i = 0; i < typeOrder.length; i++) {
            if (typeOrder[i].equals(type)) {
                return i;
            }
        }
        return typeOrder.length;
    }

    // 获取难度索引
    private int getDifficultyIndex(String difficulty, String[] difficultyOrder) {
        for (int i = 0; i < difficultyOrder.length; i++) {
            if (difficultyOrder[i].equals(difficulty)) {
                return i;
            }
        }
        return difficultyOrder.length;
    }

    // 更新已选题目表格，包括分类标题和分数统计
    private void updateSelectedQuestionsTableWithCategories() {
        selectedQuestionsModel.setRowCount(0);

        String currentType = null;
        BigDecimal typeTotal = BigDecimal.ZERO;

        for (int i = 0; i < examQuestions.size(); i++) {
            ExamQuestion eq = examQuestions.get(i);
            Question q = eq.getQuestion();

            // 如果题型改变，添加标题行和之前类型的总分
            if (currentType == null || !currentType.equals(q.getQuestionType())) {
                // 如果不是第一个类型，先添加之前类型的总分
                if (currentType != null) {
                    addTotalRow(currentType, typeTotal);
                    typeTotal = BigDecimal.ZERO;
                }

                // 添加新类型的标题
                currentType = q.getQuestionType();
                addTitleRow(currentType);
            }

            // 添加题目
            Object[] rowData = {
                    i + 1,
                    q.getQuestionText(),
                    getDifficultyDisplay(q.getDifficulty()),
                    eq.getScore()
            };
            selectedQuestionsModel.addRow(rowData);

            // 累加当前类型的总分
            typeTotal = typeTotal.add(eq.getScore());
        }

        // 添加最后一个类型的总分
        if (currentType != null) {
            addTotalRow(currentType, typeTotal);
        }
    }

    // 添加类型标题行
    private void addTitleRow(String type) {
        Object[] titleRow = {
                "",
                "【" + type + "】",
                "",
                ""
        };
        selectedQuestionsModel.addRow(titleRow);
    }

    // 添加总分行
    private void addTotalRow(String type, BigDecimal total) {
        Object[] totalRow = {
                "",
                type + "总分：",
                "",
                total
        };
        selectedQuestionsModel.addRow(totalRow);

        // 添加空行
        selectedQuestionsModel.addRow(new Object[]{"", "", "", ""});
    }
    // 辅助方法：将难度显示转换为中文
    private String getDifficultyDisplay(String difficulty) {
        switch (difficulty) {
            case "easy": return "简单";
            case "medium": return "中等";
            case "hard": return "困难";
            default: return difficulty;
        }
    }
}
