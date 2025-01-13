package View;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LoadingDialog extends JDialog {
    private JLabel loadingLabel;
    private final Color backgroundColor = new Color(255, 255, 255);
    private final Color textColor = new Color(51, 122, 183);
    private final Font loadingFont = new Font("微软雅黑", Font.PLAIN, 14);

    public LoadingDialog(Frame parent, String message) {
        super(parent, true);
        initComponents(message);
    }

    private void initComponents(String message) {
        // 设置对话框属性
        setUndecorated(true); // 无边框
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        setSize(250, 100);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建加载图标
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        iconPanel.setBackground(backgroundColor);
        JLabel spinnerLabel = new JLabel(createLoadingIcon());
        iconPanel.add(spinnerLabel);

        // 创建文本标签
        loadingLabel = new JLabel(message);
        loadingLabel.setFont(loadingFont);
        loadingLabel.setForeground(textColor);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 添加组件到主面板
        mainPanel.add(iconPanel, BorderLayout.CENTER);
        mainPanel.add(loadingLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private ImageIcon createLoadingIcon() {
        // 创建一个简单的加载图标
        int size = 30;
        ImageIcon icon = new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB));
        Graphics2D g2 = (Graphics2D) icon.getImage().getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(textColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawArc(2, 2, size - 4, size - 4, 0, 300);
        g2.dispose();
        return icon;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    // 显示加载对话框
    public static LoadingDialog show(Frame parent, String message) {
        LoadingDialog dialog = new LoadingDialog(parent, message);
        // 使用新线程显示对话框，避免阻塞
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
        return dialog;
    }
}
