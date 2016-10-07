/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loudnessequalizationtoggle;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 *
 * @author Kofola
 */
public class NativeKeyHook implements NativeKeyListener {

    public static final int DEFUALT_NKE = NativeKeyEvent.VC_CONTROL_R;
    private static int LT_NKE = LoudnessEqualizationToggle.getPrefs().getInt(LoudnessEqualizationToggle.PREFS_TOGGLE_KEY, DEFUALT_NKE);

    private boolean keyDetectionFlag;

    public void setKeyDetectionFlag(boolean keyDetectionFlag) {
        this.keyDetectionFlag = keyDetectionFlag;
    }

    public static int getLT_NKE() {
        return LT_NKE;
    }

    public static void setLT_NKE(int nke) {
        LT_NKE = nke;
    }

    public void enable() {
        GlobalScreen.addNativeKeyListener(this);
    }

    public void disable() {
        GlobalScreen.removeNativeKeyListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nke) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        if (keyDetectionFlag) {
            FXMLMainViewController.latestKeyPressed = nke.getKeyCode();
        }

    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nke) {
        if (nke.getKeyCode() == LT_NKE) {

            new Thread(() -> {
                //reg add "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\MMDevices\Audio\Render\{d348b8e8-3118-4a9c-9b43-422647b555ca}\FxProperties" /f /v "{E0A941A0-88A2-4df5-8D6B-DD20BB06E8FB},4" /t REG_DWORD /d "0"
                for (String guid : LoudnessEqualizationToggle.registryCachedDeviceInfo) {
                    System.out.println("WIRING TO REGISTRY");
                        // LoudnessEqualizationToggle.writeToLEProperties(guid, activateLEOnKey);
                    //HKEY_LOCAL_MACHINE\SOFTWARE\Microsoft\Windows\CurrentVersion\MMDevices\Audio\Render\{d348b8e8-3118-4a9c-9b43-422647b555ca}\FxProperties
                    System.out.println("GUID:" + guid);
                    LoudnessEqualizationToggle.regWriter.switchValue();
                    System.out.println("WIRING TO REGISTRY-done");
                }
            }).start();

        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nke) {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
