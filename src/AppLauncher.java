import org.json.simple.JSONArray;

import javax.swing.SwingUtilities;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // display weather forecast GUI
            WeatherAppGui weatherAppGui = new WeatherAppGui();
            weatherAppGui.setVisible(true);

            // Retrieve and display location data
            JSONArray locationData = WeatherApp.getLocationData("Tokyo");
            System.out.println(locationData);

            // Retrieve and display current time
            String currentTime = WeatherApp.getCurrentTime();
            System.out.println(currentTime);
        });
    }
}