package faceDetectionApp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jjil.algorithm.Gray8Rgb;
import jjil.algorithm.RgbAvgGray;
import jjil.core.Image;
import jjil.core.Rect;
import jjil.core.RgbImage;
import jjil.j2se.RgbImageJ2se;

import sockets.SharedQueueStr;

public class RunFaceDetcUDPThread extends Thread {
	int minScale = 1,  maxScale = 40;
        long endTime, startTime;
        long endTimeTot, startTimeTot;
        long durTime;
	int n;
	String inputFileName;
	File myFile ;
	FileOutputStream fOut;
	BufferedWriter bufWriter_out;
	
	boolean keepListening = true; 
	private int sleeptime = 10;//500;
	String localIPstrServer;
	String localIPstrClient;
	private SharedQueueStr Face_Recieve_Q;
        private SharedQueueStr Face_Send_Q;
        int NumAllocatedTasks=0;
	int tskPic[] ;
        
        private int RECEIVING_PORT_ClientRec = 8888+10;
        DatagramSocket udpsender ;

        
    public RunFaceDetcUDPThread(SharedQueueStr Face_Recieve_Q, SharedQueueStr Face_Send_Q, String localIPstrServer, String localIPstrClient)
	 {
        this.n = 1; //n;1
        this.Face_Recieve_Q = Face_Recieve_Q;
        this.Face_Send_Q = Face_Send_Q;
        this.localIPstrClient = localIPstrClient;
        this.localIPstrServer = localIPstrServer;
        keepListening = true;        
        }
		
	public void stopThread(){
			this.stop();
		}
		
    public void run() {
    	long sumExeFace=0;
        int nextPicInd=-1;
    	while(keepListening){	            
            String poll = null;
            synchronized (Face_Recieve_Q) {
                while (Face_Recieve_Q.isEmpty()) {
                    try {
                        Face_Recieve_Q.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                }
                while(!Face_Recieve_Q.isEmpty()){
                    poll = Face_Recieve_Q.remove();//  receivemsgsQ locking and synchroniz
                    String receiveedfileName = poll;//.getData().toString();
                    inputFileName = receiveedfileName;
                    
                    if(poll.indexOf(",") >= 0){// , or ? OMG
                        //start first packet contains information about all allocated subtasks and their file name and pic number
                        startTimeTot = System.currentTimeMillis();
                        String arrs[] = poll.split(",");
                        NumAllocatedTasks = arrs.length-1;
                        tskPic = new int[arrs.length-1];
                        for(int i=0; i<arrs.length-1;i++){
                            tskPic[i] = Integer.parseInt(arrs[i]);
                            System.out.println("tskPic[" + i + "] ="+tskPic[i]);
                        }                    
                    }else{// every other packet only showes that file recived at the server completely.
                        nextPicInd++;
                        int picNum = -1;
                        if(tskPic != null){
                            picNum = tskPic[nextPicInd];
                            System.out.println("load PicNum " + picNum);
                        }else{
                            System.out.println("tskPic = null, load PicNum " + picNum);
                        }
                        if(nextPicInd+1 == NumAllocatedTasks){
                            System.out.print("Hey :) All tasks finished!!!!!!!!!!!!");
                            keepListening = false;
                            break;
                        }
                    }                              
                }//while !Face_Recieve_Q.isEmpty
            }//synchronized Face_Recieve_Q
            try {//System.out.println("RunFace sleeps.");//("in faceDetect in OMC server: sleeps in run face thread after synchronized " );	        
                this.sleep(sleeptime); //System.out.println("RunFace wake up.");
            } catch (InterruptedException e) {   //e.printStackTrace();
            }
        }//while(keepListening)
        //when all files are recived completly at the server, the face detection starts. it deteects each file iteratively.
                
        for(int i = 0; i < NumAllocatedTasks; i++){
            int picNum = tskPic[i];
            System.out.println("load PicNum " + picNum);
            // load the photo
            //InputStream inputstream = RunFaceDetcThread.class.getResourceAsStream("JoannaAndJimmy.jpg");
            BufferedImage bufferimg = null;
            String fileName = " ";
            try {
                if(picNum == -1){
                    bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("joannaandjimmy900.jpg"));
                    fileName = "joannaandjimmy900";
                    System.out.println("picNum = -1");
                }
                if(picNum == 0){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("twogirls2540kbyte.jpg"));
                        fileName = "twogirls2540kbyte";
                }
                if(picNum == 1){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("detektor_award_1mb.jpg"));
                        fileName = "detektor_award_1mb";
                }
                if(picNum == 2){//(inputFileName.compareToIgnoreCase("joannaandjimmy900") == 0){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("joannaandjimmy900.jpg"));
                        fileName = "joannaandjimmy900";
                }                        
                if(picNum == 3){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("oceanseleven380.jpg"));
                        fileName = "oceanseleven380";
                }                        
                if(picNum == 4){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("elinorpicsmall142.jpg"));
                        fileName = "elinorpicsmall142";
                }
                if(picNum == 5){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("elinorpicsmall142.jpg"));
                        fileName = "elinorpicsmall142";
                }
                if(picNum == 6){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("detektor_award_1mb.jpg"));
                        fileName = "detektor_award_1mb";
                }
                if(picNum == 7){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("oceanseleven380.jpg"));
                        fileName = "oceanseleven380";
                } 
                if(picNum == 8){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("joannaandjimmy900.jpg"));
                        fileName = "joannaandjimmy900";
                } 
                if(picNum == 9){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("joannaandjimmy900.jpg"));
                        fileName = "joannaandjimmy900";
                } 
                if(picNum == 10){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("detektor_award_1mb.jpg"));
                        fileName = "detektor_award_1mb";
                }
                if(picNum == 12){//(inputFileName.compareToIgnoreCase("joannaandjimmy900") == 0){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("joannaandjimmy900.jpg"));
                        fileName = "joannaandjimmy900";
                }                        
                if(picNum == 13){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("oceanseleven380.jpg"));
                        fileName = "oceanseleven380";
                }                        
                if(picNum == 14){
                        bufferimg = ImageIO.read(RunFaceDetcUDPThread.class.getResourceAsStream("elinorpicsmall142.jpg"));
                        fileName = "elinorpicsmall142";
                }
                String s = fileName;
                System.out.println(s +" run and added to Face_Send_Q");  
                //run face detection on given image file
                long durationTime = findFaces(this.n, bufferimg, 1, 40);
                sumExeFace += durationTime;


            } catch (IOException ex) {
                Logger.getLogger(RunFaceDetcUDPThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            //notify senderThread to send the results to client
//                    byte[] fileNameBuf = new byte[1024];
//                    System.arraycopy(inputFileName.getBytes(), 0, fileNameBuf, 0, inputFileName.length());
//                    InetAddress destIP = null;
//                    try { 
//                        destIP = InetAddress.getByName(this.localIPstrClient);//"192.168.43.1");///////////////////////client IP
//                    } catch (UnknownHostException e) {e.printStackTrace();	}
//                    DatagramPacket packetfileName = new DatagramPacket(fileNameBuf,fileNameBuf.length ,destIP , 8888 );

//                    Face_Send_Q.add(fileName);
//                    synchronized (Face_Send_Q) {
//                        Face_Send_Q.notify();
//                    }

//                    String s = fileName;
//                    System.out.println(s+" run and added to Face_Send_Q");               
        }//for pic
        System.out.println("Running Faces Finished");
        endTimeTot = System.currentTimeMillis();
        long durT = endTimeTot-startTimeTot;
        System.out.println("Total Time on "+localIPstrServer+" is " + durT);
        System.out.println("Total exe time on "+localIPstrServer+" is " + sumExeFace);
                
    	InetAddress localAddr = null , destIP = null ;
	try {
		localAddr = InetAddress.getByName( localIPstrServer);//192.168.43.83 , emu=127.0.0.0  y=43.1
		destIP = InetAddress.getByName(localIPstrClient); // 192.168.43.1 or 89		
	} catch (UnknownHostException e1) {
		e1.printStackTrace();
	}
	try {
		udpsender = new DatagramSocket(RECEIVING_PORT_ClientRec, localAddr);
	} catch (SocketException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
        
        String s = NumAllocatedTasks + "," + durT+ "," + localIPstrServer;        
        byte[] fileNameBuf = new byte[1024];
        //System.arraycopy(s.getBytes(), 0, fileNameBuf, 0, s.length());
        fileNameBuf= s.getBytes();

        DatagramPacket packetfileName = new DatagramPacket(fileNameBuf,fileNameBuf.length ,destIP , RECEIVING_PORT_ClientRec );
        try {
                    udpsender.send(packetfileName);
            } catch (IOException e1) {
                    e1.printStackTrace();
            }

        
    }//run
    
    public long findFaces(int n, BufferedImage bii, int minScale, int maxScale) {
        int faceCount = 0;
        Formatter fTime = null;
        try {
            fTime = new Formatter (new File ("OMCServer_FaceDetectionTVAIO.txt"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RunFaceDetcUDPThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            BufferedImage bi = bii;
            //fTime.format( "n  runtime  \n");
            List<Rect> results = null;
            startTime = System.currentTimeMillis();
            for(int c=0; c<n; c++){
                InputStream is  = RunFaceDetcUDPThread.class.getResourceAsStream("HCSB.txt");
                Gray8DetectHaarMultiScale detectHaar = new Gray8DetectHaarMultiScale(is, minScale, maxScale);
                RgbImage im = RgbImageJ2se.toRgbImage(bi);
                RgbAvgGray toGray = new RgbAvgGray();
                toGray.push(im);
                results = detectHaar.pushAndReturn(toGray.getFront());
                //System.out.println("Found "+results.size()+" faces");
                Image i = detectHaar.getFront();
                Gray8Rgb g2rgb = new Gray8Rgb();
                g2rgb.push(i);
                RgbImageJ2se conv = new RgbImageJ2se();
                //File output = new File("c:/Temp/res_JoannaAndJimmy.jpg");//(n%2==0) ? output1:output2;
                //conv.toFile((RgbImage)g2rgb.getFront(), output.getCanonicalPath());
            }
            faceCount = results.size();
            endTime = System.currentTimeMillis();
            //File imageFile = new File("images/template.jpg");

            } catch (Throwable e) {
                throw new IllegalStateException(e);
            } 
            fTime.format( n + "  " + (endTime - startTime) +"\n");
            System.out.println(n + " FaceDetec took " + (endTime - startTime) + " ms and find " + faceCount + "faces.");
           
            fTime.close();
            return endTime - startTime;
        } 
    
    
}