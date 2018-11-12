
package googleAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;


import com.darkprograms.speech.microphone.Microphone;
import com.darkprograms.speech.recognizer.GSpeechDuplex;
import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import com.darkprograms.speech.recognizer.GoogleResponse;

import net.sourceforge.javaflacencoder.FLACFileWriter;

import model.Node;


public class GoogleAPIMain {

    //private final TextToSpeech tts = new TextToSpeech();
    private final Microphone mic = new Microphone(FLACFileWriter.FLAC);
    private final GSpeechDuplex duplex = new GSpeechDuplex("AIzaSyBOti4mM-6x9WDnZIjIeyEU21OpBXqWBgw");
    String oldText = "";
    public List<Node> NodeObjectList = new ArrayList<Node>();
    Timeout timeout;


    private boolean threadstarted;


    public static void main(String[] args) {
        new GoogleAPIMain();
    }

    /**
     * Constructor
     */
    public GoogleAPIMain() {
        NodeObjectList=ReadXML();
        timeout=new Timeout();

        Thread timeouthread=new Thread(timeout);
        timeouthread.start();

        //Duplex Configuration
        duplex.setLanguage("en");

        duplex.addResponseListener(new GSpeechResponseListener() {

            public void onResponse(GoogleResponse googleResponse) {
                String output = "";

                //Get the response from Google Cloud
                output = googleResponse.getResponse();
                System.out.println(output);
                if (output != null) {
                    makeDecision(output);
                } else
                    System.out.println("Output was null");
            }
        });


        startSpeechRecognition();

    }

    /**
     * This method makes a decision based on the given text of the Speech Recognition
     *
     * @param
     */
    public void makeDecision(String output) {

        output = output.trim();
        //System.out.println(output.trim());

        //We don't want duplicate responses
        if (!oldText.equals(output)){
            oldText = output;}
        else{
            return;}
        output=output.toLowerCase();

        if(output.toLowerCase().equals("cancel")){
            System.exit(0);
        }

        if(output.toLowerCase().contains("off") && output.toLowerCase().contains("lights")){
            for(Node n:NodeObjectList){
                if(n.getFname().equals("lights")){
                    n.Send("off");
                    System.exit(0);

                }
            }
        }
        if(output.toLowerCase().contains("on") && output.toLowerCase().contains("lights")){
            for(Node n:NodeObjectList){
                if(n.getFname().equals("lights")){
                    n.Send("on");
                    System.exit(0);
                }
            }
        }



        else{
            //System.out.println("Not entered on any else if statement");
        }

    }








    /**
     * Calls the MaryTTS to say the given text
     *
     * @param text

    public void speak(String text) {
    System.out.println(text);
    //Check if it is already speaking
    if (!tts.isSpeaking())
    new Thread(() -> tts.speak(text, 2.0f, true, false)).start();

    }
     */

    /**
     * Starts the Speech Recognition
     */
    public void startSpeechRecognition() {
        //Start a new Thread so our application don't lags
        new Thread(() -> {
            try {
                duplex.recognize(mic.getTargetDataLine(), mic.getAudioFormat());
            } catch (LineUnavailableException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Stops the Speech Recognition
     */
    public void stopSpeechRecognition() {
        mic.close();
        System.out.println("Stopping Speech Recognition...." + " , Microphone State is:" + mic.getState());
    }

    public static List<Node> ReadXML(){			///return list of node objects
        List<Node> NodeObjectList = new ArrayList<Node>();
        int numNodes=0;
        try {
            Scanner scan = new Scanner(new File("resource/Nodes.xml"));
            List<String> Nodes = new ArrayList<String>();

            Nodes.add("");
            while(scan.hasNextLine()){
                String line = scan.nextLine();
                if(line.equals("<Node>")){
                    numNodes++;
                    Nodes.add("");
                }
                Nodes.set(Nodes.size()-1, Nodes.get(Nodes.size()-1)+line );		//add line to end of Nodes

            }
            for(String node : Nodes) {

                if (node.contains("<Node>")) {
                    //System.out.println(node);
                    String Fname = node.substring(node.indexOf("<FName>") + 7, node.indexOf("</FName>"));
                    String Mac = node.substring(node.indexOf("<Mac>") + 5, node.indexOf("</Mac>"));
                    String IP = node.substring(node.indexOf("<IP>") + 4, node.indexOf("</IP>"));
                    String verbs = node.substring(node.indexOf("<Verbs>") + 7, node.indexOf("</Verbs>"));
                    String vals = node.substring(node.indexOf("<Vals>") + 6, node.indexOf("</Vals>"));

                    NodeObjectList.add(new Node(Fname.toLowerCase(),Mac,IP,verbs.toLowerCase(),vals.toLowerCase()));
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
        //System.out.println("Got "+numNodes+" Nodes from xml");
        return NodeObjectList;

    }


}
