package gui;

import gui.MainApplicationFrame.WindowType;
import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Properties;

public class WindowStateManager {
    private final String CONFIG_FILE = System.getProperty("user.home") + File.separator + "windows.properties";
    private final Properties config = new Properties();
    private final JDesktopPane desktopPane;
    private final JFrame frame;

    public WindowStateManager(JDesktopPane desktopPane_, JFrame frame_) {
        this.desktopPane = desktopPane_;
        this.frame = frame_;
    }

    public void saveWindowsState(Window window) {
        // Сохраняем состояние главного окна
        config.setProperty("x", String.valueOf(window.getX()));
        config.setProperty("y", String.valueOf(window.getY()));
        config.setProperty("width", String.valueOf(window.getWidth()));
        config.setProperty("height", String.valueOf(window.getHeight()));
        config.setProperty("state", String.valueOf(frame.getExtendedState()));

        // Сохраняем состояние внутренних окон (LogWindow, GameWindow)
        JInternalFrame[] frames = desktopPane.getAllFrames();
        for (JInternalFrame frame : frames) {
            Optional<WindowType> optionalWindowType = getWindowKey(frame);

            if (optionalWindowType.isEmpty()) {
                continue;
            }

            String windowKey = optionalWindowType.get().getValue();

            Rectangle bounds = frame.getBounds();
            config.setProperty(windowKey + ".x", String.valueOf(bounds.x));
            config.setProperty(windowKey + ".y", String.valueOf(bounds.y));
            config.setProperty(windowKey + ".width", String.valueOf(bounds.width));
            config.setProperty(windowKey + ".height", String.valueOf(bounds.height));
            config.setProperty(windowKey + ".icon", String.valueOf(frame.isIcon()));
            config.setProperty(windowKey + ".max", String.valueOf(frame.isMaximum()));
            config.setProperty(windowKey + ".order", String.valueOf(frame));
        }

        try (OutputStream outputStream = new FileOutputStream(CONFIG_FILE)) {
            config.store(outputStream, "Window State");

        } catch (IOException err) {
            Logger.error(String.format("[ERROR] Save Window State: %s", err));
            System.out.printf("[ERROR] Save Window State: %s", err);
        }
    }

    public void loadWindowsState() {
        Properties props = new Properties();

        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);

            // Восстанавливаем состояние главного окна
            int x = Integer.parseInt(props.getProperty("x", "100"));
            int y = Integer.parseInt(props.getProperty("y", "100"));
            int width = Integer.parseInt(props.getProperty("width", "800"));
            int height = Integer.parseInt(props.getProperty("height", "600"));
            int state = Integer.parseInt(props.getProperty("state", "0"));

            if (state == JFrame.NORMAL) {
                frame.setBounds(x, y, width, height);
            }

            if (state == JFrame.NORMAL && frame.getExtendedState() != JFrame.NORMAL) {
                frame.setBounds(x, y, width, height);
            }

            frame.setExtendedState(state);

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
            Logger.debug(String.format("[WARN] Load Window State: %s", err));
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
