package com.workerai.utils.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TokenResponse {
    private final boolean automine, forage/*, farming, zealot, netherwart, fishing*/;

    public TokenResponse(boolean automine, boolean forage/*, boolean farming, boolean zealot, boolean netherwart, boolean fishing*/) {
        this.automine = automine;
        this.forage = forage;
        /*this.farming = farming;
        this.zealot = zealot;
        this.netherwart = netherwart;
        this.fishing = fishing;//*/
    }

    public static TokenResponse getTokenInformation(String token, String uuid) throws UserNotFoundException {
        try {
            URL url = new URL(String.format("http://localhost:2929/getUser?token=%s&uuid=%s", token, uuid));
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            JsonObject obj = (JsonObject) new JsonParser().parse(response.toString());

            if(!obj.get("exists").getAsBoolean())
                throw new UserNotFoundException();

            return new TokenResponse(obj.get("automine").getAsBoolean(), obj.get("forage").getAsBoolean()/*, obj.get("farming").getAsBoolean(), obj.get("zealot").getAsBoolean(), obj.get("netherwart").getAsBoolean(), obj.get("fishing").getAsBoolean()*/);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static class UserNotFoundException extends Exception {
        public UserNotFoundException() {
        }

        public UserNotFoundException(String message) {
            super(message);
        }

        public UserNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }

        public UserNotFoundException(Throwable cause) {
            super(cause);
        }

        public UserNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    public boolean hasAutomine() {
        return automine;
    }

    public boolean hasForage() {
        return forage;
    }

        /*public boolean hasFarming() {
            return farming;
        }

        public boolean hasZealot() {
            return zealot;
        }

        public boolean hasNetherwart() {
            return netherwart;
        }

        public boolean hasFishing() {
            return fishing;
        }*/
}
