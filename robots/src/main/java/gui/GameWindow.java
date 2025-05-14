package gui;

import javax.swing.*;
import java.awt.*;

public class GameWindow extends JInternalFrame {

    public GameWindow(RobotModel model) {
        super("Игровое поле", true, true, true, true);
        GameVisualizer m_visualizer = new GameVisualizer(model);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_visualizer, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
    }
}
