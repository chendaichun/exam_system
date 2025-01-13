package View.SubQM;

import model.Question;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ShowQuestionDetail extends JDialog {
    private Color primaryColor = new Color(51, 122, 183);
    private Font titleFont = new Font("微软雅黑", Font.BOLD, 16);
    private Font contentFont = new Font("微软雅黑", Font.PLAIN, 14);
    private final Question question;

    public ShowQuestionDetail(JFrame parent, Question question) {
        super(parent, "题目详情", true);
        this.question = question;

        initializeUI();
        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        // 创建主面板，使用BoxLayout实现垂直布局
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 添加题目基本信息
        addSection(mainPanel, "基本信息", createBasicInfoPanel());

        // 添加题目内容
        addSection(mainPanel, "题目内容", createQuestionContentPanel());

        // 添加题目图片（如果有）
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            addSection(mainPanel, "题目图片", createImagePanel(question.getImageUrl()));
        }

        // 添加答案内容
        addSection(mainPanel, "答案", createAnswerPanel());

        // 添加答案图片（如果有）
        if (question.getAnswerImageUrl() != null && !question.getAnswerImageUrl().isEmpty()) {
            addSection(mainPanel, "答案图片", createImagePanel(question.getAnswerImageUrl()));
        }

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 添加关闭按钮
        JPanel buttonPanel = createButtonPanel();

        // 设置对话框布局
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addSection(JPanel panel, String title, JComponent content) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BorderLayout());
        sectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // 创建标题标签
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(primaryColor);
        sectionPanel.add(titleLabel, BorderLayout.NORTH);

        // 添加内容
        content.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        sectionPanel.add(content, BorderLayout.CENTER);

        panel.add(sectionPanel);
    }

    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // 添加基本信息字段
        addInfoField(panel, "题目类型：", question.getQuestionType());
        addInfoField(panel, "难度级别：", convertDifficultyToString(question.getDifficulty()));
        addInfoField(panel, "分值：", String.valueOf(question.getScore()));

        return panel;
    }

    private void addInfoField(JPanel panel, String label, String value) {
        JLabel labelComponent = new JLabel(label);
        JLabel valueComponent = new JLabel(value);
        labelComponent.setFont(contentFont);
        valueComponent.setFont(contentFont);
        panel.add(labelComponent);
        panel.add(valueComponent);
    }

    private JPanel createQuestionContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(question.getQuestionText());
        textArea.setFont(contentFont);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(null);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAnswerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(question.getAnswer());
        textArea.setFont(contentFont);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setBackground(null);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createImagePanel(String imagePath) {
        JPanel panel = new JPanel(new BorderLayout());
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));

            // 计算缩放比例（最大宽度为600像素）
            int maxWidth = 600;
            double scale = (double) maxWidth / originalImage.getWidth();
            if (scale >= 1) {
                scale = 1; // 如果图片本身较小，则不放大
            }

            // 计算缩放后的尺寸
            int scaledWidth = (int) (originalImage.getWidth() * scale);
            int scaledHeight = (int) (originalImage.getHeight() * scale);

            // 创建缩放后的图片
            Image scaledImage = originalImage.getScaledInstance(
                    scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

            // 创建可点击放大的标签
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    showFullSizeImage(imagePath);
                }
            });

            imageLabel.setToolTipText("点击查看原始大小");
            panel.add(imageLabel, BorderLayout.CENTER);

        } catch (IOException e) {
            JLabel errorLabel = new JLabel("图片加载失败: " + imagePath);
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.CENTER);
        }
        return panel;
    }

    private void showFullSizeImage(String imagePath) {
        JDialog imageDialog = new JDialog(this, "原始图片", true);
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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("关闭");
        styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());
        panel.add(closeButton);
        return panel;
    }

    private void styleButton(JButton button) {
        button.setFont(contentFont);
        button.setForeground(Color.WHITE);
        button.setBackground(primaryColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 35));

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
            }
        });
    }

    private String convertDifficultyToString(String difficulty) {
        return switch (difficulty) {
            case "easy" -> "简单";
            case "medium" -> "中等";
            case "hard" -> "困难";
            default -> difficulty;
        };
    }
}