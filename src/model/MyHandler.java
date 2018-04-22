package model;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MyHandler implements HttpHandler {
    public void handle(HttpExchange exchange) throws IOException {
        String requestMethod = exchange.getRequestMethod();
        System.out.println("Port 80:    " + requestMethod + " " + exchange.getRequestURI().toString());


        OutputStream responseBody = exchange.getResponseBody();
        if (requestMethod.equalsIgnoreCase("GET")) {


            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, 0);

            responseBody = exchange.getResponseBody();
            Headers requestHeaders = exchange.getRequestHeaders();
            Set<String> keySet = requestHeaders.keySet();
            Iterator<String> iter = keySet.iterator();

            String exchangeuri = exchange.getRequestURI().getPath();
            String Query = exchange.getRequestURI().getQuery();
            if (exchangeuri.equals(new String("/favicon.ico"))) {
                Path path = Paths.get("resource/web-facing/favicon.ico");
                byte[] data = Files.readAllBytes(path);
                int x = 0;
                while (x < data.length) {
                    responseBody.write(data[x]);
                    x++;
                }
                responseBody.close();
            }
            if(exchangeuri.equals(new String("/"))){

                String page = GetPageTemplate();
                page += "@" + InetAddress.getLocalHost().toString() + "</h1>";
                page += "Nodes:<br>";

                for (Node node : SpeechRecognizerMain.ReadXML()) {
                    String name = node.getFname();
                    page += "<p>" + name + "@";
                    if (node.Checkonline()) {
                        page += "<a href='http://" + node.getIP() + "'>" + node.getIP() + "</a></p>";
                    } else {
                        page += "Offline :(</p>";
                    }
                }

                page += "<p>Add a node <form>  Name <input name='name'</input> Mac <input name='mac'</input> Verbs <input name='verbs'</input> Vals <input name='vals'</input><button type='submit'>Sendo</button></form></p>";

                page += "<br>Running grammer:<br><div style='background-color:#fbe4d2;'><p class='code'>";
                for (String line : GetGrammer()) {
                    line = line.replace("<", "&lt;");
                    line = line.replace(">", "&gt;");
                    page = page + line + "<br>";

                }
                page += "</p></div>";
                page += "<a href='/restart'>Restart</a>";
                responseBody.write(page.getBytes());
                responseBody.close();

                if (Query.contains(new String("name=")) && Query.contains(new String("vals=")) && Query.contains(new String("verbs=")) && Query.contains(new String("mac="))) {
                    String[] nodeparts = Query.split("&");
                    String name=nodeparts[0].replace("name=","");
                    String mac=nodeparts[1].replace("mac=","");
                    String verbs=nodeparts[2].replace("verbs=","");
                    String vals=nodeparts[3].replace("vals=","");
                    System.out.println("Port 80: New node("+mac+") "+name+": with vals "+vals+" and verbs "+verbs);

                    AddNodeTOXML(new Node(name,mac,"",verbs,vals));
                }


            }
        }
    }


    public void AddNodeTOXML(Node node) {

        List<String> Nodesxml=ReadNodeXML();

        Nodesxml.add(node.toString());

        Nodesxml.add("</XML>");
        PrintWriter out = null;
        try {
            out = new PrintWriter("resource/Nodes.xml");
            for(String line : Nodesxml){
                out.println(line);
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //Refresh handler
    }


    public List<String> ReadNodeXML(){
        List<String> Nodesxml = new ArrayList<String>();
        Scanner scan = null;
        try {
            scan = new Scanner(new File("resource/Nodes.xml"));
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                if(!line.equals("</XML>")) {
                    Nodesxml.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Nodesxml;
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