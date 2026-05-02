package sockets;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecieveTCPSocketThread2 extends Thread{
    
    long startTime, endTime, durTime , sum=0 , avgBandwidth;
    boolean keepListening = true; 
    private int sleeptime = 100;//500;
    private int sleeptimeRecievingFile = 10;//400;
    final int RECEIVING_PORT = 8888;
    private SharedQueueStr Face_Recieve_Q;
    String localIPstrServer;
    String localIPstrClient;
    
    private Socket TCPReceiverSocket = null;
    private PrintStream outStrm = null;
    private DataInputStream inStrm = null;


    public RecieveTCPSocketThread2(String localIPstrServer, String localIPstrClient , SharedQueueStr Face_Recieve_Q ) throws SocketException, UnknownHostException{ 
        this.Face_Recieve_Q = Face_Recieve_Q;	
        this.localIPstrServer = localIPstrServer;
        this.localIPstrClient = localIPstrClient;
        keepListening = true;
        
    }
	
    public void stopThread(){
        keepListening = false;
        try {            
            TCPReceiverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(RecieveTCPSocketThread2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
	
    @Override
    public void run() {      
        System.out.println("recieve Th run");
        long startTimeTot1 = System.currentTimeMillis();
        try {
            System.out.println("wait for server socket");
            TCPReceiverSocket = new Socket(this.localIPstrClient, RECEIVING_PORT);
            long startTimeTot2 = System.currentTimeMillis();
            System.out.println("connect successfully, " + (startTimeTot2-startTimeTot1));
            outStrm = new PrintStream(TCPReceiverSocket.getOutputStream());
            inStrm = new DataInputStream(TCPReceiverSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + this.localIPstrClient);
        }catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + this.localIPstrClient);
        }
        
        int c = 0;
        String s = "RecieveSocketThread on Server started."; //System.out.println(s);
        String RecievedfileName = " ";
        long startTimeTot = System.currentTimeMillis();
        long endTimeTot;
        boolean WrToFile = false;
        FileOutputStream foutStrWr = null;
        byte[] buffer = new byte[1000*1024];
        int count;
        try {
            while((count = inStrm.read(buffer)) >= 0){ //((inputLine = inStrm.readLine()) != null){//(keepListening){	
                String msgthatReceive = new String(buffer, 0, count);
                if(msgthatReceive != null){
                    
                    //1- first packet contains information about all allocated subtasks and their file name and pic number
                    if(msgthatReceive.indexOf("?!Simin")!= -1){
                        System.out.println("Allocated Files: " + msgthatReceive + ".");
                        Face_Recieve_Q.add(msgthatReceive);//(receivePacketfileName);
                            synchronized (Face_Recieve_Q) {
                                    Face_Recieve_Q.notify();
                            }                        
                    }               
                    
                    //4- end of file $
                    if(msgthatReceive.indexOf("$$@$$")!= -1){
                        System.out.println("end of file $ "+ RecievedfileName);
                        endTimeTot = System.currentTimeMillis();
                        System.out.println("transferT = " + (endTimeTot-startTimeTot));
                        //foutStrWr.write(buffer, 0, count);
                        foutStrWr.close();
                        WrToFile = false;
                        Face_Recieve_Q.add(RecievedfileName);//(receivePacketfileName);
                        synchronized (Face_Recieve_Q) {
                            Face_Recieve_Q.notify();
                        }
                    }
                    
                    //2- Start reciveing a file
                    if(msgthatReceive.indexOf("&@&")!= -1){
                        WrToFile = true;
                        c++;
                        RecievedfileName = c + "-file" + ".txt"; 
                        System.out.println("begin reciving " + RecievedfileName);
                        //System.out.println(msgthatReceive);
                        try {
                            foutStrWr = new FileOutputStream(RecievedfileName);//sdCard???
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(RecieveTCPSocketThread2.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        startTimeTot = System.currentTimeMillis();
                    }
                    //3- recive file and save it to local RecievedfileName
                    if(WrToFile == true){
                        foutStrWr.write(buffer, 0, count);
                    }
                    
//                     else{ System.out.print("misData " /*+ msgthatReceive*/); }
                }                 
                
//                try {  this.sleep(sleeptime);
//                } catch (InterruptedException e) { e.printStackTrace();}
            }//while
        } catch (IOException ex) {
            Logger.getLogger(RecieveTCPSocketThread2.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            outStrm.close();
            inStrm.close();
            TCPReceiverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(RecieveTCPSocketThread2.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("\nudpreceiver finished");                    
    }//run

}