package gui;

import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается.
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 */
public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final String CONFIG_FILE = "robots/src/main/resources/windows.properties";

    private enum WindowType {
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

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        addWindow(gameWindow);

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Панель заголовка
        JPanel titleBar = new JPanel();
        titleBar.setPreferredSize(new Dimension(getWidth(), 30));
        titleBar.add(getButtonPanel(), BorderLayout.EAST);
        add(titleBar, BorderLayout.NORTH);

        loadWindowsState();

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
                saveWindowsState();
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

    private void saveWindowsState() {
        Properties props = new Properties();

        // Сохраняем состояние главного окна
        props.setProperty("main.x", String.valueOf(getX()));
        props.setProperty("main.y", String.valueOf(getY()));
        props.setProperty("main.width", String.valueOf(getWidth()));
        props.setProperty("main.height", String.valueOf(getHeight()));
        props.setProperty("main.state", String.valueOf(getExtendedState()));

        // Сохраняем состояние внутренних окон (LogWindow, GameWindow)
        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (JInternalFrame frame : frames) {
            Optional<WindowType> optionalWindowType = getWindowKey(frame);

            if (optionalWindowType.isEmpty()) {
                continue;
            }

            String windowKey = optionalWindowType.get().getValue();

            Rectangle bounds = frame.getBounds();
            props.setProperty(windowKey + ".x", String.valueOf(bounds.x));
            props.setProperty(windowKey + ".y", String.valueOf(bounds.y));
            props.setProperty(windowKey + ".width", String.valueOf(bounds.width));
            props.setProperty(windowKey + ".height", String.valueOf(bounds.height));
            props.setProperty(windowKey + ".icon", String.valueOf(frame.isIcon()));
            props.setProperty(windowKey + ".max", String.valueOf(frame.isMaximum()));
        }

        try (OutputStream outputStream = new FileOutputStream(CONFIG_FILE)) {
            props.store(outputStream, "Window State");

        } catch (IOException err) {
            System.out.printf("[ERROR] Save Window State: %s", err);
        }
    }

    private void loadWindowsState() {
        Properties props = new Properties();

        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);

            // Восстанавливаем состояние главного окна
            int x = Integer.parseInt(props.getProperty("main.x", "100"));
            int y = Integer.parseInt(props.getProperty("main.y", "100"));
            int width = Integer.parseInt(props.getProperty("main.width", "800"));
            int height = Integer.parseInt(props.getProperty("main.height", "600"));
            int state = Integer.parseInt(props.getProperty("main.state", "0"));

            if (state == JFrame.NORMAL) {
                setBounds(x, y, width, height);
            }

            if (state == JFrame.NORMAL && getExtendedState() != JFrame.NORMAL) {
                setBounds(x, y, width, height);
            }

            setExtendedState(state);

            // Восстанавливаем состояние внутренних окон
            JInternalFrame[] frames = desktopPane.getAllFrames();
            for (JInternalFrame frame : frames) {
                Optional<WindowType> optionalWindowType = getWindowKey(frame);

                if (optionalWindowType.isEmpty()) {
                    continue;
                }

                String windowKey = optionalWindowType.get().getValue();

                // Восстанавливаем только сохраненные окна (log, game)
                int frameX = Integer.parseInt(props.getProperty(windowKey + ".x", "100"));
                int frameY = Integer.parseInt(props.getProperty(windowKey + ".y", "100"));
                int frameWidth = Integer.parseInt(props.getProperty(windowKey + ".width", "400"));
                int frameHeight = Integer.parseInt(props.getProperty(windowKey + ".height", "300"));
                boolean isIcon = Boolean.parseBoolean(props.getProperty(windowKey + ".icon", "false"));
                boolean isMax = Boolean.parseBoolean(props.getProperty(windowKey + ".max", "false"));

                try {
                    if (isIcon) {
                        frame.setIcon(true);
                    } else if (isMax) {
                        frame.setMaximum(true);
                    } else {
                        frame.setBounds(frameX, frameY, frameWidth, frameHeight);
                    }
                } catch (PropertyVetoException ignore) {
                    // Ignore
                }
            }

        } catch (IOException | NumberFormatException err) {
            System.out.printf("[WARN] Load Window State: %s%n", err);
        }
    }

    private Optional<WindowType> getWindowKey(JInternalFrame frame) {
        if (frame instanceof LogWindow) {
            return Optional.of(WindowType.LOG);
        } else if (frame instanceof GameWindow) {
            return Optional.of(WindowType.GAME);
        }

        return Optional.empty();
    }
}