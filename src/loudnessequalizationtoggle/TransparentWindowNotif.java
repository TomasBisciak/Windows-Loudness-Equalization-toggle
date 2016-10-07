/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loudnessequalizationtoggle;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Window;

/**
 *
 * @author Kofola
 */
public class TransparentWindowNotif {

    private Window w;
    private String notification;

    public TransparentWindowNotif(String notificationString) {
        notification = notificationString;
        w = new Window(null) {
            @Override
            public void paint(Graphics g) {
                Font font = getFont().deriveFont(20.0F);
                g.setColor(Color.BLACK);
                FontMetrics metrics = g.getFontMetrics();
                g.setFont(font);
                g.drawString(notification, ShiftWest((getWidth() - metrics.stringWidth(notification)) / 2, 1), ShiftNorth((getHeight() - metrics.getHeight()) / 2, 1));
                g.drawString(notification, ShiftWest((getWidth() - metrics.stringWidth(notification)) / 2, 1), ShiftSouth((getHeight() - metrics.getHeight()) / 2, 1));
                g.drawString(notification, ShiftEast((getWidth() - metrics.stringWidth(notification)) / 2, 1), ShiftNorth((getHeight() - metrics.getHeight()) / 2, 1));
                g.drawString(notification, ShiftEast((getWidth() - metrics.stringWidth(notification)) / 2, 1), ShiftSouth((getHeight() - metrics.getHeight()) / 2, 1));
                g.setColor(Color.WHITE);
                g.drawString(notification, (getWidth() - metrics.stringWidth(notification)) / 2, (getHeight() - metrics.getHeight()) / 2);
            }

            @Override
            public void update(Graphics g) {
            }

            int ShiftNorth(int p, int distance) {
                return (p - distance);
            }

            int ShiftSouth(int p, int distance) {
                return (p + distance);
            }

            int ShiftEast(int p, int distance) {
                return (p + distance);
            }

            int ShiftWest(int p, int distance) {
                return (p - distance);
            }
        };

        w.setAlwaysOnTop(
                true);
        w.setBounds(w.getGraphicsConfiguration().getBounds());
        w.setBackground(
                new Color(0, true));
        w.setFocusable(false);
        w.setVisible(false);

    }

    public void notify(String notification, boolean visibility) {
        this.notification = notification;
        w.validate();
        w.repaint();
        w.setVisible(visibility);
    }

}
