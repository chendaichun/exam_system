package View;

import DAO.QuestionHistoryDAO;
import model.QuestionHistory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class ShowHistoryDialog extends JDialog {
    private DefaultTableModel tableModel;
    private JTable historyTable;
    private QuestionHistoryDAO historyDAO;
    private final int questionId;

    // 定义界面主要颜色和字体
    private final Color primaryColor = new Color(51, 122, 183);
    private final Color backgroundColor = new Color(240, 242, 245);
    private final Font titleFont = new Font("微软雅黑", Font.BOLD, 16);
    private final Font tableFont = new Font("微软雅黑", Font.PLAIN, 14);

    public ShowHistoryDialog(Frame owner, int questionId) {
        super(owner, "题目操作历史", true);
        this.questionId = questionId;
        this.historyDAO = new QuestionHistoryDAO();
        initComponents();
        loadHistoryData();
    }

    private void initComponents() {
        setSize(800, 500);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建标题标签
        JLabel titleLabel = new JLabel("题目操作历史记录", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // 创建表格
        createHistoryTable();
        JScrollPane scrollPane = new JScrollPane(historyTable);

        // 创建底部按钮
        JButton closeButton = createStyledButton("关闭");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(closeButton);

        // 添加组件到主面板
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void createHistoryTable() {
        String[] columnNames = {"操作时间", "操作类型", "操作用户"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        setupTableStyle(historyTable);
    }

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

        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(150);  // 操作时间列
        table.getColumnModel().getColumn(1).setPreferredWidth(100);  // 操作类型列
        table.getColumnModel().getColumn(2).setPreferredWidth(100);  // 操作用户列
    }

    private void loadHistoryData() {
        List<QuestionHistory> historyList = historyDAO.getHistoryByQuestionId(questionId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        tableModel.setRowCount(0);
        for (QuestionHistory history : historyList) {
            Object[] rowData = {
                    sdf.format(history.getOperationTime()),
                    history.getOperationTypeDisplay(),
                    history.getUsername()
            };
            tableModel.addRow(rowData);
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(tableFont);
        button.setForeground(Color.WHITE);
        button.setBackground(primaryColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(100, 30));

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
}
