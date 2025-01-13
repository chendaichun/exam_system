package View.SubQM;

import DAO.CategoryDAO;
import model.Category;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AddCategoryDialog extends JDialog {
    private CategoryDAO categoryDAO;
    private JTextField nameField;
    private JComboBox<Category> parentCombo;
    private final Color primaryColor = new Color(51, 122, 183);
    private final Font defaultFont = new Font("微软雅黑", Font.PLAIN, 14);
    private Runnable refreshCallback;

    public AddCategoryDialog(Frame owner, CategoryDAO categoryDAO, Runnable refreshCallback) {
        super(owner, "添加分类", true);
        this.categoryDAO = categoryDAO;
        this.refreshCallback = refreshCallback;
        initComponents();
    }

    private void initComponents() {
        setSize(400, 250);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 分类名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("分类名称：");
        nameLabel.setFont(defaultFont);
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        nameField.setFont(defaultFont);
        formPanel.add(nameField, gbc);

        // 父级分类
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel parentLabel = new JLabel("父级分类：");
        parentLabel.setFont(defaultFont);
        formPanel.add(parentLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        parentCombo = new JComboBox<>();
        parentCombo.setFont(defaultFont);
        loadCategories();
        formPanel.add(parentCombo, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        JButton confirmButton = createStyledButton("确定");
        JButton cancelButton = createStyledButton("取消");

        confirmButton.addActionListener(e -> addCategory());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        // 添加到主面板
        add(new JPanel() {{
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
            add(formPanel, BorderLayout.CENTER);
        }}, BorderLayout.CENTER);

        add(new JPanel() {{
            setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            add(buttonPanel);
        }}, BorderLayout.SOUTH);
    }

    private void loadCategories() {
        parentCombo.addItem(new Category() {{
            setCategoryId(0);
            setCategoryName("作为顶级分类");
        }});

        List<Category> categories = categoryDAO.getAllCategories();
        for (Category category : categories) {
            parentCombo.addItem(category);
        }
    }

    private void addCategory() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入分类名称");
            return;
        }

        Category newCategory = new Category();
        newCategory.setCategoryName(name);

        Category selectedParent = (Category) parentCombo.getSelectedItem();
        if (selectedParent.getCategoryId() != 0) {
            newCategory.setParentId(selectedParent.getCategoryId());
        } else {
            newCategory.setParentId(0);  // 顶级分类
        }

        if (categoryDAO.addCategory(newCategory)) {
            JOptionPane.showMessageDialog(this, "添加成功");
            refreshCallback.run();  // 刷新分类树
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "添加失败");
        }
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
        return button;
    }
}