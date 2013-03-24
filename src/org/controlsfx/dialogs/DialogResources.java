package org.controlsfx.dialogs;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.controlsfx.tools.SVGLoader;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/**
 *
 */
class DialogResources {
    
    // Localization strings.
    private static ResourceBundle rbFX;

    static {
        reset();
    }

    static void reset() {
        rbFX = ResourceBundle.getBundle("impl.org.controlsfx.dialogs.resources.dialog-resources");
    }
    
    
    /**
     * Method to get an internationalized string from the deployment resource.
     */
    static String getMessage(String key) {
        try {
            return rbFX.getString(key);
        } catch (MissingResourceException ex) {
            // Do not trace this exception, because the key could be
            // an already translated string.
            System.out.println("Failed to get string for key '" + key + "'");
            return key;
        }
    }
    
    /**
    * Returns a string from the resources
    */
    static String getString(String key) {
        try {
            return rbFX.getString(key);
        } catch (MissingResourceException mre) {
            // Do not trace this exception, because the key could be
            // an already translated string.
            System.out.println("Failed to get string for key '" + key + "'");
            return key;
        }
    }

    /**
    * Returns a string from a resource, substituting argument 1
    */
    static String getString(String key, Object... args) {
        return MessageFormat.format(getString(key), args);
    }


    /**
     * Returns an <code>ImageView</code> given an image file name or resource name
     */
    static public ImageView getIcon(final String key) {
        try {
            return AccessController.doPrivileged(
                    new PrivilegedExceptionAction<ImageView>()   {
                        @Override public ImageView run() {
                            return getIcon_(key);
                        }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    static public ImageView getIcon_(String key) {
        String resourceName = getString(key);
        URL url = ControlResources.class.getResource(resourceName);
        if (url == null) {
            System.out.println("Can't create ImageView for key '" + key + 
                    "', which has resource name '" + resourceName + 
                    "' and URL 'null'");
            return null;
        }
        return getIcon(url);
    }

    static public ImageView getIcon(URL url) {
        if (url.toString().endsWith(".svg")) {
            WritableImage image = new WritableImage(48, 48);
            SVGLoader.loadSVGImage(url, image);
            return new ImageView(image);
        }
        return new ImageView(new Image(url.toString()));
    }
}