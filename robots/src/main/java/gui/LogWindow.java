package gui;

import log.LogChangeListener;
import log.LogEntry;
import log.LogWindowSource;

import javax.swing.*;
import java.awt.*;

public class LogWindow extends JInternalFrame implements LogChangeListener {
    private final LogWindowSource m_logSource;
    private final TextArea m_logContent;

    public LogWindow(LogWindowSource logSource) {
        super("Протокол работы", true, true, true, true);
        m_logSource = logSource;
        m_logSource.registerListener(this);
        m_logContent = new TextArea("");
        m_logContent.setSize(200, 500);


        JFrame frame = new JFrame();
        JPanel panel = new JPanel(new BorderLayout());
        frame.setUndecorated(true);

        // Создаем кнопку "Закрыть"
        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener((_) -> {
            int result = JOptionPane.showConfirmDialog(null, "Закрыть приложение?");

            if (result == JOptionPane.YES_OPTION) {
                dispose();
            }
        });

        // Создаем кнопку "Свернуть"
        JButton minimizeButton = new JButton("Свернуть");
        minimizeButton.addActionListener(_ -> {
            frame.setState(JFrame.ICONIFIED); // Свернуть окно
        });

        // Создаем кнопку "Развернуть"
        JButton maximizeButton = new JButton("Развернуть");
        maximizeButton.addActionListener(_ -> {
            if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                frame.setExtendedState(JFrame.NORMAL); // Восстановить размер окна
            } else {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Развернуть окно
            }
        });

        // Добавляем кнопки на панель
        panel.add(minimizeButton);
        panel.add(maximizeButton);
        panel.add(closeButton);

        // Добавляем панель с кнопками в окно
        frame.add(panel, BorderLayout.NORTH);

        panel.add(m_logContent, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
        updateLogContent();
    }

    private void updateLogContent() {
        StringBuilder content = new StringBuilder();
        for (LogEntry entry : m_logSource.all()) {
            content.append(entry.strMessage()).append("\n");
        }
        m_logContent.setText(content.toString());
        m_logContent.invalidate();
    }

    @Override
    public void onLogChanged() {
        EventQueue.invokeLater(this::updateLogContent);
    }
}
