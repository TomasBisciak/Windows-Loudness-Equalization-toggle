/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loudnessequalizationtoggle;

import java.awt.TrayIcon;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import static loudnessequalizationtoggle.LoudnessEqualizationToggle.PREFS_SHOW_MAIN_WIN;
import org.jnativehook.keyboard.NativeKeyEvent;

/**
 *
 * @author Kofola
 */
public class FXMLMainViewController implements Initializable {

    @FXML
    private Button btnCloseAndCreate;
    @FXML
    private CheckBox chckBoxDSW;
    @FXML
    private Label lblActivity;

    @FXML
    private void toTray(ActionEvent event) {
        LoudnessEqualizationToggle.primStage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

//        toggleButtonAROS.selectedProperty().addListener((value, oldVal, newVal) -> {
//
//            if (newVal) {
//                toggleButtonAROS.setStyle("-fx-background-color:#8e44ad;-fx-background-radius:0;");
//                //write to REGISTRY TO ATUOSTARTUP 
//            } else {
//                toggleButtonAROS.setStyle("-fx-background-color:#2980b9;-fx-background-radius:0;");
//                //write  to REGISTRY TO AUTOSTARTUP
//            }
//
//        });
        chckBoxDSW.setSelected(LoudnessEqualizationToggle.getPrefs().getBoolean(PREFS_SHOW_MAIN_WIN, false));
        chckBoxDSW.selectedProperty().addListener((value, oldVal, newVal) -> {
            if (newVal) {
                LoudnessEqualizationToggle.getPrefs().putBoolean(LoudnessEqualizationToggle.PREFS_SHOW_MAIN_WIN, true);
            } else {
                LoudnessEqualizationToggle.getPrefs().putBoolean(LoudnessEqualizationToggle.PREFS_SHOW_MAIN_WIN, false);
            }

        });
        lblActivity.textProperty().bind(LoudnessEqualizationToggle.refreshRegInfoThread.getIsActiveCurrentlyProperty());
        txtFieldDetect.setText(NativeKeyEvent.getKeyText(NativeKeyHook.getLT_NKE()));
        /*
           toggleButtonAROS.setSelected(LoudnessEqualizationToggle.getPrefs().getBoolean(LoudnessEqualizationToggle.PREFS_AUTOSTART, false));\
       
        toggleButtonAROS.selectedProperty().addListener((val, oldVal, newVal) -> {
            if (newVal) {
                System.out.println("Copy file to startup folder");
                try {
                    LoudnessEqualizationToggle.copyToStartupFolder();
                    LoudnessEqualizationToggle.getPrefs().putBoolean(LoudnessEqualizationToggle.PREFS_AUTOSTART, true);
                } catch (Exception e) {
                    System.out.println("997Problem");
                    LoudnessEqualizationToggle.getTray().showMessage("Cannot be done.Maybe required file is not in your directory?", TrayIcon.MessageType.ERROR);
                    e.printStackTrace();
                }
            } else {
                System.out.println("Delet file from startup folder");

                try {
                    LoudnessEqualizationToggle.removeFromStartupFolder();
                    LoudnessEqualizationToggle.getPrefs().putBoolean(LoudnessEqualizationToggle.PREFS_AUTOSTART, false);
                } catch (Exception e) {
                        System.out.println(" 997Problem");
                    LoudnessEqualizationToggle.getTray().showMessage("Cannot be done.Maybe required file is not in your directory?", TrayIcon.MessageType.ERROR);
                    e.printStackTrace();
                }

            }
        });
        */
     
    }

    @FXML
    private Button btnDetect;
    @FXML
    private TextField txtFieldDetect;

    public static int latestKeyPressed = Integer.MIN_VALUE;

    @FXML
    @SuppressWarnings("SleepWhileInLoop")
    private void detect() {
        new Thread(() -> {
            while (latestKeyPressed == Integer.MIN_VALUE) {
                LoudnessEqualizationToggle.nativeKeyHook.setKeyDetectionFlag(true);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLMainViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
                //Unknown keyCode: 0x-80000000
                if (!NativeKeyEvent.getKeyText(latestKeyPressed).equals("Unknown keyCode: 0x-80000000")) {
                    NativeKeyHook.setLT_NKE(latestKeyPressed);
                    LoudnessEqualizationToggle.getPrefs().putInt(LoudnessEqualizationToggle.PREFS_TOGGLE_KEY, latestKeyPressed);
                    txtFieldDetect.setText(NativeKeyEvent.getKeyText(latestKeyPressed));
                    break;
                }
            }
            LoudnessEqualizationToggle.nativeKeyHook.setKeyDetectionFlag(false);
            latestKeyPressed = Integer.MIN_VALUE;

        }).start();
    }

}
