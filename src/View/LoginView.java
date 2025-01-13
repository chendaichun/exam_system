package View;

import DAO.UserDAO;
import Util.DatabaseUtil;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;

import static java.lang.System.exit;

public class LoginView extends JFrame {
    private final Color PRIMARY_COLOR = new Color(51, 122, 183);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Font TITLE_FONT = new Font("微软雅黑", Font.BOLD, 28);
    private final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private final Font INPUT_FONT = new Font("微软雅黑", Font.PLAIN, 14);

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginView() {
        initializeFrame();
        createComponents();
        setVisible(true);
    }

    private void initializeFrame() {
        setTitle("考试组卷系统");
        setSize(400, 500);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit(0);
            }
        });
        setLocationRelativeTo(null);
        setBackground(BACKGROUND_COLOR);
        setResizable(false);
        setLayout(new BorderLayout());
    }

    private void createComponents() {
        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout(0, 30));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // 标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBackground(BACKGROUND_COLOR);
        JLabel titleLabel = new JLabel("考试组卷系统");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        titlePanel.add(titleLabel);

        // 表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        // 用户名输入
        JLabel usernameLabel = new JLabel("用户名");
        usernameLabel.setFont(LABEL_FONT);
        usernameField = createStyledTextField();

        // 密码输入
        JLabel passwordLabel = new JLabel("密码");
        passwordLabel.setFont(LABEL_FONT);
        passwordField = createStyledPasswordField();

        // 登录按钮
        JButton loginButton = createStyledButton("登录");

        // 添加组件到表单
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);
        gbc.gridy = 1;
        formPanel.add(usernameField, gbc);
        gbc.gridy = 2;
        formPanel.add(Box.createVerticalStrut(10), gbc);
        gbc.gridy = 3;
        formPanel.add(passwordLabel, gbc);
        gbc.gridy = 4;
        formPanel.add(passwordField, gbc);
        gbc.gridy = 5;
        formPanel.add(Box.createVerticalStrut(30), gbc);
        gbc.gridy = 6;
        formPanel.add(loginButton, gbc);

        // 组装主面板
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 添加到窗口
        add(mainPanel);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(INPUT_FONT);
        field.setPreferredSize(new Dimension(300, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(INPUT_FONT);
        field.setPreferredSize(new Dimension(300, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setPreferredSize(new Dimension(300, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(40, 96, 144));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        // 添加登录事件处理
        button.addActionListener(e -> handleLogin());

        return button;
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "用户名和密码不能为空！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserByUsernameAndPassword(username, password);

        if (user != null) {
            dispose();
            new MainView(user);
        } else {
            try {
                Connection conn = DatabaseUtil.getConnection();
                JOptionPane.showMessageDialog(this,
                        "用户名或密码错误！",
                        "登录失败",
                        JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "数据库连接错误：" + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginView::new);
    }
}