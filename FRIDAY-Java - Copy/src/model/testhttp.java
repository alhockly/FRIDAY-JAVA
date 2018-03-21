package model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class testhttp {



    public static String HTTPGET(String IP, String path) throws Exception {

        String url="http://"+IP+path;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        //System.out.println(response.toString());



        return response.toString();

    }

    public static void main(String[] args) {
        List<String> IPList = Arrays.asList("100.71.193.122","100.71.193.120");
        new testhttp();
        int x=0;
        while(x<IPList.size()) {
            try {
                String response = testhttp.HTTPGET(IPList.get(x),"");
                System.out.println(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            x++;
        }

    }
}


