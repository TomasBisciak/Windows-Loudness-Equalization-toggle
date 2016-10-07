/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loudnessequalizationtoggle;

import java.awt.TrayIcon;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

/**
 *
 * @author Kofola
 */
public class LoudnessEqualizationToggle extends Application {

    /**
     * Reusable primary stage for window
     */
    public static Stage primStage;
    /**
     * Currently used Application port
     */
    private static int appPort;

    private static Tray tray;
    /**
     * Socket that this application uses and blocks any other instance that is requested to run/ there is a way to walk around this by specifying port for the application
     */
    private static ServerSocket socket;
    public static final int DEFAULT_APP_PORT = 64875;
    private static final Preferences appPref = Preferences.userRoot().node("letoggle/app");

    //can PROBABLY hold multiple devices im not even sure thats possible in windows bud ...no idea jsut in case
    public static final CopyOnWriteArrayList<String> registryCachedDeviceInfo = new CopyOnWriteArrayList<>();

    public static final NativeKeyHook nativeKeyHook = new NativeKeyHook();;

    public static final String PREFS_SHOW_MAIN_WIN = "dsmw";
    public static final String PREFS_TOGGLE_KEY = "togglekeycode";
    //public static final String PREFS_AUTOSTART = "autostart";
    public static final RegistryWriterThread regWriter = new RegistryWriterThread();
    public static final RefreshRegistryInfoThread refreshRegInfoThread = new RefreshRegistryInfoThread();

    /**
     * Checks if application is running Used mainly for notification after application close/tray enabled
     */
    private static boolean isRunning;

    public static Tray getTray() {
        return tray;
    }

    @Override
    public void start(Stage stage) throws Exception {
        //on condition that.
        executeOnStart();
        regWriter.start();
        refreshRegInfoThread.start();
    }

    //called only from writer thread
    public static final void execCmdNoReturn(String cmd) throws IOException {
        Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        System.out.println("Command:" + cmd);
        System.out.println("Output:");
        while (s.hasNext()) {
            System.out.println(s.next());
        }
    }

    public static synchronized int getRegLEValue(String GUID) {
        try {
            /*
             return  Integer.valueOf( new Scanner(Runtime.getRuntime().exec("reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\"
             + "MMDevices\\Audio\\Render\\" + GUID + "\\FxProperties\""
             + " /v {E0A941A0-88A2-4df5-8D6B-DD20BB06E8FB},4").getInputStream()).next().split("\\W+")[1]);
             */
            Scanner s = new Scanner(Runtime.getRuntime().exec("reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\"
                    + "MMDevices\\Audio\\Render\\" + GUID + "\\FxProperties\""
                    + " /v {E0A941A0-88A2-4df5-8D6B-DD20BB06E8FB},4").getInputStream());
            System.out.println("GUID:" + GUID + ":");
            System.out.println(s.next());//skip over
            System.out.println(s.next());
            System.out.println(s.next());
            String dataString=s.next();
            System.out.println("DEBUG: "+dataString);
            int data = Integer.valueOf(dataString.split("x")[1]);
            System.out.println("Data in registry:" + data);
            return data;

        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(LoudnessEqualizationToggle.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Cannot access value.");
            return -1;
        }
    }

    public static synchronized final void initRegistryCachedDeviceInfo() {
        try {
            registryCachedDeviceInfo.removeAll(LoudnessEqualizationToggle.registryCachedDeviceInfo);
            String[] debug=LoudnessEqualizationToggle.execCmd("LoudnessEqualizationToggleNative").split("%");
            for(String s:debug){
                System.out.println("debug:"+s);
            }
            registryCachedDeviceInfo.addAll(Arrays.asList(debug));
            parseOutput(LoudnessEqualizationToggle.registryCachedDeviceInfo);
        } catch (IOException ex) {
            Logger.getLogger(RefreshRegistryInfoThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static final String execCmd(String cmd) throws java.io.IOException {
        Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        String str = "";//no need for stringBuilder, small output.
        while (s.hasNext()) {
            str += str + s.next() + "%";
        }
        return str;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        processArguments(args);
        System.out.println("Starting application");
        initialize();
        launch(args);

    }

    private static void initialize() {
        hookNativeKeyListener();
        shutdownHook();//not sure if needed
        //start refreshThread
        tray = new Tray(primStage, "megaphone16x16.png");
    }

    /**
     * Loads applicaiton main frame pane, and first application view into dynamic pane.
     *
     * @return Main application frame pane "ScrollPane in this case".
     * @throws IOException If path to fxml is invalid.
     */
    /**
     * Execute on application startup , recognizes if app FTR
     */
    private static void executeOnStart() {

        if (!appPref.getBoolean(PREFS_SHOW_MAIN_WIN, false)) {
            showMainWindow();
        }

    }

    public static Preferences getPrefs() {
        return appPref;
    }

    /*
     public static void copyToStartupFolder() throws IOException{
     execCmdNoReturn("robocopy \\ \"%APPDATA%\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\" LoudnessEqualizationToggleBatchShortcut.lnk");
     }
     public static void removeFromStartupFolder() throws IOException{
     execCmdNoReturn("if exist \"%APPDATA%\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\LoudnessEqualizationToggleBatchShortcut.lnk\" "
     + "del \"%APPDATA%\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\LoudnessEqualizationToggleBatchShortcut.lnk\"");
     }
     */
    public static void showMainWindow() {
        Platform.runLater(() -> {
            primStage = new Stage();
            primStage.setTitle("Loudness Equalization  Toggle - 1.0");
            primStage
                    .getIcons().add(new Image(LoudnessEqualizationToggle.class
                                    .getResourceAsStream("megaphone.png")));

            try {
                primStage.setScene(new Scene((BorderPane) FXMLLoader.load(LoudnessEqualizationToggle.class.getResource("FXMLMainView.fxml"))));

            } catch (IOException ex) {
                System.out.println("Problem at show main window");
            }

            primStage.setOnCloseRequest((WindowEvent event) -> {
                tray.showMessage("Application still active.", TrayIcon.MessageType.INFO);

            });

            primStage.show();
        });
    }

    private static void hookNativeKeyListener() {
        new Thread(() -> {
            try {
                GlobalScreen.registerNativeHook();
                System.out.println("Hook registered");
            } catch (NativeHookException ex) {
                System.out.println(ex.getMessage());
                //TODO notify user that globalkey listener doesnt work.
                System.exit(1);
            }
            System.out.println("Hook state: " + GlobalScreen.isNativeHookRegistered());
            GlobalScreen.addNativeKeyListener(nativeKeyHook);
            //DISABLE LOGGING FOR HOOK
            Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
        }).start();

    }

    private static synchronized CopyOnWriteArrayList<String> parseOutput(CopyOnWriteArrayList<String> output) {//returns all the devices basedo n output from out c++ application

        try {
            System.out.println("Parsing output :" + output);
            for (int i = 0; i < output.size(); i++) {
                output.set(i, output.get(i).split("}\\.")[1]);
                output.set(i, output.get(i).replace("\n", "").replace("\r", ""));
            }
        } catch (Exception e) {
            System.out.println("Problem parsing string");
            e.printStackTrace();
        }

        return output;

    }

    private static void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                //execute before shutdown
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException ex) {
                Logger.getLogger(LoudnessEqualizationToggle.class.getName()).log(Level.SEVERE, null, ex);
            }

        }));
    }

    /**
     *
     * @param args passed into application
     */
    public static void processArguments(String[] args) {
        //holds which values were sucessfully parsed/used
        boolean[] paramFlags = new boolean[2];
        if (args.length != 0) {

            //switch case to check parameters
            for (int i = 0; i < args.length; i += 2) {

                switch (args[i]) {
                    case "-p": {
                        try {
                            if (!occupyPort(Integer.valueOf(args[i + 1]))) {
                                System.out.println("Port" + Integer.valueOf(args[i + 1]) + " cannot be occupied by application");
                                continue;
                            }
                            paramFlags[0] = true;
                        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                            System.err.println("Argument is not a valid number/argument not found");
                        }
                        break;
                    }
                    case "": {

                        break;
                    }
                    default: {
                        System.out.println("Invalid parameter \" " + args[i] + " \",use default execution? Y/N:");
                        if (new Scanner(System.in).nextLine().equalsIgnoreCase("y")) {

                        }
                    }

                }

            }

        } else {
            //use default values
            for (int i = 0; i < paramFlags.length; i++) {
                switch (i) {
                    case 0: {
                        if (!paramFlags[i]) {
                            occupyPort(DEFAULT_APP_PORT);
                        }
                        break;
                    }
                    case 1: {

                        break;
                    }

                }

            }

        }

    }

    /**
     * Opens a ServerSocket for OpenChannel aplication making sure that only one instance is running at the time, if not specified otherwise by providing specified ports that differ from each other and fall within range.
     *
     * @param port port to be occupied by application valid range 49152-65535
     */
    private static boolean occupyPort(int port) {

        if (!(port >= 49152 && port <= 65535)) { //(IANA) suggested range
            System.err.println("Not a valid port number.");
            return false;
        }
        try {
            socket = new ServerSocket(port, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
            appPort = port;
        } catch (IOException ex) {
            System.err.println("Application already running/Port occupied by a process.");
            ex.printStackTrace();
            //TODO popup inform user
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex1) {
                ex1.printStackTrace();
            }
            System.exit(1);
        }
        return false;
    }

}
