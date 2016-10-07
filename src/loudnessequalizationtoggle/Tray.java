/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loudnessequalizationtoggle;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.HeadlessException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javafx.application.Platform;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

/**
 *
 * @author Kofola
 */
public class Tray {

    private final Stage stage;
    private TrayIcon icon;
    private CheckboxMenuItem leProperty;

    public Tray(Stage stage, String imagePath) {

        this.stage = stage;
        hookTray(imagePath);
    }

    public void showMessage(String message, TrayIcon.MessageType TYPE) {
        icon.displayMessage("Loudness Equalization Toggle", message, TYPE);
    }

    private void hookTray(String image) {

        try {
            icon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream(image)), "Loudness Equalization Toggle",
                    createPopupMenu());

            icon.addActionListener((ActionEvent e) -> {
                Platform.runLater(() -> {
                    stage.setIconified(false);//nevim co presne naco som t osem dal
                });

            });
            SystemTray.getSystemTray().add(icon);

            icon.displayMessage("Loudness Equalization", "Toggle Enabled",
                    TrayIcon.MessageType.INFO);

            Platform.setImplicitExit(false);

        } catch (AWTException | IOException ex) {
        }

    }

    public PopupMenu createPopupMenu() throws HeadlessException {
        PopupMenu menu = new PopupMenu();

        MenuItem exit = new MenuItem("Exit");
        MenuItem showWindow = new MenuItem("Show window");
        leProperty = new CheckboxMenuItem("Loudness Equalization");
        leProperty.setEnabled(false);
        showWindow.addActionListener((ActionEvent e) -> {
            Platform.runLater(() -> {
                try {
                    LoudnessEqualizationToggle.showMainWindow();
                } catch (Exception ex) {
                }
            });

        });

        exit.addActionListener((ActionEvent e) -> {
            try {
                LoudnessEqualizationToggle.nativeKeyHook.disable();
                GlobalScreen.unregisterNativeHook();
                System.exit(0);
            } catch (NativeHookException ex) {
            }
        });

        menu.add(showWindow);
        menu.add(leProperty);
        menu.add(exit);

        return menu;
    }

    public CheckboxMenuItem getLeProperty() {
        return leProperty;
    }

    public void setTrayImage(String imagePath) {
        try {
            icon.setImage(ImageIO.read(getClass().getResourceAsStream(imagePath)));
        } catch (IOException ex) {
        }
    }

}
