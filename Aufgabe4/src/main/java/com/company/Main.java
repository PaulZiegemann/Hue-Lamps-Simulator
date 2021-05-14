package com.company;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;


public class Main {

    //Globale Variablen
    private static final String TEST_STANDORT = "Lothstraße 64, München";
    private static final String HERE_API_KEY = "";
    private static final String OPENW_API_KEY = "";
    private static final String HOST = "localhost";
    private static final String COLOR_ORANGE = "4444";
    private static final String COLOR_GREEN = "23536";
    private static final String COLOR_RED = "65136";
    //Scanner um Eingaben in der Kommandozeile abzufangen
    Scanner sc = new Scanner(System.in);


    public static void main(String[] args) throws InterruptedException {
        Main test = new Main();
        //Zieladdresse eintippen
        String zielort = test.getInputAddress();
        Thread.sleep(5000);
        //speichere double Geokoordinaten WGS84 longitude und latitude ab
        double[] testArrayStart = test.get_coordinates_Request_HERE(TEST_STANDORT);
        double[] testArrayD = test.get_coordinates_Request_HERE(zielort);
        //speichere JSONObject aus here route Webservice abruf
        JSONObject jot = (test.get_time_needed(testArrayStart, testArrayD));
        //speichere doublewerte ab, sDur Zeit zum Ziel, wS Temperatur Standort, wD Temperatur Zielort
        double wS = test.get_weather(testArrayStart);
        double wD = test.get_weather(testArrayD);
        //Ändere Lampenfarben zu den gegebenen Werten um
        double sDur = test.light1_time_needed(jot);
        test.light2_weather_Start(wS);
        test.light3_weather_Dest(wD);
        System.out.println("Sie brauchen: " + (int)(sDur) + ":" + (int)((sDur-(int)sDur)*60) + " h"  + " zum Ziel und es ist " + (Math.round((wS - 273.15))) + " Grad am Standort und " + (Math.round((wD - 273.15))) + " Grad am Zielort.");


    }
    public String getInputAddress()  {
        String adresse = "";
        System.out.println("Stra�e und Hausnummer:");
        adresse += ", ";
        adresse += sc.nextLine();
        System.out.println("Ort");

        return adresse;
    }

    public double[] get_coordinates_Request_HERE(String ort) {

        HttpURLConnection con;
        String get = null;
        try {
            get = "https://geocode.search.hereapi.com/v1/geocode?q=" + URLEncoder.encode(ort, "UTF-8") + "&apiKey=" + HERE_API_KEY;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            // open the connection
            // - for unencrypted HTTP use "http://" as protocol specification
            // - for TLS/HTTPS simply use "https://" (Java library will handle encryption and certificate handling)
            con = (HttpURLConnection)
                    new URL(get).openConnection();


            try (BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()))) {

                JSONTokener tokener = new JSONTokener(bf);
                JSONObject json = new JSONObject(tokener);
                JSONArray items = json.getJSONArray("items");
                double lat = (items.getJSONObject(0).getJSONObject("position").getDouble("lat"));
                double lng = (items.getJSONObject(0).getJSONObject("position").getDouble("lng"));

                double pos[] = {lat, lng};
                return pos;


            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject get_time_needed(double[] start, double[] destination) {

        HttpURLConnection con;
        String get = null;

        get = "https://router.hereapi.com/v8/routes?transportMode=car&origin=" + start[0] + "," + start[1] + "&destination=" + destination[0] + "," + destination[1] + "&return=summary" + "&apiKey=" + HERE_API_KEY;

        try {
            // open the connection
            // - for unencrypted HTTP use "http://" as protocol specification
            // - for TLS/HTTPS simply use "https://" (Java library will handle encryption and certificate handling)
            con = (HttpURLConnection)
                    new URL(get).openConnection();

            try (BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                JSONTokener tokener = new JSONTokener(bf);
                JSONObject json = new JSONObject(tokener);

                return json;
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    double get_weather(double[] pos) {
        String get = "https://api.openweathermap.org/data/2.5/weather?lat=" + pos[0] + "&lon=" + pos[1] + "&appid=" + OPENW_API_KEY;

        HttpURLConnection con;
        double getW = 0;

        try {
            // open the connection
            // - for unencrypted HTTP use "http://" as protocol specification
            // - for TLS/HTTPS simply use "https://" (Java library will handle encryption and certificate handling)
            con = (HttpURLConnection)
                    new URL(get).openConnection();

            try (BufferedReader bf = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                JSONTokener tokener = new JSONTokener(bf);
                JSONObject json = new JSONObject(tokener);
                JSONObject main = (JSONObject) json.get("main");


                getW = main.getDouble("temp");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getW;

    }


        public void light2_weather_Start(double wStart){

        String url = "http://" + HOST + "/api/newdeveloper/lights/2/state";

        String alert;

        String color;

        if(wStart - 273.15 < 15){
            color = COLOR_RED;
            alert = "none";
        }else if(wStart - 273.15 > 15 && wStart - 273.15 < 20 ){
            color = COLOR_ORANGE;
            alert = "none";
        }else{
            color = COLOR_GREEN;
            alert = "none";
        }

        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut httpPut = new HttpPut(url);

            JSONObject json = new JSONObject();
            json.put("on", true);
            json.put("bri", 250);
            json.put("sat", 254);
            json.put("hue", color);
            json.put("alert", alert);
            json.put("transitiontime", 10);

            StringEntity str = new StringEntity(json.toString());

            httpPut.setEntity(str);

            client.execute(httpPut);



        } catch (IOException | JSONException e) {
            System.out.println("Verbindung zur HUE konnte nicht hergestellt werden!");
        }

    }

    public void light3_weather_Dest(double wStart){

        String url = "http://" + HOST + "/api/newdeveloper/lights/3/state";

        String alert;

        String color;

        if(wStart - 273.15 < 15){
            color = COLOR_RED;
            alert = "none";
        }else if(wStart - 273.15 > 15 && wStart - 273.15 < 20 ){
            color = COLOR_ORANGE;
            alert = "none";
        }else{
            color = COLOR_GREEN;
            alert = "none";
        }

        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut httpPut = new HttpPut(url);

            JSONObject json = new JSONObject();
            json.put("on", true);
            json.put("bri", 250);
            json.put("sat", 254);
            json.put("hue", color);
            json.put("alert", alert);
            json.put("transitiontime", 10);

            StringEntity str = new StringEntity(json.toString());

            httpPut.setEntity(str);

            client.execute(httpPut);

        } catch (IOException | JSONException e) {
            System.out.println("Verbindung zur HUE konnte nicht hergestellt werden!");
        }

    }




    public double light1_time_needed(JSONObject jo) {

        String url = "http://" + HOST + "/api/newdeveloper/lights/1/state";

        String alert;

        String color;

        JSONArray route = jo.getJSONArray("routes");
        JSONArray section = route.getJSONObject(0).getJSONArray("sections");
        JSONObject summary = section.getJSONObject(0).getJSONObject("summary");
        int duration = summary.getInt("duration");

        double duration_in_min = (duration / 60);

        if (duration_in_min < 10) {
            color = COLOR_GREEN;
            alert = "none";
        } else if (duration_in_min > 10 && duration_in_min < 30) {
            color = COLOR_ORANGE;
            alert = "none";
        } else if (duration_in_min > 30 && duration_in_min < 180) {
            color = COLOR_RED;
            alert = "none";
        } else {
            color = COLOR_RED;
            alert = "select";
        }


            try {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpPut httpPut = new HttpPut(url);

                JSONObject json = new JSONObject();
                json.put("on", true);
                json.put("bri", 250);
                json.put("sat", 254);
                json.put("hue", color);
                json.put("alert", alert);
                json.put("transitiontime", 10);

                StringEntity str = new StringEntity(json.toString());

                httpPut.setEntity(str);

                client.execute(httpPut);


            } catch (IOException | JSONException e) {
                System.out.println("Verbindung zur HUE konnte nicht hergestellt werden!");
            }
            return duration_in_min/60;

    }


}



