package Util;

import model.*;
import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;
import java.math.BigDecimal;

public class WordExportUtil {
    public static void exportExamToRTF(Exam exam, String filePath) throws IOException {
        DefaultStyledDocument document = new DefaultStyledDocument();
        RTFEditorKit kit = new RTFEditorKit();

        try {
            // 创建样式
            Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

            // 标题样式
            Style titleStyle = document.addStyle("title", defaultStyle);
            StyleConstants.setFontSize(titleStyle, 18);
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setAlignment(titleStyle, StyleConstants.ALIGN_CENTER);

            // 普通文本样式
            Style normalStyle = document.addStyle("normal", defaultStyle);
            StyleConstants.setFontSize(normalStyle, 12);

            // 题型标题样式
            Style typeStyle = document.addStyle("type", defaultStyle);
            StyleConstants.setFontSize(typeStyle, 14);
            StyleConstants.setBold(typeStyle, true);

            // 插入标题
            document.insertString(document.getLength(), exam.getExamName() + "\n\n", titleStyle);

            // 插入试卷信息
            String examInfo = String.format("总分：%s分    考试时长：%d分钟\n\n",
                    exam.getTotalScore(), exam.getDuration());
            document.insertString(document.getLength(), examInfo, normalStyle);

            // 处理题目
            String currentType = null;
            BigDecimal typeTotal = BigDecimal.ZERO;
            int questionNumber = 1;

            if (exam.getExamQuestions() != null) {
                for (ExamQuestion eq : exam.getExamQuestions()) {
                    Question q = eq.getQuestion();

                    // 如果是新题型，添加标题
                    if (currentType == null || !currentType.equals(q.getQuestionType())) {
                        if (currentType != null) {
                            // 添加上一题型总分
                            document.insertString(document.getLength(),
                                    String.format("%s总分：%s分\n\n", currentType, typeTotal),
                                    normalStyle);
                        }

                        currentType = q.getQuestionType();
                        document.insertString(document.getLength(),
                                "【" + currentType + "】\n", typeStyle);
                        typeTotal = BigDecimal.ZERO;
                    }

                    // 添加题目
                    document.insertString(document.getLength(),
                            String.format("%d. %s （%s分）\n",
                                    questionNumber++, q.getQuestionText(), eq.getScore()),
                            normalStyle);

                    typeTotal = typeTotal.add(eq.getScore());
                }

                // 添加最后一个题型的总分
                if (currentType != null) {
                    document.insertString(document.getLength(),
                            String.format("%s总分：%s分\n", currentType, typeTotal),
                            normalStyle);
                }
            }

            // 保存文档
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                kit.write(out, document, 0, document.getLength());
            }

        } catch (BadLocationException e) {
            throw new IOException("文档生成失败", e);
        }
    }
}