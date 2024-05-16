package car;

import driver.DriverPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class CarPanel extends JPanel implements ActionListener {

    static final int screenWidth = 600;
    static final int screenHeight = 600;
    static final int unitSize = 25;
    static final int gameUnits = (screenWidth * screenHeight) / unitSize;
    public static final int delay = 400;

    private volatile boolean running = true;

    private Car car;
    private Barrier barrier;

    private Timer timer;
    private final Random random;


    public CarPanel() {
        timer = new Timer(delay, this);
        timer.start();
        random = new Random();

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.setVisible(true);

        startGame();
    }

    public void startGame() {
        running = true;
        car = new Car();
        barrier = new Barrier();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isRunning()) {
            move();
            checkCollisions();
        }
        repaint();
    }

    private void move() {
        for (int i = car.length; i > 0; i--) {
            car.XPositions[i] = car.XPositions[i - 1];
            car.YPositions[i] = car.YPositions[i - 1];
        }

        switch (car.direction) {
            case Right -> car.XPositions[0] += unitSize;
            case Left -> car.XPositions[0] -= unitSize;
            case Up -> car.YPositions[0] -= unitSize;
            case Down -> car.YPositions[0] += unitSize;
        }
    }

    public void checkCollisions() {
        checkWallCollision();
        checkBarrierCollision();
    }

    private void checkWallCollision() {
        if (car.XPositions[0] > screenWidth) {
            car.XPositions[0] -= 2 * unitSize;
            running = false;
        } else if (car.XPositions[0] < 0) {
            car.XPositions[0] += unitSize;
            running = false;
        } else if (car.YPositions[0] > screenHeight) {
            car.YPositions[0] -= 2 * unitSize;
            running = false;
        } else if (car.YPositions[0] < 0) {
            car.YPositions[0] += unitSize;
            running = false;
        }
    }

    public void checkBarrierCollision() {
        for (int i = 0; i < car.length; i++) {
            if (car.XPositions[i] == barrier.XPosition && car.YPositions[i] == barrier.YPosition) {
                barrier = new Barrier();
                running = false;
                break;
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        drawGrid(g);
        drawSnake(g);
        drawApple(g);
    }

    private void drawGrid(Graphics g) {
        for (int i = 0; i < screenHeight / unitSize; i++) {
            g.drawLine(i * unitSize, 0, i * unitSize, screenHeight);
            g.drawLine(0, i * unitSize, screenWidth, i * unitSize);
        }
    }

    private void drawSnake(Graphics g) {
        g.setColor(Color.GREEN);
        for (int i = 0; i < car.length; i++) {
            g.fillRect(car.XPositions[i], car.YPositions[i], unitSize, unitSize);
        }
    }

    private void drawApple(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(barrier.XPosition, barrier.YPosition, unitSize, unitSize);
    }

    public class Barrier {
        public int XPosition;
        public int YPosition;

        public Barrier() {
            XPosition = random.nextInt(screenWidth / unitSize) * unitSize;
            YPosition = random.nextInt(screenWidth / unitSize) * unitSize;
        }
    }

    public class Car {
        private final int startXPosition = gameUnits / 2;
        private final int startYPosition = gameUnits / 2;

        private final int[] XPositions = new int[gameUnits];
        private final int[] YPositions = new int[gameUnits];

        private int length = 1;
        private DriverPanel.Direction direction;

        public Car() {
            direction = DriverPanel.Direction.Right;
            createBodyPartPositions();
        }

        private void createBodyPartPositions() {
            if (direction == DriverPanel.Direction.Right) {
                for (int i = this.length; i > 0; i--) {
                    XPositions[i] = ((startXPosition + i) * unitSize);
                }
            } else if (direction == DriverPanel.Direction.Left) {
                for (int i = 0; i < this.length; i++) {
                    XPositions[i] = ((startXPosition - i) * unitSize);
                }
            } else if (direction == DriverPanel.Direction.Up) {
                for (int i = this.length; i > 0; i--) {
                    YPositions[i] = ((startYPosition + i) * unitSize);
                }
            } else if (direction == DriverPanel.Direction.Down) {
                for (int i = 0; i < this.length; i++) {
                    YPositions[i] = ((startYPosition - i) * unitSize);
                }
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setDirection(DriverPanel.Direction direction) {
        this.car.direction = direction;
    }

    public boolean isRunning() {
        return running;
    }

    public DriverPanel.Direction getDirection() {
        return this.car.direction;
    }
}
