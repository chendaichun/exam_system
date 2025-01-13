package View;

import DAO.CategoryDAO;
import DAO.QuestionDAO;
import model.Category;
import model.Question;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class QuestionManagementView extends JFrame {
    private DefaultTableModel tableModel;
    private JTable questionTable;
    private JTree categoryTree;
    private QuestionDAO questionDAO;
    private CategoryDAO categoryDAO;
    private Frame parentFrame;
    private User currentUser;

    // 定义界面主要颜色和字体
    private Color primaryColor = new Color(51, 122, 183);
    private Color backgroundColor = new Color(240, 242, 245);
    private Font titleFont = new Font("微软雅黑", Font.BOLD, 16);
    private Font defaultFont = new Font("微软雅黑", Font.PLAIN, 14);
    private Font tableFont = new Font("微软雅黑", Font.PLAIN, 13);

    public QuestionManagementView(Frame parentFrame, User user) {
        this.parentFrame = parentFrame;
        this.currentUser = user;
        questionDAO = new QuestionDAO();
        categoryDAO = new CategoryDAO();
        initComponents();
        loadQuestionData();
        //setupStyle();
    }
    public QuestionManagementView() {
        questionDAO = new QuestionDAO();
        categoryDAO = new CategoryDAO();
        initComponents();
        loadQuestionData();
        //setupStyle();
    }

    private void initComponents() {
        setTitle("题库管理系统");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        // 添加窗口关闭监听器
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
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建左侧分类树面板
        JPanel leftPanel = createLeftPanel();

        // 创建右侧内容面板
        JPanel rightPanel = createRightPanel();

        // 创建分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(5);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(backgroundColor);

        // 创建分类树标题
        JLabel titleLabel = new JLabel("题目分类", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 创建分类树
        categoryTree = new JTree(createCategoryTreeModel());
        categoryTree.setFont(defaultFont);
        JScrollPane treeScrollPane = new JScrollPane(categoryTree);

        // 创建分类管理按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setBackground(backgroundColor);

        JButton addCategoryBtn = createStyledButton("添加分类");
        JButton editCategoryBtn = createStyledButton("编辑分类");
        JButton deleteCategoryBtn = createStyledButton("删除分类");

        buttonPanel.add(addCategoryBtn);
        buttonPanel.add(editCategoryBtn);
        buttonPanel.add(deleteCategoryBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(treeScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建右侧内容面板
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(backgroundColor);

        // 创建工具栏
        JToolBar toolBar = createToolBar();

        // 创建搜索面板
        JPanel searchPanel = createSearchPanel();

        // 创建题目表格
        createQuestionTable();
        JScrollPane tableScrollPane = new JScrollPane(questionTable);

        // 添加组件到面板
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.CENTER);
        panel.add(tableScrollPane, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建工具栏
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(backgroundColor);
        toolBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 创建按钮
        JButton addButton = createStyledButton("添加题目");
        JButton editButton = createStyledButton("编辑题目");
        JButton deleteButton = createStyledButton("删除题目");
        JButton showButton = createStyledButton("显示题目详情");


        // 添加事件监听器
        addButton.addActionListener(e -> showAddQuestionDialog()); // 添加题目
        editButton.addActionListener(e -> showEditQuestionDialog());
        deleteButton.addActionListener(e -> deleteSelectedQuestion()); // 删除题目
        showButton.addActionListener(e -> showQuestionDetail());

        // 添加按钮到工具栏
        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(showButton);

        return toolBar;
    }

    /**
     * 创建搜索面板
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(backgroundColor);

        // 添加搜索条件组件
        panel.add(new JLabel("关键词："));
        panel.add(new JTextField(20));

        panel.add(new JLabel("题型："));
        panel.add(new JComboBox<>(new String[]{"全部", "选择题", "填空题", "简答题", "计算题"}));

        panel.add(new JLabel("难度："));
        panel.add(new JComboBox<>(new String[]{"全部", "简单", "中等", "困难"}));

        JButton searchButton = createStyledButton("搜索");
        panel.add(searchButton);

        return panel;
    }

    /**
     * 创建题目表格
     */
    private void createQuestionTable() {
        String[] columnNames = {"ID", "题目内容", "题型", "难度", "分值", "所属分类", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        questionTable = new JTable(tableModel);
        setupTableStyle(questionTable);
    }

    /**
     * 设置表格样式
     */
    private void setupTableStyle(JTable table) {
        table.setFont(tableFont);
        table.setRowHeight(30);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(232, 241, 249));
        table.setSelectionForeground(Color.BLACK);

        // 设置表头样式
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
    }

    /**
     * 创建分类树模型
     */
    private DefaultTreeModel createCategoryTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("全部分类");
        List<Category> categories = categoryDAO.getAllCategories();

        // 构建分类树
        for (Category category : categories) {
            if (category.getParentId() == null) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
                buildCategoryTree(node, categories);
                root.add(node);
            }
        }

        return new DefaultTreeModel(root);
    }

    /**
     * 递归构建分类树
     */
    private void buildCategoryTree(DefaultMutableTreeNode parentNode, List<Category> categories) {
        Category parent = (Category) parentNode.getUserObject();
        for (Category category : categories) {
            if (category.getParentId() != null && category.getParentId().equals(parent.getCategoryId())) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
                buildCategoryTree(node, categories);
                parentNode.add(node);
            }
        }
    }

    /**
     * 显示添加分类对话框
     */
    private void showAddCategoryDialog() {
        JDialog dialog = new JDialog(this, "添加分类", true);
        //styleDialog(dialog, "添加新分类");

        //JPanel formPanel = createStyledFormPanel();
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 添加表单字段
        JTextField nameField = new JTextField(20);
        JComboBox<Category> parentComboBox = new JComboBox<>();
        parentComboBox.addItem(null);  // 添加一个空选项作为顶级分类
        for (Category category : categoryDAO.getAllCategories()) {
            parentComboBox.addItem(category);
        }

        // 添加到面板
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("分类名称："), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("父级分类："), gbc);
        gbc.gridx = 1;
        formPanel.add(parentComboBox, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton confirmButton = createStyledButton("确定");
        JButton cancelButton = createStyledButton("取消");

        confirmButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "分类名称不能为空");
                return;
            }

            Category parent = (Category) parentComboBox.getSelectedItem();
            Category newCategory = new Category();
            newCategory.setCategoryName(name);
            newCategory.setParentId(parent == null ? null : parent.getCategoryId());

            if (categoryDAO.addCategory(newCategory)) {
                JOptionPane.showMessageDialog(dialog, "添加成功");
                refreshCategoryTree();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "添加失败");
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    //显示添加题目对话框
    private void showAddQuestionDialog() {
        AddQuestionDialog dialog = new AddQuestionDialog(
                this,           // 父窗口
                categoryDAO,    // 分类DAO
                questionDAO,    // 题目DAO
                currentUser,    // 当前用户
                this::loadQuestionData  // 刷新数据的回调方法
        );
        dialog.setVisible(true);
    }

    // 显示题目详情
    private void showQuestionDetail() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要查看的题目");
            return;
        }
        int questionId = (int) questionTable.getValueAt(selectedRow, 0);
        Question q = questionDAO.getQuestionById(questionId);
        ShowQuestionDetail dialog = new ShowQuestionDetail(this, q);
        dialog.setVisible(true);
    }

    private void showEditQuestionDialog() {
        new ShowEditQuestionDialog(this, );
    }

    // 刷新分类树
    private void refreshCategoryTree() {
        categoryTree.setModel(createCategoryTreeModel());
    }

    // 加载题目数据
    private void loadQuestionData() {
        List<Question> questions = questionDAO.getAllQuestions();
        tableModel.setRowCount(0);

        for (Question question : questions) {
            Object[] rowData = {
                    question.getQuestionId(),
                    question.getQuestionText(),
                    question.getQuestionType(),
                    getDifficultyDisplay(question.getDifficulty()),
                    question.getScore(),
                    getCategoryName(question.getCategoryId()),
                    question.getCreatedAt()
            };
            tableModel.addRow(rowData);
        }
    }

    // 获取难度等级显示文本
    private String getDifficultyDisplay(String difficulty) {
        switch (difficulty) {
            case "easy": return "简单";
            case "medium": return "中等";
            case "hard": return "困难";
            default: return difficulty;
        }
    }

    /**
     * 获取分类名称
     */
    private String getCategoryName(int categoryId) {
        Category category = categoryDAO.getCategoryById(categoryId);
        return category != null ? category.getCategoryName() : "";
    }

    /**
     * 删除选中的题目
     */
    private void deleteSelectedQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的题目");
            return;
        }

        int questionId = (int) questionTable.getValueAt(selectedRow, 0);

        int option = JOptionPane.showConfirmDialog(this,
                "确定要删除该题目吗？", "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            if (questionDAO.deleteQuestion(questionId)) {
                JOptionPane.showMessageDialog(this, "删除成功");
                loadQuestionData();
            } else {
                JOptionPane.showMessageDialog(this, "题目已经被试卷使用，删除失败");
            }
        }
    }


    // 创建统一样式的按钮
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

    public static void main(String[] args) {
        new QuestionManagementView().setVisible(true);
    }
}