package model;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

public class MyHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        System.out.println("Port 80:    "+requestMethod+" "+exchange.getRequestURI());
        if (requestMethod.equalsIgnoreCase("GET")) {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            OutputStream responseBody = exchange.getResponseBody();
            Headers requestHeaders = exchange.getRequestHeaders();
            Set<String> keySet = requestHeaders.keySet();
            Iterator<String> iter = keySet.iterator();



            String page = GetPageTemplate();

            page+="@"+InetAddress.getLocalHost().toString()+"</h1>";

            page+="Nodes:<br>";

            List<Node> Nodes =SpeechRecognizerMain.ReadXML();
            for(Node node: Nodes){
                String name= node.getFname();
                page+="<p>"+name+"@";
                if(node.Checkonline()){
                    page+="<a href='http://"+node.getIP()+"'>"+node.getIP()+"</a></p>";
                }
                else{
                    page+="Offline :(</p>";
                }


            }

            page+="<br>Running grammer:<br><div style='background-color:#fbe4d2;'><p class='code'>";


            for(String line : GetGrammer()){
                line = line.replace("<","&lt;");
                line = line.replace(">","&gt;");
                page=page+line+"<br>";

            }
            page+="</p></div>";



            page+="<a href='/restart'>Restart</a>";

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


    public String GetSelfIP(){
        String ip="";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // *EDIT*
                    if (addr instanceof Inet4Address) continue;

                    ip = addr.getHostAddress();
                    System.out.println(iface.getDisplayName() + " " + ip);
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
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