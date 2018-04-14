package model;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class MyHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            OutputStream responseBody = exchange.getResponseBody();
            Headers requestHeaders = exchange.getRequestHeaders();
            Set<String> keySet = requestHeaders.keySet();
            Iterator<String> iter = keySet.iterator();



            String page = GetPageTemplate()+"<br>Grammer<br>";


            for(String line : GetGrammer()){
                line = line.replace("<","&lt;");
                line = line.replace(">","&gt;");
                page=page+line+"<br>";

            }

            responseBody.write(page.getBytes());

            responseBody.close();
        }
    }

    public List<String> GetGrammer(){
        List<String> gram = new ArrayList<>();
        Scanner scan = null;
        try {
            scan = new Scanner(new File("resource/grammars/grammar.gram"));
            while(scan.hasNextLine()) {

                gram.add(scan.nextLine());

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return  gram;
    }
    public String GetPageTemplate(){
        String template="";
        Scanner scan = null;
        try {
            scan = new Scanner(new File("resource/web-facing/pageTemplate.html"));
            while(scan.hasNextLine()) {
                template=template+scan.nextLine();


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return  template;
    }

}