package View;

import DAO.CategoryDAO;
import DAO.QuestionDAO;
import View.SubQM.*;
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
    private JTextField searchKeywordField;
    private JComboBox<String> questionTypeCombo;
    private JComboBox<String> difficultyCombo;

    // 定义界面主要颜色和字体
    private Color primaryColor = new Color(51, 122, 183);
    private Color backgroundColor = new Color(240, 242, 245);
    private Font titleFont = new Font("微软雅黑", Font.BOLD, 16);
    private Font defaultFont = new Font("微软雅黑", Font.PLAIN, 14);
    private Font tableFont = new Font("微软雅黑", Font.PLAIN, 13);
    private Font litleFont = new Font("微软雅黑", Font.PLAIN, 10);

    public QuestionManagementView(Frame parentFrame, User user) {
        this.parentFrame = parentFrame;
        this.currentUser = user;
        questionDAO = new QuestionDAO();
        categoryDAO = new CategoryDAO();
        initComponents();
        loadQuestionData();
    }

    // 测试用的狗叫函数
    public QuestionManagementView() {
        questionDAO = new QuestionDAO();
        categoryDAO = new CategoryDAO();
        initComponents();
        loadQuestionData();
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

    // 左侧面板
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(backgroundColor);

        // 创建分类树标题
        JLabel titleLabel = new JLabel("题目分类", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 在createLeftPanel方法中,创建分类树后添加监听器
        categoryTree = new JTree(createCategoryTreeModel());
        categoryTree.setFont(defaultFont);
        categoryTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    categoryTree.getLastSelectedPathComponent();
            if (node != null) {
                if (node.getUserObject() instanceof Category) {
                    Category selectedCategory = (Category) node.getUserObject();
                    filterQuestionsByCategory(selectedCategory.getCategoryId());
                } else if (node.getUserObject().equals("全部分类")) {
                    loadQuestionData(); // 显示所有题目
                }
            }
        });
        JScrollPane treeScrollPane = new JScrollPane(categoryTree);

        // 创建分类管理按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(3,1));
        buttonPanel.setBackground(backgroundColor);

        JButton addCategoryBtn = createStyledButton("添加分类");
        JButton editCategoryBtn = createStyledButton("编辑分类");
        JButton deleteCategoryBtn = createStyledButton("删除分类");

        buttonPanel.add(addCategoryBtn);
        buttonPanel.add(editCategoryBtn);
        buttonPanel.add(deleteCategoryBtn);

        addCategoryBtn.addActionListener(e -> {
            AddCategoryDialog dialog = new AddCategoryDialog(
                    this,
                    categoryDAO,
                    () -> {
                        DefaultTreeModel model = createCategoryTreeModel();
                        categoryTree.setModel(model);
                    }
            );
            dialog.setVisible(true);
        });

        editCategoryBtn.addActionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    categoryTree.getLastSelectedPathComponent();
            if (node == null || !(node.getUserObject() instanceof Category)) {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的分类");
                return;
            }

            Category category = (Category) node.getUserObject();
            EditCategoryDialog dialog = new EditCategoryDialog(
                    this,
                    categoryDAO,
                    category,
                    () -> {
                        DefaultTreeModel model = createCategoryTreeModel();
                        categoryTree.setModel(model);
                    }
            );
            dialog.setVisible(true);
        });
        // 在 createLeftPanel 方法中添加删除按钮的事件处理
        deleteCategoryBtn.addActionListener(e -> deleteCategory());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(treeScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // 右侧面板
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(backgroundColor);

        // 创建一个面板包含工具栏和搜索面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(backgroundColor);

        // 添加工具栏
        JToolBar toolBar = createToolBar();
        topPanel.add(toolBar, BorderLayout.NORTH);

        // 添加搜索面板
        JPanel searchPanel = createSearchPanel();
        topPanel.add(searchPanel, BorderLayout.CENTER);

        // 创建题目表格
        createQuestionTable();
        JScrollPane tableScrollPane = new JScrollPane(questionTable);

        // 将组件添加到主面板
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER); // 表格设置为CENTER而不是SOUTH

        return panel;
    }

    //创建工具栏
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
        JButton historyButton = createStyledButton("显示题目历史");


        // 添加事件监听器
        addButton.addActionListener(e -> showAddQuestionDialog()); // 添加题目
        editButton.addActionListener(e -> showEditQuestionDialog());
        deleteButton.addActionListener(e -> deleteSelectedQuestion()); // 删除题目
        showButton.addActionListener(e -> showQuestionDetail());
        historyButton.addActionListener(e -> showHistoryDetail());

        // 添加按钮到工具栏
        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(showButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(historyButton);

        return toolBar;
    }

    // 添加删除分类的方法
    private void deleteCategory() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                categoryTree.getLastSelectedPathComponent();

        if (node == null || !(node.getUserObject() instanceof Category)) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的分类");
            return;
        }

        Category category = (Category) node.getUserObject();

        // 检查是否有子分类
        if (categoryDAO.hasChildCategories(category.getCategoryId())) {
            JOptionPane.showMessageDialog(this,
                    "该分类下还有子分类，请先删除子分类！",
                    "无法删除",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 检查是否被题目使用
        if (categoryDAO.isUsedByQuestions(category.getCategoryId())) {
            JOptionPane.showMessageDialog(this,
                    "该分类下还有题目，请先移除或修改相关题目的分类！",
                    "无法删除",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 确认删除
        int option = JOptionPane.showConfirmDialog(this,
                "确定要删除分类 "+ category.getCategoryName() +  "吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            if (categoryDAO.deleteCategory(category.getCategoryId())) {
                JOptionPane.showMessageDialog(this, "删除成功");
                // 刷新分类树
                DefaultTreeModel model = createCategoryTreeModel();
                categoryTree.setModel(model);
            } else {
                JOptionPane.showMessageDialog(this, "删除失败");
            }
        }
    }

    // 创建搜索面板
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setBackground(backgroundColor);

        // 创建并初始化搜索组件
        panel.add(new JLabel("关键词："));
        searchKeywordField = new JTextField(20);
        panel.add(searchKeywordField);

        panel.add(new JLabel("题型："));
        questionTypeCombo = new JComboBox<>(new String[]{"全部", "选择题", "填空题", "简答题", "计算题"});
        panel.add(questionTypeCombo);

        panel.add(new JLabel("难度："));
        difficultyCombo = new JComboBox<>(new String[]{"全部", "简单", "中等", "困难"});
        panel.add(difficultyCombo);

        JButton searchButton = createStyledButton("搜索");
        searchButton.addActionListener(e -> searchQuestions());
        panel.add(searchButton);

        return panel;
    }

    // 搜索
    private void searchQuestions() {
        Thread searchThread = new Thread(() -> {
            LoadingDialog loadingDialog = LoadingDialog.show(this, "正在搜索...");
            try {
                String keyword = searchKeywordField.getText().trim();
                String questionType = (String) questionTypeCombo.getSelectedItem();
                String difficultyDisplay = (String) difficultyCombo.getSelectedItem();
                String difficulty = getDifficultyValue(difficultyDisplay);

                // 获取当前选中的分类
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        categoryTree.getLastSelectedPathComponent();
                // 将变量声明为 final
                final Integer selectedCategoryId = node != null && node.getUserObject() instanceof Category ?
                        ((Category) node.getUserObject()).getCategoryId() : null;

                // 获取所有题目并筛选
                List<Question> allQuestions = questionDAO.getAllQuestions();
                List<Question> filteredQuestions = allQuestions.stream()
                        .filter(q -> {
                            // 关键词匹配
                            boolean keywordMatch = keyword.isEmpty() ||
                                    q.getQuestionText().toLowerCase().contains(keyword.toLowerCase()) ||
                                    q.getAnswer().toLowerCase().contains(keyword.toLowerCase());

                            // 题型匹配
                            boolean typeMatch = questionType.equals("全部") ||
                                    q.getQuestionType().equals(questionType);

                            // 难度匹配
                            boolean difficultyMatch = difficulty.equals("all") ||
                                    q.getDifficulty().equals(difficulty);

                            // 分类匹配
                            boolean categoryMatch = selectedCategoryId == null ||
                                    q.getCategoryId() == selectedCategoryId.intValue();

                            return keywordMatch && typeMatch && difficultyMatch && categoryMatch;
                        })
                        .collect(java.util.stream.Collectors.toList());
                // 更新表格
                tableModel.setRowCount(0);
                for (Question question : filteredQuestions) {
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
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(QuestionManagementView.this,
                            "搜索失败：" + ex.getMessage());
                });
            } finally {
                if (loadingDialog != null) {
                    SwingUtilities.invokeLater(() -> loadingDialog.dispose());
                }
            }
        });
        searchThread.start();
    }

    //创建题目表格
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

    // 设置表格样式
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

    // 创建分类树模型
    private DefaultTreeModel createCategoryTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("全部分类");
        List<Category> categories = categoryDAO.getAllCategories();

        // 调试输出
        System.out.println("获取到的分类数量: " + categories.size());
        for (Category category : categories) {
            System.out.println("分类: " + category.getCategoryName()
                    + ", ID: " + category.getCategoryId()
                    + ", 父ID: " + category.getParentId());
        }

        // 先添加顶级分类(parentId为null的)
        for (Category category : categories) {
            if (category.getParentId() == 0) {  // 判断是否是顶级分类
                // System.out.println("添加顶级分类: " + category.getCategoryName());  // 调试输出
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
                buildCategoryTree(node, categories);
                root.add(node);
            }
        }

        return new DefaultTreeModel(root);
    }

    // 构建类型树
    private void buildCategoryTree(DefaultMutableTreeNode parentNode, List<Category> allCategories) {
        Category parent = (Category) parentNode.getUserObject();

        // 调试输出
        System.out.println("构建" + parent.getCategoryName() + "的子分类");

        // 查找当前节点的所有子分类
        for (Category category : allCategories) {
            // 如果当前分类的父ID等于当前节点的ID，说明是它的子分类
            if (category.getParentId() != null && category.getParentId().equals(parent.getCategoryId())) {
                System.out.println("  添加子分类: " + category.getCategoryName());  // 调试输出
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(category);
                buildCategoryTree(node, allCategories);
                parentNode.add(node);
            }
        }
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

    // 显示编辑问题详情
    private void showEditQuestionDialog() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要查看的题目");
            return;
        }
        int questionId = (int) questionTable.getValueAt(selectedRow, 0);
        Question q = questionDAO.getQuestionById(questionId);
        ShowEditQuestionDialog dialog = new ShowEditQuestionDialog(
                this,
                q,
                categoryDAO,
                questionDAO,
                currentUser.getUserId(),
                this::loadQuestionData
        );
        dialog.setVisible(true);
    }

    // 加载题目数据
    private void loadQuestionData() {
        // 创建一个新线程来加载数据
        Thread loadThread = new Thread(() -> {
            LoadingDialog loadingDialog = LoadingDialog.show(this, "正在加载题库数据");
            try {
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
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "加载数据失败：" + ex.getMessage());
                });
            } finally {
                // 关闭加载对话框
                if (loadingDialog != null) {
                    SwingUtilities.invokeLater(() -> loadingDialog.dispose());
                }
            }
        });
        loadThread.start();
    }

    // 展示题目历史记录
    void showHistoryDetail() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择要查看历史的题目");
            return;
        }
        int questionId = (int) questionTable.getValueAt(selectedRow, 0);
        ShowHistoryDialog dialog = new ShowHistoryDialog(this, questionId);
        dialog.setVisible(true);
    }

    // 过滤器
    private void filterQuestionsByCategory(int categoryId) {
        Thread filterThread = new Thread(() -> {
            LoadingDialog loadingDialog = LoadingDialog.show(this, "正在加载分类题目...");
            try {
                // 获取所有题目
                List<Question> questions = questionDAO.getAllQuestions();

                // 筛选属于当前分类的题目
                List<Question> filteredQuestions = questions.stream()
                        .filter(q -> q.getCategoryId() == categoryId)
                        .collect(java.util.stream.Collectors.toList());

                // 更新表格
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    for (Question question : filteredQuestions) {
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
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(QuestionManagementView.this,
                            "加载分类题目失败：" + ex.getMessage());
                });
            } finally {
                if (loadingDialog != null) {
                    SwingUtilities.invokeLater(() -> loadingDialog.dispose());
                }
            }
        });
        filterThread.start();
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

    // 反过来
    private String getDifficultyValue(String displayText) {
        switch (displayText) {
            case "简单": return "easy";
            case "中等": return "medium";
            case "困难": return "hard";
            default: return "all";
        }
    }

    // 获取分类名称
    private String getCategoryName(int categoryId) {
        Category category = categoryDAO.getCategoryById(categoryId);
        return category != null ? category.getCategoryName() : "";
    }

    // 删除选中的题目d
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
            if (questionDAO.deleteQuestion(questionId,currentUser.getUserId())) {
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

    // test
    public static void main(String[] args) {
        new QuestionManagementView().setVisible(true);
    }
}