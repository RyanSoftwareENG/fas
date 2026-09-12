package settings;

import java.io.*;
import java.util.Properties;

public class AppSettings {

    private static final String FILE_NAME = System.getProperty("user.home") + File.separator + ".FAS";
    private Properties properties;

    public AppSettings() {
        properties = new Properties();
        loadSettings();
    }

    //  دالة لتحميل الإعدادات عند فتح البرنامج
    private void loadSettings() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
            } catch (IOException e) {
            }
        } else {
            setDefaultSettings();
        }
    }

    //  دالة لتعيين القيم الافتراضية
    private void setDefaultSettings() {
        properties.setProperty("window_width", "800.0");
        properties.setProperty("window_height", "600.0");
        properties.setProperty("window_pos_x", "100.0");
        properties.setProperty("window_pos_y", "100.0");
        properties.setProperty("is_maximized", "false");
        properties.setProperty("last_view", "Home");
    }

    // دالة لحفظ اعدادت البرنامج الحالية
    public void saveSettings(double width, double height, double posX, double posY, boolean isMaximized, String lastView) {
        if (!isMaximized) {
            properties.setProperty("window_width", String.valueOf(width));
            properties.setProperty("window_height", String.valueOf(height));
            properties.setProperty("window_pos_x", String.valueOf(posX));
            properties.setProperty("window_pos_y", String.valueOf(posY));
        }
        properties.setProperty("is_maximized", String.valueOf(isMaximized));
        properties.setProperty("last_view", lastView);

        try (OutputStream output = new FileOutputStream(FILE_NAME)) {
            properties.store(output, "System Configuration Settings");
        } catch (IOException e) {
            System.out.println("حدث خطأ أثناء حفظ ملف الإعدادات: " + e.getMessage());
        }
    }

    public double getWidth() {
        return Double.parseDouble(properties.getProperty("window_width", "800.0"));
    }

    public double getHeight() {
        return Double.parseDouble(properties.getProperty("window_height", "600.0"));
    }

    public double getPosX() {
        return Double.parseDouble(properties.getProperty("window_pos_x", "100.0"));
    }

    public double getPosY() {
        return Double.parseDouble(properties.getProperty("window_pos_y", "100.0"));
    }

    public boolean isMaximized() {
        return Boolean.parseBoolean(properties.getProperty("is_maximized", "false"));
    }

    public String getLastView() {
        return properties.getProperty("last_view", "Home");
    }
}