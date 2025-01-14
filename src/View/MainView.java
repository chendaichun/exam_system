package View;

import model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainView extends JFrame {
    private User currentUser;
    private final Color PRIMARY_COLOR = new Color(51, 122, 183);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color BUTTON_HOVER_COLOR = new Color(40, 96, 144);
    private final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 24);
    private final Font BUTTON_FONT = new Font("微软雅黑", Font.PLAIN, 16);

    public MainView(User user) {
        this.currentUser = user;
        initializeFrame();
        createComponents();
        setVisible(true);
    }

    private void initializeFrame() {
        setTitle("考试组卷系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 20));
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void createComponents() {
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(PRIMARY_COLOR);
        topPanel.setPreferredSize(new Dimension(getWidth(), 80));
        topPanel.setBorder(new EmptyBorder(15, 25, 15, 25));

        JLabel titleLabel = new JLabel("考试组卷系统");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("欢迎，" + currentUser.getUsername());
        welcomeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        welcomeLabel.setForeground(Color.WHITE);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(welcomeLabel, BorderLayout.EAST);
        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // 添加功能按钮
        if (currentUser.getRole().equals("admin")) {
            centerPanel.add(createMenuButton("用户管理", e -> showUserManagement()), gbc);
        }
        centerPanel.add(createMenuButton("题库管理", e -> showQuestionManagement()), gbc);
        centerPanel.add(createMenuButton("组卷管理", e -> showExamManagement()), gbc);

        return centerPanel;
    }

    private JButton createMenuButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(new Dimension(300, 50));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BUTTON_HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        button.addActionListener(action);
        return button;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.setBorder(new EmptyBorder(10, 25, 20, 25));

        JButton logoutButton = new JButton("退出登录");
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        logoutButton.setForeground(PRIMARY_COLOR);
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());

        bottomPanel.add(logoutButton);
        return bottomPanel;
    }

    private void showUserManagement() {
        // TODO: 实现用户管理功能
        this.setVisible(false);
        new UserManagementView(this).setVisible(true);
        //JOptionPane.showMessageDialog(this, "用户管理功能即将开放");
    }

    private void showQuestionManagement() {
        // TODO: 实现题库管理功能
        this.setVisible(false);
        new QuestionManagementView(this, currentUser).setVisible(true);
        //JOptionPane.showMessageDialog(this, "题库管理功能即将开放");
    }

    private void showExamManagement() {
        // TODO: 实现组卷管理功能
        new ExamManagementView(this, currentUser).setVisible(true);
        //JOptionPane.showMessageDialog(this, "组卷管理功能即将开放");
    }

    private void logout() {
        dispose();
        new LoginView();
    }
}