package gui;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class RobotCoordinatesWindow extends JInternalFrame implements Observer {
    private final JLabel coordinatesLabel;

    public RobotCoordinatesWindow(RobotModel model) {
        super("Координаты робота", true, true, true, true);
        model.addObserver(this);

        coordinatesLabel = new JLabel("Робот: (0, 0) | Цель: (0, 0)");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(coordinatesLabel, BorderLayout.CENTER);
        getContentPane().add(panel);
        pack();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof double[] pos) {  // Обновление позиции робота
            updateRobotPosition(pos[0], pos[1]);
        } else if (arg instanceof int[] target) {  // Обновление позиции цели
            updateTargetPosition(target[0], target[1]);
        }
    }

    private void updateRobotPosition(double x, double y) {
        EventQueue.invokeLater(() -> {
            String current = coordinatesLabel.getText();
            String newText = String.format("Робот: (%.1f, %.1f)", x, y) +
                    current.substring(current.indexOf(" | "));
            coordinatesLabel.setText(newText);
        });
    }

    // Новый метод: обновление позиции цели
    private void updateTargetPosition(int x, int y) {
        EventQueue.invokeLater(() -> {
            String current = coordinatesLabel.getText();
            String newText = current.substring(0, current.indexOf(" | ") + 3) +
                    String.format("Цель: (%d, %d)", x, y);
            coordinatesLabel.setText(newText);
        });
    }
}