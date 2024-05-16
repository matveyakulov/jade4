package car;

import javax.swing.*;

public class CarFrame extends JFrame {

    private CarPanel carPanel = new CarPanel();
    public CarFrame() {
        this.add(carPanel);
        this.setTitle("Car energy is 100%");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }

    public CarPanel getCarPanel() {
        return carPanel;
    }

    public void updateEnergy(int energy) {
        this.setTitle("Car energy is " + energy + "%");
        repaint();
    }
}
