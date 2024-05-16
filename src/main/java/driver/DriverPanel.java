package driver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DriverPanel extends JFrame {

    public enum Direction {
        Right,
        Left,
        Up,
        Down
    }

    final int screenWidth = 600;
    final int screenHeight = 600;

    private Direction direction = Direction.Right;
    private volatile boolean running = true;
    private volatile boolean needSendCommand = false;
    private volatile boolean needCharge = false;

    public DriverPanel() {
        this.setName("DriverPanel");
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addKeyListener(new Adapter());
        this.pack();
        this.repaint();
    }

    public class Adapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_RIGHT -> {
                    direction = Direction.Right;
                    needSendCommand = true;
                }
                case KeyEvent.VK_LEFT -> {
                    direction = Direction.Left;
                    needSendCommand = true;
                }
                case KeyEvent.VK_UP -> {
                    direction = Direction.Up;
                    needSendCommand = true;
                }
                case KeyEvent.VK_DOWN -> {
                    direction = Direction.Down;
                    needSendCommand = true;
                }
                case KeyEvent.VK_S -> {
                    if (e.isControlDown()) {
                        running = true;
                        needSendCommand = true;
                    }
                }
                case KeyEvent.VK_E -> {
                    if (e.isControlDown()) {
                        running = false;
                        needSendCommand = true;
                    }
                }
                case KeyEvent.VK_F -> {
                    if (e.isControlDown()) {
                        needCharge = true;
                        needSendCommand = true;
                    }
                }
            }
        }
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isNeedSendCommand() {
        return needSendCommand;
    }

    public void setNeedSendCommand(boolean needSendCommand) {
        this.needSendCommand = needSendCommand;
    }

    public boolean isNeedCharge() {
        return needCharge;
    }

    public void setNeedCharge(boolean needCharge) {
        this.needCharge = needCharge;
    }
}
