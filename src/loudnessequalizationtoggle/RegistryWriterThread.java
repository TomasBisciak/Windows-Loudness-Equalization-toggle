/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loudnessequalizationtoggle;

import java.awt.TrayIcon;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kofola
 */
public class RegistryWriterThread extends Thread {

    //guarded by 'this'
    private volatile boolean writeRequestPending;
    //guarded by 'this'

    public RegistryWriterThread() {
        this.setDaemon(true);
    }

    public synchronized void switchValue() {
        writeRequestPending = true;
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        while (true) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {//should not happen
                System.out.println("Writer thread interupted.");
                Logger.getLogger(RegistryWriterThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (writeRequestPending) {
                System.out.println("WRITE Request Pending");
                try {
                    LoudnessEqualizationToggle.initRegistryCachedDeviceInfo();
                    int audioReset = 1;
                    //check what is supposed to be written and write it.
                    int regValue = -1;
                  
                        for (String guid : LoudnessEqualizationToggle.registryCachedDeviceInfo) {
                            regValue = LoudnessEqualizationToggle.getRegLEValue(guid);
                            System.out.println("VALUE THATS IN THE REGISTRY ATM (BEFORE WRITE):" + regValue);
                            if (regValue == -1) {
                                System.out.println("Cant read value,leave as is.");
                                //show tray error
                                audioReset = 0;
                                break;
                            } else {
                                LoudnessEqualizationToggle.execCmdNoReturn("reg add \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\MMDevices\\Audio\\Render\\" + guid + "\\FxProperties\""
                                        + " /f /v \"{E0A941A0-88A2-4df5-8D6B-DD20BB06E8FB},4\" /t REG_DWORD /d \""
                                        /*first i check if value has been read correctly , then in case it was i turn aroudn value that was read and write it, 
                                         otherwise i write value that was already present there, i know this value from value to write, in order 
                                         for this to work i have to keep track of what kind of value is there with daemon*/
                                        + (regValue == 0 ? 1 : 0) + "\"");
                            }
                        }
                    

                    //restart audio
                    //needs permissions
                    if (audioReset == 1) {
                        System.out.println("Restarting audio.");
                        LoudnessEqualizationToggle.execCmdNoReturn("net stop audiosrv");
                        LoudnessEqualizationToggle.execCmdNoReturn("net stop AudioEndpointBuilder");
                        LoudnessEqualizationToggle.execCmdNoReturn("net start audiosrv");
                        LoudnessEqualizationToggle.execCmdNoReturn("net start AudioEndpointBuilder");
                        System.out.println("Audio restarted.");
                        LoudnessEqualizationToggle.getTray().showMessage((regValue == 0) ? "Loudness Equalization : Active" : "Loudness Equalization : Inactive", TrayIcon.MessageType.INFO);
                    }
                    //after all done write request is false,we dont care if user spams it
                    writeRequestPending = false;
                    System.out.println("New value written into registry.");
                } catch (IOException ex) {
                    Logger.getLogger(RegistryWriterThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }
}
