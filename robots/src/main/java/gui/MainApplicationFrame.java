package gui;

import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Что требуется сделать:
 * 1. Метод создания меню перегружен функционалом и трудно читается.
 * Следует разделить его на серию более простых методов (или вообще выделить отдельный класс).
 */
public class MainApplicationFrame extends JFrame {
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final String CONFIG_FILE = System.getProperty("user.home") + File.separator + ".properties";


    public MainApplicationFrame() {
        JFrame frame = new JFrame();
        frame.setState(Frame.ICONIFIED);
        frame.setUndecorated(true);

        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        int inset = 50;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2);
        setContentPane(desktopPane);

        LogWindow logWindow = createLogWindow();
        addWindow(logWindow);

        GameWindow gameWindow = new GameWindow();
        gameWindow.setSize(400, 400);
        addWindow(gameWindow);

        setJMenuBar(generateMenuBar());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Панель заголовка
        JPanel titleBar = new JPanel();

        JPanel buttonPanel = getButtonPanel();

        titleBar.add(buttonPanel, BorderLayout.EAST);
    }

    private JPanel getButtonPanel() {
        JButton closeButton = new JButton("Закрыть");
        JButton minimizeButton = new JButton("Свернуть");
        JButton maximizeButton = new JButton("Развернуть");

        // Обработчики кнопок
        closeButton.addActionListener(_ -> System.exit(0));
        minimizeButton.addActionListener(_ -> setState(JFrame.ICONIFIED));
        maximizeButton.addActionListener(_ ->
                setExtendedState(getExtendedState() == JFrame.MAXIMIZED_BOTH ? JFrame.NORMAL : JFrame.MAXIMIZED_BOTH));

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
        logWindow.setSize(300, 800);
        setMinimumSize(logWindow.getSize());
        logWindow.pack();
        Logger.debug("Протокол работает");
        return logWindow;
    }

    protected void addWindow(JInternalFrame frame) {
        desktopPane.add(frame);
        frame.setVisible(true);
    }

//    protected JMenuBar createMenuBar() {
//        JMenuBar menuBar = new JMenuBar();
//
//        //Set up the lone menu.
//        JMenu menu = new JMenu("Document");
//        menu.setMnemonic(KeyEvent.VK_D);
//        menuBar.add(menu);
//
//        //Set up the first menu item.
//        JMenuItem menuItem = new JMenuItem("New");
//        menuItem.setMnemonic(KeyEvent.VK_N);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_N, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("new");
//        menuItem.addActionListener(this);
//        menu.add(menuItem);
//
//        //Set up the second menu item.
//        menuItem = new JMenuItem("Quit");
//        menuItem.setMnemonic(KeyEvent.VK_Q);
//        menuItem.setAccelerator(KeyStroke.getKeyStroke(
//                KeyEvent.VK_Q, ActionEvent.ALT_MASK));
//        menuItem.setActionCommand("quit");
//        menuItem.addActionListener(this);
//        menu.add(menuItem);
//
//        return menuBar;
//    }

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
            int result = JOptionPane.showConfirmDialog(null, "Закрыть приложение?",
                    "Выберите действие", JOptionPane.YES_NO_OPTION);

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
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException eZ) {
            // just ignore
        }
    }

    private void saveWindowsState() {
        System.out.println("Start save window");
        Properties props = System.getProperties();

        props.setProperty("main.x", String.valueOf(getX()));
        props.setProperty("main.y", String.valueOf(getY()));
        props.setProperty("main.width", String.valueOf(getWidth()));
        props.setProperty("main.height", String.valueOf(getHeight()));
        props.setProperty("main.state", String.valueOf(getExtendedState()));

        try (OutputStream outputStream = new FileOutputStream(CONFIG_FILE)) {
            props.store(outputStream, "Window State");

        } catch (IOException err) {
            System.out.printf("[ERROR] Save Window State: %s", err);
        }
    }

    private void loadWindowsState() {
        File fileProperties = new File(CONFIG_FILE);

        // ...
    }
}
