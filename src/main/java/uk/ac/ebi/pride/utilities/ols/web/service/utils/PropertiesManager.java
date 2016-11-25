package uk.ac.ebi.pride.utilities.ols.web.service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertiesManager {

    private static String initFilePath = "application.properties";
    private static Properties prop;

    private static void setInitFilePath() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream(initFilePath);
            prop = new Properties();
            if (input != null) {
                prop.load(input);
            }

        } catch (IOException ex) {
            Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getPropertyValue(String key) {
        if (prop == null){
            setInitFilePath();
        }
        return prop.getProperty(key);
    }

    public static Properties getProperties() {
        if (prop == null){
            setInitFilePath();
        }
        return prop;
    }
}