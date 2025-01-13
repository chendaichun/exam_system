package View;


import model.Question;
import model.Category;
import DAO.CategoryDAO;
import DAO.QuestionDAO;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ShowEditQuestionDialog extends JDialog {
    private final Color primaryColor = new Color(51, 122, 183);
    private final Font titleFont = new Font("微软雅黑", Font.BOLD, 16);
    private final Font contentFont = new Font("微软雅黑", Font.PLAIN, 14);

    private final Question question;
    private final CategoryDAO categoryDAO;
    private final QuestionDAO questionDAO;
    private final JTextArea questionTextArea;
    private final JComboBox<Category> categoryComboBox;
    private final JComboBox<String> typeComboBox;
    private final JComboBox<String> difficultyComboBox;
    private final JTextField scoreField;
    private final JTextArea answerArea;
    private String questionImagePath;
    private String answerImagePath;
    private final JLabel questionImageLabel;
    private final JLabel answerImageLabel;
    private final JFileChooser fileChooser;
    private final Runnable onQuestionUpdated;

    public ShowEditQuestionDialog(JFrame parent, Question question, CategoryDAO categoryDAO,
                                  QuestionDAO questionDAO, Runnable onQuestionUpdated) {
        super(parent, "编辑题目", true);
        this.question = question;
        this.categoryDAO = categoryDAO;
        this.questionDAO = questionDAO;
        this.onQuestionUpdated = onQuestionUpdated;

        // 初始化文件选择器
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "图片文件", "jpg", "jpeg", "png", "gif"));

        // 初始化组件
        questionTextArea = createQuestionTextArea();
        categoryComboBox = createCategoryComboBox();
        typeComboBox = new JComboBox<>(new String[]{"选择题", "填空题", "简答题", "计算题"});
        difficultyComboBox = new JComboBox<>(new String[]{"简单", "中等", "困难"});
        scoreField = new JTextField(10);
        answerArea = createAnswerTextArea();
        questionImageLabel = new JLabel("未选择图片");
        answerImageLabel = new JLabel("未选择图片");

        // 初始化已有数据
        initializeExistingData();

        // 设置布局
        initializeUI();

        // 设置窗口属性
        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeExistingData() {
        questionTextArea.setText(question.getQuestionText());

        // 设置分类
        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
            Category category = categoryComboBox.getItemAt(i);
            if (category.getCategoryId() == question.getCategoryId()) {
                categoryComboBox.setSelectedIndex(i);
                break;
            }
        }

        // 设置题目类型
        typeComboBox.setSelectedItem(question.getQuestionType());

        // 设置难度
        difficultyComboBox.setSelectedItem(convertDifficultyToDisplay(question.getDifficulty()));

        // 设置分值
        scoreField.setText(String.valueOf(question.getScore()));

        // 设置答案
        answerArea.setText(question.getAnswer());

        // 设置图片
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            questionImagePath = question.getImageUrl();
            questionImageLabel.setText(new File(questionImagePath).getName());
        }

        if (question.getAnswerImageUrl() != null && !question.getAnswerImageUrl().isEmpty()) {
            answerImagePath = question.getAnswerImageUrl();
            answerImageLabel.setText(new File(answerImagePath).getName());
        }
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 添加各部分内容
        addSection(mainPanel, "题目内容", createQuestionContentPanel());
        addSection(mainPanel, "题目图片", createImageUploadPanel("选择题目图片", questionImageLabel,
                path -> questionImagePath = path));
        addSection(mainPanel, "基本信息", createBasicInfoPanel());
        addSection(mainPanel, "答案", createAnswerPanel());
        addSection(mainPanel, "答案图片", createImageUploadPanel("选择答案图片", answerImageLabel,
                path -> answerImagePath = path));

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 添加按钮面板
        JPanel buttonPanel = createButtonPanel();

        // 设置对话框布局
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JTextArea createQuestionTextArea() {
        JTextArea area = new JTextArea(5, 30);
        area.setFont(contentFont);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JTextArea createAnswerTextArea() {
        JTextArea area = new JTextArea(3, 30);
        area.setFont(contentFont);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JComboBox<Category> createCategoryComboBox() {
        JComboBox<Category> comboBox = new JComboBox<>();
        List<Category> categories = categoryDAO.getAllCategories();
        for (Category category : categories) {
            comboBox.addItem(category);
        }
        return comboBox;
    }

    private void addSection(JPanel panel, String title, JComponent content) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BorderLayout());
        sectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(primaryColor);
        sectionPanel.add(titleLabel, BorderLayout.NORTH);

        content.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        sectionPanel.add(content, BorderLayout.CENTER);

        panel.add(sectionPanel);
    }

    private JPanel createQuestionContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(questionTextArea);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("所属分类："));
        panel.add(categoryComboBox);

        panel.add(new JLabel("题目类型："));
        panel.add(typeComboBox);

        panel.add(new JLabel("难度级别："));
        panel.add(difficultyComboBox);

        panel.add(new JLabel("分值："));
        panel.add(scoreField);

        return panel;
    }

    private JPanel createAnswerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(answerArea);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createImageUploadPanel(String buttonText, JLabel imageLabel,
                                          java.util.function.Consumer<String> pathConsumer) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton uploadButton = createStyledButton("选择图片");
        JButton previewButton = createStyledButton("预览");
        JButton clearButton = createStyledButton("清除");

        uploadButton.addActionListener(e -> {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                imageLabel.setText(file.getName());
                pathConsumer.accept(file.getAbsolutePath());
            }
        });

        previewButton.addActionListener(e -> {
            String path = imageLabel.getText().equals("未选择图片") ? null :
                    (buttonText.contains("题目") ? questionImagePath : answerImagePath);
            if (path != null) {
                showFullSizeImage(path);
            } else {
                JOptionPane.showMessageDialog(this, "没有可预览的图片");
            }
        });

        clearButton.addActionListener(e -> {
            imageLabel.setText("未选择图片");
            pathConsumer.accept(null);
        });

        panel.add(uploadButton);
        panel.add(previewButton);
        panel.add(clearButton);
        panel.add(imageLabel);
        return panel;
    }

    private void showFullSizeImage(String imagePath) {
        JDialog imageDialog = new JDialog(this, "预览图片", true);
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            JScrollPane scrollPane = new JScrollPane(imageLabel);

            imageDialog.add(scrollPane);
            imageDialog.setSize(Math.min(1024, image.getWidth() + 50),
                    Math.min(768, image.getHeight() + 50));
            imageDialog.setLocationRelativeTo(this);
            imageDialog.setVisible(true);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "图片加载失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton saveButton = createStyledButton("保存");
        JButton cancelButton = createStyledButton("取消");

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(cancelButton);
        return panel;
    }

    private void handleSave() {
        if (validateInput()) {
            updateQuestionFromInput();
            // TODO: 调用DAO的更新方法
            if (true) {  // 更新成功
                JOptionPane.showMessageDialog(this, "保存成功");
                if (onQuestionUpdated != null) {
                    onQuestionUpdated.run();
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "保存失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateInput() {
        if (questionTextArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "题目内容不能为空");
            return false;
        }

        try {
            double score = Double.parseDouble(scoreField.getText().trim());
            if (score <= 0) {
                JOptionPane.showMessageDialog(this, "分值必须大于0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "分值必须为数字");
            return false;
        }

        if (answerArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "答案不能为空");
            return false;
        }

        return true;
    }

    private void updateQuestionFromInput() {
        question.setQuestionText(questionTextArea.getText().trim());
        question.setQuestionType((String) typeComboBox.getSelectedItem());
        question.setDifficulty(convertDifficultyToDatabase((String) difficultyComboBox.getSelectedItem()));
        question.setScore(Double.parseDouble(scoreField.getText().trim()));
        question.setAnswer(answerArea.getText().trim());
        question.setImageUrl(questionImagePath);
        question.setAnswerImageUrl(answerImagePath);

        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        question.setCategoryId(selectedCategory.getCategoryId());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(contentFont);
        button.setForeground(Color.WHITE);
        button.setBackground(primaryColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 30));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
            }
        });
        return button;
    }

    private String convertDifficultyToDisplay(String difficulty) {
        return switch (difficulty) {
            case "easy" -> "简单";
            case "medium" -> "中等";
            case "hard" -> "困难";
            default -> difficulty;
        };
    }

    private String convertDifficultyToDatabase(String displayDifficulty) {
        return switch (displayDifficulty) {
            case "简单" -> "easy";
            case "中等" -> "medium";
            case "困难" -> "hard";
            default -> displayDifficulty;
        };
    }
}
