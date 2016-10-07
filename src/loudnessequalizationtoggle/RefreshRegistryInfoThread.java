/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loudnessequalizationtoggle;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;

/**
 * Set daemon thread
 *
 * @author Kofola
 */
public class RefreshRegistryInfoThread extends Thread {

    private final int REFRESH_RATE = 1000;
    //guarded by this
    public final SimpleStringProperty isActiveCurrently = new SimpleStringProperty("Loudness Equalization : Inactive");//if system has LE active

    public synchronized SimpleStringProperty getIsActiveCurrentlyProperty() {
        return isActiveCurrently;
    }

    public RefreshRegistryInfoThread() {

    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        while (true) {
            try {
                LoudnessEqualizationToggle.initRegistryCachedDeviceInfo();
                synchronized (LoudnessEqualizationToggle.registryCachedDeviceInfo) {

                    for (String guid : LoudnessEqualizationToggle.registryCachedDeviceInfo) {
                        int regValue = LoudnessEqualizationToggle.getRegLEValue(guid);
                        System.out.println("Value that is in the registry :" + regValue);
                        Platform.runLater(() -> {
                            isActiveCurrently.set((regValue == 1) ? "Loudness Equalization : Active" : "Loudness Equalization : Inactive");
                            LoudnessEqualizationToggle.getTray().getLeProperty().setState(regValue == 1);
                        });
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(REFRESH_RATE);
            } catch (InterruptedException ex) {
                Logger.getLogger(RefreshRegistryInfoThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
