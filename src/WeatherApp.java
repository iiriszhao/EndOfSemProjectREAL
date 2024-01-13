import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java. util. Scanner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

// retrieve weather data from API - will determine the latest weather data from the
// external API and return it. The GUI will
// display data to the user
public class WeatherApp {
   // fetch weather data for given loction
   public static JSONObject getWeatherData(String locationName) {
       // get location coordinates using the geolocation API
       JSONArray locationData = getLocationData(locationName);

       if (locationData == null || locationData.isEmpty()){
           System.out.println("Error: Could not retrieve location data");
           return null;
       }

       // extract: latitude and longitude data
       JSONObject location = (JSONObject) locationData.get(0);
       double latitude = (double) location.get("latitude");
       double longitude = (double) location.get("longitude");

       // build API request URL with location coordinates
       String urlString = "https://api.open-meteo.com/v1/forecast?" +
       "latitude=" + latitude + "&longitude=" + longitude +
       "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m";

       try{
           // call api and get response
           HttpURLConnection conn = fetchApiResponse(urlString);

           // check for response status
           // 200 - means connection succesful
           if(conn.getResponseCode() != 200){
               System.out.println("Error: Could not connect to API");
               return null;
           }

           // store resulting JSON data
           StringBuilder resultJson = new StringBuilder();
           Scanner scanner = new Scanner(conn.getInputStream());
           while(scanner.hasNext()){
               //read and store into string builder
               resultJson.append(scanner.nextLine());
           }

           // close scanner
           scanner.close();

           // close url connection
           conn.disconnect();

           // parse through our data
           JSONParser parser = new JSONParser();
           JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

           // retrieve hourly data
           JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");

           // want to get the current hour's data
           // need to get the index of current hour
           JSONArray time = (JSONArray) hourly.get("time");
           int index = findIndexOfCurrentTime(time);

           // get temperature
           JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
           double temperature = (double) temperatureData.get(index);

           // get weather code
           JSONArray weathercode = (JSONArray) hourly.get("weather_code");
           String weatherCondition = convertWeatherCode((long) weathercode.get(index));

           // get humidity
           JSONArray relativeHumidity = (JSONArray) hourly.get("relativeHumidity_2m");
           long humidity = (long) relativeHumidity.get(index);

           // get windspeed
           JSONArray windspeedData = (JSONArray) hourly.get("windspeed_10m");
           double windspeed = (double) windspeedData.get(index);

           // build the weather json data object to access in frontend
           JSONObject weatherData = new JSONObject();
           weatherData.put("temperature", temperature);
           weatherData.put("weather_condition", weatherCondition);
           weatherData.put("windspeed", windspeed);

           return weatherData;

       }catch(Exception e){
           e.printStackTrace();
       }

       return null;
   }
   public static JSONArray getLocationData(String locationName) {
       // replace any whitespace in location name to + to adhere to API's request format.";
       locationName = locationName.replaceAll(" ", "+");

       // build API url with location parameter
       String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
               locationName + "&count=10&language=en&format=json";
       try{
           // call api and get a response
           HttpURLConnection conn = fetchApiResponse(urlString);

           // check response status
           // 200 means successful connection
           if(conn.getResponseCode() != 200){
               System.out.println("Error: Could not connect to API");
               return null;
           }else{
               // store the api results
               StringBuilder resultJson = new StringBuilder();
               Scanner scanner = new Scanner(conn.getInputStream());

               // read and store the resulting Json data into our string builder
               while(scanner.hasNext()){
                   resultJson.append(scanner.nextLine());

               }

               // close scanner
               scanner.close();

               // close url connection
               conn.disconnect();

               // parse the JSON string into a JSON object
               JSONParser parser = new JSONParser();
               JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

               // get the list of location data the API generated from the location name
               JSONArray locationData  = (JSONArray) resultsJsonObj.get("results");
               return locationData;
           }

       }catch(Exception e){
           e.printStackTrace();

       }

       // couldn't find location
       return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString){
       try{
           // attempt to create connection
           URL url = new URL(urlString);
           HttpURLConnection conn = (HttpURLConnection) url.openConnection();

           // set request method to get
           conn.setRequestMethod("GET");

           // connect to our api
           conn.connect();
           return conn;
       }catch(IOException e){
           e.printStackTrace();
       }

       return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList){
       String currentTime = getCurrentTime();

       // iterate through the time list and see which one matches the current time
        for(int i=0; i <timeList.size(); i++){
            String time = (String) timeList.get(i);
            if(time.equalsIgnoreCase(currentTime)){
                //return index
                return i;
            }

        }
        return 0;
    }
    public static String getCurrentTime(){
       // get current data and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // format date to be 2024-01-13T00:00 (how it's read in the API)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00");

        // format and print the current date and time
        String formattedDateTime = currentDateTime.format(formatter);

        return formattedDateTime;

    }

    // convert the weather code to something more readable
    private static String convertWeatherCode(long weathercode) {
        String weatherCondition = "";
        if (weathercode == 0L) {
            weatherCondition = "Clear";
        } else if (weathercode <= 3L && weathercode > 0L) {
            weatherCondition = "Cloudy";
        } else if ((weathercode >= 51L && weathercode <= 67L)
                || (weathercode >= 80L && weathercode <= 99L)) {
            // rain
            weatherCondition = "Rain";
        } else if (weathercode >= 71L && weathercode <= 77L) {
            // snow
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }
}
