package com.declanminer2005.minecraft.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Web {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DATA = "7EC0A69F72BBC9BE04FF6A8E419C01B87A16C716F9C081A7F7C91703E3A68C0C7E72B0E4CB";
    private static final String HOST = "minecrafthub52px.aternos.me";
    private static final int PORT = 80;
    private static final String URI = "/";

    public static void main(String[] args) throws IOException {
        StringBuilder postData = new StringBuilder();
        postData.append("data=").append(DATA);

        byte[] postDataBytes = postData.toString().getBytes(StandardCharsets.UTF_8);

        URL url = new URL("http", HOST, PORT, URI);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        con.setRequestProperty("X-Custom-Header", DATA);

        // Send post request
        con.setDoOutput(true);
        con.getOutputStream().write(postDataBytes);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Print result
        LOGGER.info(response.toString());
    }
}
