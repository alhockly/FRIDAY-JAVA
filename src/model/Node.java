package model;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;



public class Node {

    String Fname;
    String Mac;
    String IP;
    List<String> Verbs;
    List<String> Vals;

    public boolean isOnline() {
        return isOnline;
    }

    boolean isOnline;


    public Node(String Fname, String Mac, String IP,String Verbs, String Vals) {



        this.Fname = Fname;
        this.Mac = Mac;
        this.IP = IP;
        this.Verbs = new ArrayList<String>();
        this.Vals = new ArrayList<String>();

        if (Verbs.contains(",")) {
            List<String> verbslist = Arrays.asList(Verbs.split(","));
            this.Verbs = verbslist;
        } else {
            this.Verbs.add(Verbs);
        }
        if (Vals.contains(",")) {
            List<String> valslist = Arrays.asList(Vals.split(","));
            this.Vals = valslist;
        } else {
            this.Vals.add(Vals);
        }


    }


    @Override
    public String toString(){

        String toreturn= "<Node>\n\t<FName>"+Fname+"</FName>\n\t<Mac>"+Mac+"</Mac>\n\t<IP>"+IP+"</IP>\n\t<Verbs>";

        for(String verb : Verbs){                               ///TODO commas need to be added appropriately
            toreturn+=verb;
        }
        toreturn+="</Verbs>\n\t<Vals>";

        for(String val:Vals){
            toreturn+=val;
        }
        toreturn+="</Vals>\n</Node>";
        return toreturn;
    }

    public void UpdateXMLIP(){
        System.out.println("adding new ip "+IP+" to XML");
        List<String> Nodesxml = new ArrayList<String>();
        Scanner scan = null;
        try {
            scan = new Scanner(new File("resource/Nodes.xml"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(scan.hasNextLine()){
            String line = scan.nextLine();
                Nodesxml.add(line);
        }
        int count=0;
        for(String line : Nodesxml){

            if(line.contains(Mac)){
                Nodesxml.set(count+1,"\t<IP>"+IP+"</IP>");
                //System.out.println(Nodesxml.toString());
                break;
            }
            count++;
        }

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


    }

    public boolean Checkonline() {
        if (IP.equals("")) {
            return false;
        }
        if (IP.contains("L")) {
            IP = IP.substring(1, IP.length());
        }

        int response = Ping(800);
        if (response == -1) {

            if (!this.IP.contains("L")) {
                this.IP = "L" + this.IP;     //give this IP an L, we dont wanna be too destructive so its added
            }

            UpdateXMLIP();

            this.isOnline = false;
            return false;

        }
        return true;

    }

    public int Ping(int timeout) {
        int responseCode = -1;
        if (this.IP.contains("L")) {
            this.IP = this.IP.substring(1, this.IP.length());
        }
        if (this.IP.equals("")) {
            System.out.println(getFname() + " Node IP is unknown");

            return responseCode;
        }


        try {
            String url = "http://" + this.IP;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            // optional default is GET
            con.setRequestMethod("GET");
            con.setConnectTimeout(timeout); //time in microseconds
            con.setReadTimeout(timeout);
            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            responseCode = con.getResponseCode();

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("err timout");

        } catch (java.io.IOException e) {

        }
        return responseCode;

    }

    public void Send(String val) {

        if (!IP.equals("") || !IP.equals("L")) {
            if(IP.contains("L")){
                IP=IP.substring(1,IP.length());
            }
            try {
                String response = HTTPGET(this.IP, val, 500);

            } catch (SocketTimeoutException e) {
                System.out.println("connection Timeout from Node.Send.timeout");
                System.out.println(Fname+" is offline");
                if(!getIP().contains("L")){
                    setIP("L"+getIP());
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("nodeObject failed to send to "+IP);;

            }

        }
    }

    public void StartDeepScanForMac() {
        int response = Ping(500);      //maybe use a variable for this
        if(response==-1){
            Thread t = new Thread(new DeepScan(100,71,193,0));             //TODO this shouldn't be hardcoded at all lol
            t.start();
            return;
        }
        System.out.println("lights is up u doofus (I Node.pinged :) )");

    }

    public String HTTPGET(String IP, String val, int timeout) throws Exception {

        String url = "http://" + IP + "/" + val;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");
        //con.setConnectTimeout(10);
        con.setConnectTimeout(timeout);
        //add request header
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = -1;
        try {
            responseCode = con.getResponseCode();
        } catch (ConnectException f) {
            System.out.println("http get timeout");
        }
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

    public String getFname() {
        return Fname;
    }

    public String getMac() {
        return Mac;
    }


    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public List<String> getVals() {
        return Vals;
    }

    public List<String> getVerbs() {
        return Verbs;
    }

    public class DeepScan implements Runnable {                 ///Constructor takes octets of an ip as 4 ints. oct 1 and 2 are fixed, oct 3 and 4 are "start-from" to 255

        private String Nodeipsfile;
        Node node = Node.this;

        //defaults lol
        int oct1 = 100;
        int oct2 = 71;
        int oct3 = 190;
        int oct4 = 0;


        public DeepScan(int oct1,int oct2,int oct3,int oct4) {
            this.oct1 = oct1;
            this.oct2 = oct2;
            this.oct3 = oct3;
            this.oct4 = oct4;
        }

        public void run() {
            System.out.println("Deep Scan Running, searching for " + node.Fname + " Node (" + node.Mac + ")");
            List<String> IPList = new ArrayList<String>();


            int x = oct3;
            while (x < 256) {
                int y = oct4;
                while (y < 256) {
                    IPList.add(Integer.toString(oct1) + "." + Integer.toString(oct2) + "." + Integer.toString(x) + "." + Integer.toString(y));
                    y++;
                }
                x++;
            }


            //System.out.println(String.join(",", IPList));
            int ipnum=0;
            int responseCode;
            for (String ip : IPList) {
                //System.out.println(ip);
                if(ipnum%255==0){
                System.out.println(node.Fname + "(" + node.Mac + ") Node deepscan:"+ipnum+"/"+IPList.size()+" "+ip);
                }
                int pingTimeout = 800;
                responseCode = Ping(ip, pingTimeout);
                if (responseCode != 200) {
                    //System.out.println("Deep Scan ("+responseCode+")");
                } else {
                    System.out.println(node.Fname + " (" + node.Mac + ")" + " deepscan: Found a webserver at http://" + ip);
                    addToFoundIps(ip);
                    try {
                        String response = HTTPGET(ip, "",pingTimeout);
                        //System.out.println(response);
                        if (response.contains(node.Mac)) {
                            System.out.println("Found " + node.Fname + "(" + node.Mac + ")" + " @ " + ip);
                            node.setIP(ip);
                            node.UpdateXMLIP();
                            return;
                        }
                    } catch (SocketTimeoutException e) {

                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                ipnum++;
            }
        }

        public int Ping(String ip, int timeout) {
            int responseCode = -1;

            try {
                String url = "http://" + ip;
                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                // optional default is GET
                con.setRequestMethod("GET");
                con.setConnectTimeout(timeout); //time in microseconds
                con.setReadTimeout(timeout);
                //add request header
                con.setRequestProperty("User-Agent", "Mozilla/5.0");

                responseCode = con.getResponseCode();

            } catch (java.net.SocketTimeoutException e) {
                //System.out.println("err timout");

            } catch (java.io.IOException e) {
                //System.out.println("io Exception node.deepscan.ping");
                //e.printStackTrace();
            }
            return responseCode;

        }


        private String HTTPGET(String IP, String path, int timeout) throws Exception {

            String url = "http://" + IP + path;

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");
            con.setConnectTimeout(timeout);

            //add request header
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = -1;
            try {
                responseCode = con.getResponseCode();
                if (responseCode == 404) {
                    return "";
                }

            } catch (SocketTimeoutException e) {
                System.out.println("connect Timeout @ "+IP);
                //e.printStackTrace();
                return Integer.toString(responseCode);
            }
                    //System.out.println("\nSending 'GET' request to URL : " + url);
                    //System.out.println("Response Code : " + responseCode);

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
    }

    public void addToFoundIps(String ip){

        List<String> foundips = new ArrayList<String>();
        Scanner scan = null;
        try {
            scan = new Scanner(new File("resource/Foundips.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while(scan.hasNextLine()){
            String line = scan.nextLine();
            foundips.add(line);
        }

        foundips.add(ip);

        PrintWriter out = null;
        try {
            out = new PrintWriter("resource/Foundips.txt");
            for(String ips: foundips) {
                out.println(ips);
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
