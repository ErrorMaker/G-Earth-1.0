package gearth.ui.extensions.authentication;

import gearth.extensions.Extension;
import gearth.misc.ConfirmationDialog;
import gearth.ui.extensions.GEarthExtension;
import gearth.ui.extensions.executer.ExtensionRunner;
import gearth.ui.extensions.executer.ExtensionRunnerFactory;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Jonas on 16/10/18.
 */
public class Authenticator {

    private static Map<String, String> cookies = new HashMap<>();

    public static String generateCookieForExtension(String filename) {
        String cookie = getRandomCookie();
        cookies.put(filename, cookie);

        return cookie;
    }

    public static boolean evaluate(GEarthExtension extension) {
        if (extension.isInstalledExtension()) {
            return claimSession(extension.getFileName(), extension.getCookie());
        }
        else {
            return askForPermission(extension);
        }
    }

    /**
     * authenticator: authenticate an extension and remove the cookie
     * @param filename
     * @param cookie
     * @return if the extension is authenticated
     */
    private static boolean claimSession(String filename, String cookie) {
        if (cookies.containsKey(filename) && cookies.get(filename).equals(cookie)) {
            cookies.remove(filename);
            return true;
        }
        return false;
    }

    private static volatile boolean rememberOption = false;
    //for not-installed extensions, popup a dialog
    private static boolean askForPermission(GEarthExtension extension) {
        boolean[] allowConnection = {true};

        final String connectExtensionKey = "allow_extension_connection";

        if (ConfirmationDialog.showDialog(connectExtensionKey)) {
            boolean[] done = {false};
            Platform.runLater(() -> {
                Alert alert = ConfirmationDialog.createAlertWithOptOut(Alert.AlertType.WARNING, connectExtensionKey
                        ,"Confirmation Dialog", null,
                        "Extension \""+extension.getTitle()+"\" tries to connect but isn't known to G-Earth, accept this connection?", "Remember my choice",
                        ButtonType.YES, ButtonType.NO
                );

                if (!(alert.showAndWait().filter(t -> t == ButtonType.YES).isPresent())) {
                    allowConnection[0] = false;
                }
                done[0] = true;
                if (!ConfirmationDialog.showDialog(connectExtensionKey)) {
                    rememberOption = allowConnection[0];
                }
            });

            while (!done[0]) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return allowConnection[0];
        }

        return rememberOption;
    }

    private static String getRandomCookie() {
        StringBuilder builder = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < 40; i++) {
            builder.append(r.nextInt(40));
        }

        return builder.toString();
    }
}
