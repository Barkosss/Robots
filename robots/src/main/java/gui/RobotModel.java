package gui;

import java.util.Observable;

public class RobotModel extends Observable {
    private volatile double robotPositionX = 100;
    private volatile double robotPositionY = 100;
    private volatile double robotDirection = 0;
    private volatile int targetPositionX = 150;
    private volatile int targetPositionY = 150;

    public double getRobotPositionX() {
        return robotPositionX;
    }

    public double getRobotPositionY() {
        return robotPositionY;
    }

    public double getRobotDirection() {
        return robotDirection;
    }

    public int getTargetPositionX() {
        return targetPositionX;
    }

    public int getTargetPositionY() {
        return targetPositionY;
    }

    public void setRobotPosition(double x, double y, double direction) {
        this.robotPositionX = x;
        this.robotPositionY = y;
        this.robotDirection = direction;

        setChanged();
        notifyObservers(new double[]{x, y});
    }

    public void setTargetPosition(int x, int y) {
        this.targetPositionX = x;
        this.targetPositionY = y;

        setChanged();
        notifyObservers(new int[]{x, y});
    }
}
