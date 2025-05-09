package gui;

import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается.
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 */
public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final WindowStateManager windowStateManager = new WindowStateManager(desktopPane, this);

    public enum WindowType {
        LOG("log"), GAME("game");

        WindowType(String game) {
            this.game = game;
        }

        private final String game;

        public String getValue() {
            return game;
        }
    }

    public MainApplicationFrame() {
        setContentPane(desktopPane);

        RobotModel robotModel = new RobotModel();

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow(robotModel);
        addWindow(gameWindow);

        RobotCoordinatesWindow coordinatesWindow = new RobotCoordinatesWindow(robotModel);
        coordinatesWindow.setBounds(620, 10, 250, 100);
        addWindow(coordinatesWindow);

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Панель заголовка
        JPanel titleBar = new JPanel();
        titleBar.setPreferredSize(new Dimension(getWidth(), 30));
        titleBar.add(getButtonPanel(), BorderLayout.EAST);
        add(titleBar, BorderLayout.NORTH);

        windowStateManager.loadWindowsState();

        setVisible(true);
    }

    private JPanel getButtonPanel() {
        JButton closeButton = new JButton("Закрыть");
        JButton minimizeButton = new JButton("Свернуть");
        JButton maximizeButton = new JButton("Развернуть");

        // Обработчики кнопок
        closeButton.addActionListener(_ -> System.exit(0));
        minimizeButton.addActionListener(_ -> setState(JFrame.ICONIFIED));
        maximizeButton.addActionListener(_ -> setExtendedState(getExtendedState() == JFrame.MAXIMIZED_BOTH ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH));

        // Панель кнопок справа
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 2));
        buttonPanel.setOpaque(false);
        buttonPanel.add(minimizeButton);
        buttonPanel.add(maximizeButton);
        buttonPanel.add(closeButton);
        return buttonPanel;
    }

    protected LogWindow createLogWindow() {
        LogWindow logWindow = new LogWindow(Logger.getDefaultLogSource());
        logWindow.setLocation(10, 10);
        setMinimumSize(logWindow.getSize());
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

    private JMenuBar generateMenuBar() {
        JMenu lookAndFeelMenu = new JMenu("Режим отображения");
        lookAndFeelMenu.setMnemonic(KeyEvent.VK_V);
        lookAndFeelMenu.getAccessibleContext().setAccessibleDescription("Управление режимом отображения приложения");
        lookAndFeelMenu.add(createSystemLookAndFeel());
        lookAndFeelMenu.add(createCrossplatformLookAndFeel());

        JButton buttonCloseWindow = new JButton("Закрыть");
        buttonCloseWindow.getAccessibleContext().setAccessibleDescription("Завершение работы приложенияЗ");
        buttonCloseWindow.add(createCloseWindowButton());

        JMenu testMenu = new JMenu("Тесты");
        testMenu.setMnemonic(KeyEvent.VK_T);
        testMenu.getAccessibleContext().setAccessibleDescription("Тестовые команды");
        testMenu.add(createAddLogMessageItem());

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(lookAndFeelMenu);
        menuBar.add(testMenu);
        menuBar.add(buttonCloseWindow);
        return menuBar;
    }

    private JMenuItem createSystemLookAndFeel() {
        JMenuItem systemLookAndFeel = new JMenuItem("Системная схема", KeyEvent.VK_S);
        systemLookAndFeel.addActionListener((_) -> {
            setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            this.invalidate();
        });

        return systemLookAndFeel;
    }

    private JMenuItem createCrossplatformLookAndFeel() {
        JMenuItem crossplatformLookAndFeel = new JMenuItem("Универсальная схема", KeyEvent.VK_S);
        crossplatformLookAndFeel.addActionListener((_) -> {
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            this.invalidate();
        });

        return crossplatformLookAndFeel;
    }

    private JMenuItem createAddLogMessageItem() {
        JMenuItem addLogMessageItem = new JMenuItem("Сообщение в лог", KeyEvent.VK_S);
        addLogMessageItem.addActionListener((_) -> Logger.debug("Новая строка"));

        return addLogMessageItem;
    }

    private JButton createCloseWindowButton() {
        JButton closeWindowButton = new JButton("Закрыть");
        closeWindowButton.addActionListener((_) -> {
            int result = JOptionPane.showConfirmDialog(null, "Закрыть приложение?", "Выберите действие", JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                windowStateManager.saveWindowsState(this);
                System.exit(0);
            }
        });
        return closeWindowButton;
    }

    private void setLookAndFeel(String className) {
        try {
            UIManager.setLookAndFeel(className);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException eZ) {
            // just ignore
        }
    }
}