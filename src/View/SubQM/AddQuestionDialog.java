package View.SubQM;

import DAO.CategoryDAO;
import DAO.QuestionDAO;
import model.Category;
import model.Question;
import model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class AddQuestionDialog extends JDialog {
    private Color primaryColor = new Color(51, 122, 183);
    private Font defaultFont = new Font("微软雅黑", Font.PLAIN, 14);
    private final JTextArea questionText;
    private final JComboBox<Category> categoryComboBox;
    private final JComboBox<String> typeComboBox;
    private final JComboBox<String> difficultyComboBox;
    private final JTextField scoreField;
    private final JTextArea answerArea;
    private final CategoryDAO categoryDAO;
    private final QuestionDAO questionDAO;
    private final User currentUser;
    private final Runnable onQuestionAdded;

    private String questionImagePath;
    private String answerImagePath;
    private JLabel questionImageLabel;
    private JLabel answerImageLabel;
    private JFileChooser fileChooser;

    public AddQuestionDialog(JFrame parent, CategoryDAO categoryDAO, QuestionDAO questionDAO,
                             User currentUser, Runnable onQuestionAdded) {
        super(parent, "添加题目", true);
        this.categoryDAO = categoryDAO;
        this.questionDAO = questionDAO;
        this.currentUser = currentUser;
        this.onQuestionAdded = onQuestionAdded;

        // 初始化文件选择器
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "gif"));

        // 初始化组件
        questionText = createQuestionTextArea();
        categoryComboBox = createCategoryComboBox();
        typeComboBox = createStyledComboBox(new String[]{"选择题", "填空题", "简答题", "计算题"});
        difficultyComboBox = createStyledComboBox(new String[]{"简单", "中等", "困难"});
        scoreField = createStyledTextField();
        answerArea = createAnswerTextArea();

        // 初始化图片标签
        questionImageLabel = new JLabel("未选择图片");
        answerImageLabel = new JLabel("未选择图片");

        // Setup layout
        setupLayout();

        // Set dialog
        pack();
        setLocationRelativeTo(parent);
    }

    private JTextArea createQuestionTextArea() {
        JTextArea area = new JTextArea(5, 30);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private JTextArea createAnswerTextArea() {
        JTextArea area = new JTextArea(3, 30);
        area.setLineWrap(true);
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

    private void setupLayout() {
        JPanel formPanel = createFormPanel();
        JPanel buttonPanel = createButtonPanel();

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }



    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, Component component, int gridy) {
        gbc.gridy = gridy;
        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }



    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(defaultFont);
        button.setForeground(Color.WHITE);
        button.setBackground(primaryColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 30));

        // 添加鼠标悬停效果
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


    private void handleConfirm() {
        if (validateInput()) {
            Question question = createQuestionFromInput();
            if (questionDAO.addQuestion(question)) {
                JOptionPane.showMessageDialog(this, "添加成功");
                onQuestionAdded.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "添加失败");
            }
        }
    }

    private boolean validateInput() {
        String questionContent = questionText.getText().trim();
        String scoreText = scoreField.getText().trim();
        String answerContent = answerArea.getText().trim();

        if (questionContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入题目内容");
            return false;
        }

        if (scoreText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入分值");
            return false;
        }

        try {
            double score = Double.parseDouble(scoreText);
            if (score <= 0) {
                JOptionPane.showMessageDialog(this, "分值必须大于0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "分值必须为数字");
            return false;
        }

        if (answerContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入答案");
            return false;
        }

        return true;
    }




    private JTextField createStyledTextField() {
        JTextField field = new JTextField(10);
        field.setFont(defaultFont);
        return field;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(defaultFont);
        return comboBox;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int gridy = 0;
        addFormField(formPanel, gbc, "题目内容：", new JScrollPane(questionText), gridy++);

        // 添加题目图片上传
        JPanel questionImagePanel = createImageUploadPanel("题目图片", questionImageLabel,
                path -> questionImagePath = path);
        addFormField(formPanel, gbc, "题目图片：", questionImagePanel, gridy++);

        addFormField(formPanel, gbc, "所属分类：", categoryComboBox, gridy++);
        addFormField(formPanel, gbc, "题目类型：", typeComboBox, gridy++);
        addFormField(formPanel, gbc, "难度级别：", difficultyComboBox, gridy++);
        addFormField(formPanel, gbc, "分值：", scoreField, gridy++);
        addFormField(formPanel, gbc, "答案：", new JScrollPane(answerArea), gridy++);

        // 添加答案图片上传
        JPanel answerImagePanel = createImageUploadPanel("答案图片", answerImageLabel,
                path -> answerImagePath = path);
        addFormField(formPanel, gbc, "答案图片：", answerImagePanel, gridy++);

        return formPanel;
    }

    private JPanel createImageUploadPanel(String buttonText, JLabel imageLabel,
                                          Consumer<String> pathConsumer) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton uploadButton = createStyledButton("选择图片");

        uploadButton.addActionListener(e -> {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String path = file.getAbsolutePath();
                imageLabel.setText(file.getName());
                pathConsumer.accept(path);
            }
        });

        panel.add(uploadButton);
        panel.add(imageLabel);
        return panel;
    }

    private JButton createPreviewButton() {
        JButton previewButton = createStyledButton("预览");
        previewButton.addActionListener(e -> showPreviewDialog());
        return previewButton;
    }

    private void showPreviewDialog() {
        JDialog previewDialog = new JDialog(this, "预览题目", true);
        previewDialog.setLayout(new BorderLayout(10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 使用表格布局显示基本信息
        JPanel basicInfoPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        basicInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        basicInfoPanel.add(new JLabel("题目类型："));
        basicInfoPanel.add(new JLabel((String) typeComboBox.getSelectedItem()));

        basicInfoPanel.add(new JLabel("所属分类："));
        basicInfoPanel.add(new JLabel(categoryComboBox.getSelectedItem().toString()));

        basicInfoPanel.add(new JLabel("难度级别："));
        basicInfoPanel.add(new JLabel((String) difficultyComboBox.getSelectedItem()));

        basicInfoPanel.add(new JLabel("分值："));
        basicInfoPanel.add(new JLabel(scoreField.getText()));

        contentPanel.add(basicInfoPanel);

        // 题目内容
        addPreviewField(contentPanel, "题目内容：", questionText.getText());

        // 题目图片
        if (questionImagePath != null) {
            addImagePreview(contentPanel, "题目图片：", questionImagePath);
        }

        // 答案内容
        addPreviewField(contentPanel, "答案内容：", answerArea.getText());

        // 答案图片
        if (answerImagePath != null) {
            addImagePreview(contentPanel, "答案图片：", answerImagePath);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        previewDialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = createStyledButton("关闭");
        closeButton.addActionListener(evt -> previewDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        previewDialog.add(buttonPanel, BorderLayout.SOUTH);

        previewDialog.setSize(600, 800);
        previewDialog.setLocationRelativeTo(this);
        previewDialog.setVisible(true);
    }

    private void addImagePreview(JPanel panel, String label, String imagePath) {
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        imagePanel.add(new JLabel(label), BorderLayout.NORTH);

        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            // 计算缩放比例
            int maxWidth = 400;
            double scale = (double) maxWidth / originalImage.getWidth();
            if (scale >= 1) {
                scale = 1;
            }

            int scaledWidth = (int) (originalImage.getWidth() * scale);
            int scaledHeight = (int) (originalImage.getHeight() * scale);

            Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

            imagePanel.add(imageLabel, BorderLayout.CENTER);
        } catch (IOException e) {
            JLabel errorLabel = new JLabel("图片加载失败: " + imagePath);
            errorLabel.setForeground(Color.RED);
            imagePanel.add(errorLabel, BorderLayout.CENTER);
        }

        panel.add(imagePanel);
        panel.add(Box.createVerticalStrut(10));
    }

    private void addPreviewField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel labelComponent = new JLabel(label);
        fieldPanel.add(labelComponent, BorderLayout.NORTH);

        JTextArea valueArea = new JTextArea(value);
        valueArea.setWrapStyleWord(true);
        valueArea.setLineWrap(true);
        valueArea.setEditable(false);
        valueArea.setBackground(null);
        valueArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        fieldPanel.add(valueArea, BorderLayout.CENTER);
        panel.add(fieldPanel);
        panel.add(Box.createVerticalStrut(5));
    }

    protected JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton previewButton = createPreviewButton();
        JButton confirmButton = createStyledButton("确定");
        JButton cancelButton = createStyledButton("取消");

        confirmButton.addActionListener(e -> handleConfirm());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(previewButton);
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }


    private Question createQuestionFromInput() {
        Question question = new Question();
        question.setQuestionText(questionText.getText().trim());
        question.setQuestionType((String) typeComboBox.getSelectedItem());
        question.setDifficulty(convertDifficulty((String) difficultyComboBox.getSelectedItem()));
        question.setScore(Double.parseDouble(scoreField.getText().trim()));
        question.setAnswer(answerArea.getText().trim());

        // 设置图片路径
        if (questionImagePath != null) {
            question.setImageUrl(questionImagePath);
        }
        if (answerImagePath != null) {
            question.setAnswerImageUrl(answerImagePath);
        }

        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        question.setCategoryId(selectedCategory.getCategoryId());
        question.setCreatedBy(currentUser.getUserId());

        return question;
    }

    //转换难度等级
    private String convertDifficulty(String displayDifficulty) {
        switch (displayDifficulty) {
            case "简单": return "easy";
            case "中等": return "medium";
            case "困难": return "hard";
            default: return "medium";
        }
    }
}
