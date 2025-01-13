package View;

import DAO.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class UserManagementView extends JFrame {
    private DefaultTableModel tableModel;
    private JTable userTable;
    private UserDAO userDAO;
    private Frame parentFrame;
    // 定义界面主要颜色
    private Color primaryColor = new Color(51, 122, 183);  // 主要颜色：蓝色
    private Color backgroundColor = new Color(240, 242, 245);  // 背景颜色：浅灰色
    // 定义字体样式
    private Font titleFont = new Font("微软雅黑", Font.BOLD, 16);  // 标题字体
    private Font defaultFont = new Font("微软雅黑", Font.PLAIN, 14);  // 默认字体
    private Font tableFont = new Font("微软雅黑", Font.PLAIN, 13);  // 表格字体

    public UserManagementView(Frame parentFrame) {
        this.parentFrame = parentFrame;
        userDAO = new UserDAO();
        initComponents();
        loadUserData();
        setupStyle();
    }

    private void setupStyle() {
        // 设置窗口背景色
        getContentPane().setBackground(backgroundColor);

        // 应用自定义样式到所有组件
        SwingUtilities.invokeLater(() -> {
            setComponentStyles(this);
        });
    }


    private void setComponentStyles(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton) {
                JButton button = (JButton) c;
                button.setFont(defaultFont);
                button.setForeground(Color.WHITE);
                button.setBackground(primaryColor);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else if (c instanceof JLabel) {
                ((JLabel) c).setFont(defaultFont);
            } else if (c instanceof Container) {
                setComponentStyles((Container) c);
            }
        }
    }

    private void initComponents() {
        setTitle("用户管理系统");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        // 添加窗口关闭监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (parentFrame != null) {
                    parentFrame.setVisible(true);
                }
            }
        });

        // 创建主面板并设置内边距
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建工具栏
        JToolBar toolBar = createToolBar();

        // 创建表格
        createTable();
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        // 添加组件到主面板
        mainPanel.add(toolBar, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 设置布局
        setLayout(new BorderLayout());
        add(mainPanel);
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(backgroundColor);
        toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 创建带样式的按钮
        JButton addButton = createStyledButton("添加用户", "add_user.png");
        JButton editButton = createStyledButton("编辑用户", "edit_user.png");
        JButton deleteButton = createStyledButton("删除用户", "delete_user.png");

        // 添加按钮事件监听器
        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> showEditUserDialog());
        deleteButton.addActionListener(e -> deleteSelectedUser());

        // 添加按钮到工具栏，并设置间距
        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteButton);

        return toolBar;
    }
    private JButton createStyledButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(defaultFont);
        button.setPreferredSize(new Dimension(100, 35));
        button.setForeground(Color.WHITE);
        button.setBackground(primaryColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

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

    private void createTable() {
        String[] columnNames = {"ID", "用户名", "角色", "邮箱", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        // 设置表格样式
        userTable.setFont(tableFont);
        userTable.setRowHeight(30);
        userTable.setShowGrid(true);
        userTable.setGridColor(new Color(230, 230, 230));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setSelectionBackground(new Color(232, 241, 249));
        userTable.setSelectionForeground(Color.BLACK);

        // 设置表格表头样式
        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
    }


    private void loadUserData() {
        List<User> users = userDAO.getAllUsers();
        tableModel.setRowCount(0);

        for (User user : users) {
            Object[] rowData = {
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getEmail(),
                    user.getCreatedAt()
            };
            tableModel.addRow(rowData);
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog(this, "添加用户", true);
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加表单字段
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"teacher", "admin"});
        JTextField emailField = new JTextField();

        formPanel.add(new JLabel("用户名："));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("密码："));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("角色："));
        formPanel.add(roleComboBox);
        formPanel.add(new JLabel("邮箱："));
        formPanel.add(emailField);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");

        confirmButton.addActionListener(e -> {
            if (addUser(usernameField.getText(), new String(passwordField.getPassword()),
                    (String) roleComboBox.getSelectedItem(), emailField.getText())) {
                dialog.dispose();
                loadUserData();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        // 设置对话框布局
        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean addUser(String username, String password, String role, String email) {
        if (username.trim().isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空");
            return false;
        }

        if (userDAO.isUsernameExists(username)) {
            JOptionPane.showMessageDialog(this, "用户名已存在");
            return false;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setEmail(email);

        if (userDAO.addUser(user)) {
            JOptionPane.showMessageDialog(this, "添加成功");
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "添加失败");
            return false;
        }
    }

    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要编辑的用户");
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);
        User user = userDAO.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "未找到用户信息");
            return;
        }

        // 创建编辑对话框
        JDialog dialog = new JDialog(this, "编辑用户", true);
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField usernameField = new JTextField(user.getUsername());
        usernameField.setEnabled(false);
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"teacher", "admin"});
        roleComboBox.setSelectedItem(user.getRole());
        roleComboBox.setEnabled(false);
        JTextField emailField = new JTextField(user.getEmail());

        formPanel.add(new JLabel("用户名："));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("新密码："));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("角色："));
        formPanel.add(roleComboBox);
        formPanel.add(new JLabel("邮箱："));
        formPanel.add(emailField);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = new JButton("确定");
        JButton cancelButton = new JButton("取消");

        confirmButton.addActionListener(e -> {
            if (updateUser(user, new String(passwordField.getPassword()), emailField.getText())) {
                dialog.dispose();
                loadUserData();
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean updateUser(User user, String newPassword, String newEmail) {
        user.setEmail(newEmail);
        if (!newPassword.isEmpty()) {
            user.setPassword(newPassword);
        }

        if (userDAO.updateUser(user)) {
            JOptionPane.showMessageDialog(this, "更新成功");
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "更新失败");
            return false;
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的用户");
            return;
        }

        int userId = (int) userTable.getValueAt(selectedRow, 0);

        // 首先检查用户是否创建了试卷
        if (userDAO.hasCreatedExams(userId)) {
            JOptionPane.showMessageDialog(this,
                    "该用户已创建试卷，不能删除！\n删除用户前请先删除该用户创建的所有试卷。",
                    "删除失败",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int option = JOptionPane.showConfirmDialog(this,
                "确定要删除该用户吗？", "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(userId)) {
                JOptionPane.showMessageDialog(this, "删除成功");
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败");
            }
        }
    }
}