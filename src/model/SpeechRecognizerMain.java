package model;


import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Time;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Port;

import com.sun.net.httpserver.HttpServer;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.result.WordResult;
import tts.TextToSpeech;


public class SpeechRecognizerMain {


	TextToSpeech voice = new TextToSpeech();


	public List<Node> GetNodeObjectList(){
		return NodeObjectList;
	}

	public List<Node> NodeObjectList = new ArrayList<Node>();

	private boolean Listening;

	private float gain = 0.5f;

	//TextToSpeech VoiceObject = new TextToSpeech();

	Boolean isLightsOn;

	// Necessary
	private LiveSpeechRecognizer recognizer;
	
	// Logger
	private Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * This String contains the Result that is coming back from SpeechRecognizer
	 */
	private String speechRecognitionResult;
	
	//-----------------Lock Variables-----------------------------
	
	/**
	 * This variable is used to ignore the results of speech recognition cause actually it can't be stopped...
	 * 
	 * <br>
	 * Check this link for more information: <a href=
	 * "https://sourceforge.net/p/cmusphinx/discussion/sphinx4/thread/3875fc39/">https://sourceforge.net/p/cmusphinx/discussion/sphinx4/thread/3875fc39/</a>
	 */
	private boolean ignoreSpeechRecognitionResults = false;
	
	/**
	 * Checks if the speech recognise is already running
	 */
	private boolean speechRecognizerThreadRunning = false;
	
	/**
	 * Checks if the resources Thread is already running
	 */
	private boolean resourcesThreadRunning;
	
	//---
	
	/**
	 * This executor service is used in order the playerState events to be executed in an order
	 */
	private ExecutorService eventsExecutorService = Executors.newFixedThreadPool(2);


	//------------------------------------------------------------------------------------



	/**
	 * Constructor
	 * @param nodeObjectList
	 */
	public SpeechRecognizerMain(List<Node> nodeObjectList) {

		Listening=false;
		voice.setVoice("cmu-slt-hsmm");
		this.NodeObjectList=nodeObjectList;


		Class c = this.getClass();          // if you want to use the current class

		System.out.println("Package: "+c.getPackage()+"\nClass: "+c.getSimpleName()+"\nFull Identifier: "+c.getName());
		// Loading Message
		logger.log(Level.INFO, "Loading Speech Recognizer...\n");

		// Configuration
		Configuration configuration = new Configuration();


		configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");

		configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		// Load model from the jar
		//configuration.setAcousticModelPath("resource:libraries/sphinx4-data-5prealpha-20160628.232535-10.jar!/edu/cmu/sphinx/models/en-us/en-us/");



		//====================================================================================
		//=====================READ THIS!!!===============================================
		//Uncomment this line of code if you want the recognizer to recognize every word of the language
		//you are using , here it is English for example
		//====================================================================================
		//configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

		//====================================================================================
		//=====================READ THIS!!!===============================================
		//If you don't want to use a grammar file comment the 3 lines below and uncomment the above line for language model
		//====================================================================================

		// Grammar
		configuration.setGrammarPath("resource/grammars");
		configuration.setGrammarName("grammar");
		configuration.setUseGrammar(true);

		try {
			recognizer = new LiveSpeechRecognizer(configuration);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}

		// Start recognition process pruning previously cached data.
		// recognizer.startRecognition(true);

		//Check if needed resources are available
		startResourcesThread();
		//Start speech recognition thread
		startSpeechRecognition();
	}
	
	//-----------------------------------------------------------------------------------------------
	
	/**
	 * Starts the Speech Recognition Thread
	 */
	public synchronized void startSpeechRecognition() {
		
		//Check lock
		if (speechRecognizerThreadRunning)
			logger.log(Level.INFO, "Speech Recognition Thread already running...\n");
		else
			//Submit to ExecutorService
			eventsExecutorService.submit(() -> {
				
				//locks
				speechRecognizerThreadRunning = true;
				ignoreSpeechRecognitionResults = false;
				
				//Start Recognition
				recognizer.startRecognition(true);
				
				//Information			
				logger.log(Level.INFO, "You can start to speak...\n");
				
				try {
					while (speechRecognizerThreadRunning) {
						/*
						 * This method will return when the end of speech is reached. Note that the end pointer will determine the end of speech.
						 */
						SpeechResult speechResult = recognizer.getResult();
						
						//Check if we ignore the speech recognition results
						if (!ignoreSpeechRecognitionResults) {
							
							//Check the result
							if (speechResult == null)
								logger.log(Level.INFO, "I can't understand what you said.\n");
							else {
								
								//Get the hypothesis
								speechRecognitionResult = speechResult.getHypothesis();
								
								//You said?
								System.out.println("You said: [" + speechRecognitionResult + "]\n");
								
								//Call the appropriate method 
								makeDecision(speechRecognitionResult, speechResult.getWords());
								
							}
						} else
							logger.log(Level.INFO, "Ingoring Speech Recognition Results...");
						
					}
				} catch (Exception ex) {
					logger.log(Level.WARNING, null, ex);
					speechRecognizerThreadRunning = false;
				}
				
				logger.log(Level.INFO, "SpeechThread has exited...");
				
			});
	}
	
	/**
	 * Stops ignoring the results of SpeechRecognition
	 */
	public synchronized void stopIgnoreSpeechRecognitionResults() {
		
		//Stop ignoring speech recognition results
		ignoreSpeechRecognitionResults = false;
	}
	
	/**
	 * Ignores the results of SpeechRecognition
	 */
	public synchronized void ignoreSpeechRecognitionResults() {
		
		//Instead of stopping the speech recognition we are ignoring it's results
		ignoreSpeechRecognitionResults = true;
		
	}
	
	//-----------------------------------------------------------------------------------------------
	
	/**
	 * Starting a Thread that checks if the resources needed to the SpeechRecognition library are available
	 */
	public void startResourcesThread() {
		
		//Check lock
		if (resourcesThreadRunning)
			logger.log(Level.INFO, "Resources Thread already running...\n");
		else
			//Submit to ExecutorService
			eventsExecutorService.submit(() -> {
				try {
					
					//Lock
					resourcesThreadRunning = true;
					
					// Detect if the microphone is available
					while (true) {
						
						//Is the Microphone Available
						if (!AudioSystem.isLineSupported(Port.Info.MICROPHONE))
							logger.log(Level.INFO, "Microphone is not available.\n");
						
						// Sleep some period
						Thread.sleep(350);
					}
					
				} catch (InterruptedException ex) {
					logger.log(Level.WARNING, null, ex);
					resourcesThreadRunning = false;
				}
			});
	}
	
	/**
	 * Takes a decision based on the given result
	 * 
	 * @param speechWords
	 */
	public void makeDecision(String speech , List<WordResult> speechWords) {
		//System.out.println(speech);
		//System.out.println(speechWords.toString());
		if(speech.contains("<unk>")){
			Listening=false;
		}
		if(speech.contains("friday")){
			Listening=true;
		}
		//System.out.println("Listening? "+Listening);

		if(Listening==true) {

			//if speech contains fname and val in either order, send val to the node with Fname
			for(Node node: NodeObjectList){
				String value=null;
				//System.out.println(node.getFname()+node.getMac()+node.getIP());
				if(speech.contains(node.getFname())){
					for(String verb : node.getVerbs()){
						if((speech.contains(verb)&& !verb.equals(""))){
							for(String val : node.getVals()){
								if(speech.contains(val)){
									value=val;
								}
							}
							if(value.equals("")){
								System.out.println("verb command");
								System.out.println("found match! send >" + verb + "< to " + node.getFname() + " @ " + node.getIP());
								if (node.getIP().equals("") || node.getIP().equals("L")) {
									System.out.println("unable to send, IP of node unknown");
								} else {
									node.Send(verb);
									return;
								}
							}
							else {
								System.out.println("found match! send >" + value + "< to " + node.getFname() + " @ " + node.getIP());
								if (node.getIP().equals("") || node.getIP().equals("L")) {
									System.out.println("unable to send, IP of node unknown");
								} else {
									node.Send(value);
									return;
								}
							}
						}
					}
				}
			}


			if(speech.contains("deep scan") && speech.contains("for")) {
				for (Node node : NodeObjectList) {
					if(speech.contains(node.getFname())) {
						node.StartDeepScanForMac();
					}
				}
			}



			if (speech.equals("friday how many voice commands do you have")){

				int nodeCommands=0;
					int nodecount = NodeObjectList.size();
					int valscount =0;
					for( Node node:NodeObjectList){
						for(String val : node.getVals()){
							valscount++;
						}
					}
				nodeCommands=nodecount*valscount;
				System.out.println(nodeCommands+" commands"+" 2 system commands");
				//voice.speak(nodeCommands+" node commands and 2 system commands",gain, false, true);
			}
		}
	}




	public static List<Node> ReadXML(){			///return list of node objects
		List<Node> NodeObjectList = new ArrayList<Node>();
		int numNodes=0;
		try {
			Scanner scan = new Scanner(new File("src/Nodes.xml"));
			List<String> Nodes = new ArrayList<String>();

			Nodes.add("");
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(line.equals("<Node>")){
					numNodes++;
					Nodes.add("");
				}
				Nodes.set(Nodes.size()-1, Nodes.get(Nodes.size()-1)+line );

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
	
	public boolean getIgnoreSpeechRecognitionResults() {
		return ignoreSpeechRecognitionResults;
	}
	
	public boolean getSpeechRecognizerThreadRunning() {
		return speechRecognizerThreadRunning;
	}

	private static void checkNodesOnline(List<Node> NodeObjectList){
		int found=0;
		for (Node node : NodeObjectList){
			if(node.Checkonline()){
				System.out.println(node.getFname()+" is up @ "+node.getIP());
				if(node.getIP().contains("L")){										//Remove L from IP if its online
					node.setIP(node.getIP().substring(1,node.getIP().length()));
				}
				found++;
			}
			else{
				//System.out.println(node.getFname()+" is down :(");

				node.StartDeepScanForMac();
			}
		}
		System.out.println("Found "+found+"/"+NodeObjectList.size()+" nodes");
		try {
			TimeUnit.MICROSECONDS.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private static void RebuildGrammar(List<Node> NodeObjectList){
		List<String> gramToAdd = new ArrayList<String>();
		List<String> Appliances = new ArrayList<String>();
		List<String> oldGram = new ArrayList<String>();

		Scanner scan = null;
		try {
			scan = new Scanner(new File("resource/grammars/grammar.old"));
			while(scan.hasNextLine()) {

				oldGram.add(scan.nextLine());

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		for (Node node : NodeObjectList){
			Appliances.add(node.getFname());

			String syntax = "public <"+node.getFname()+"syntax> = <StartListen> <"+node.getFname()+"verbs> <appliance>";

			if(!node.getVals().get(0).equals("")){
				String vals ="<"+node.getFname()+"vals> = ("+String.join(" | ",node.getVals())+");";
				gramToAdd.add(vals);

				syntax=syntax+"<"+node.getFname()+"vals>;";
			}
			else {
				syntax = syntax + ";";
			}
			String verbs = "<"+node.getFname()+"verbs> = ("+String.join(" | ",node.getVerbs())+");";



			gramToAdd.add(verbs);

			gramToAdd.add(syntax);


		}
		String appliance = "<appliance> = ("+String.join(" | ",Appliances)+");";
		gramToAdd.add(appliance);


		PrintWriter out = null;
		try {
			out = new PrintWriter("resource/grammars/grammar.gram");

			for(String line : oldGram){
				out.println(line);
			}
			for(String line : gramToAdd){
				out.println(line);
			}

			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}








	}
	public static void StartHttpServer() throws IOException {
		InetSocketAddress addr = new InetSocketAddress(80);
		HttpServer server = HttpServer.create(addr, 0);

		server.createContext("/", new MyHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("Server is listening on port 80" );
	}









	/**
	 * Main Method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		List<Node> NodeObjectList = ReadXML();
		System.out.println("Pinging Nodes");
		checkNodesOnline(NodeObjectList);
		System.out.println("rebuilding grammer");
		RebuildGrammar(NodeObjectList);
		try {
			StartHttpServer();
		} catch (IOException e) {
			e.printStackTrace();
		}

		new SpeechRecognizerMain(NodeObjectList);
	}
}




