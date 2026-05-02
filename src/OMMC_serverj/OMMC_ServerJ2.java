
package OMMC_serverj;

import faceDetectionApp.NotUsed_RunFaceDetcTCPThread;
import java.net.SocketException;
import java.net.UnknownHostException;

import sockets.NotUsed_RecieveUDPSocketThread;
//import sockets.SendSocketThread;
import faceDetectionApp.RunFaceDetcUDPThread;
import faceDetectionApp.NotUsed_RunFaceDetcTCPThreadSeri;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import sockets.*;

/**
 *
 * @author simin
 */
public class OMMC_ServerJ2 {
    
    public static void main(String[] args) {
                
        SharedQueueStr Face_Recieve_Q;
        SharedQueueStr Face_Send_Q;
        String localIPstrServer;
        String localIPstrClient;
        //initialize
        Face_Recieve_Q = new SharedQueueStr();
        Face_Send_Q = new SharedQueueStr();
        Scanner in = new Scanner(System.in);
        System.out.println("Enter localIPstrServer:");
        localIPstrServer = "192.168.43." + in.next();
        System.out.println("Enter localIPstrClient:");
        localIPstrClient = "192.168.43." + in.next();
	
        boolean srialFileTransfer = false;//false;
        if(srialFileTransfer == true){
            //recieveSocketThread
            NotUsed_RecieveUDPSocketThread recieveSocketThread = null;
            try {
                recieveSocketThread = new NotUsed_RecieveUDPSocketThread(localIPstrServer, localIPstrClient, Face_Recieve_Q);
            } catch (SocketException ex) {
                Logger.getLogger(OMMC_ServerJ2.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownHostException ex) {
                Logger.getLogger(OMMC_ServerJ2.class.getName()).log(Level.SEVERE, null, ex);
            }
            recieveSocketThread.start();
            //FaceDetcThread
            RunFaceDetcUDPThread runFaceDetcThread = new RunFaceDetcUDPThread(Face_Recieve_Q, Face_Send_Q,localIPstrServer, localIPstrClient);
            runFaceDetcThread.start();
            System.out.println("RunFaceDetcUDPThread started! " + srialFileTransfer);
        
        }else{
            //recieveSocketThread
            RecieveTCPSocketThread2 recieveSocketThread = null;
            try {
                recieveSocketThread = new RecieveTCPSocketThread2(localIPstrServer, localIPstrClient, Face_Recieve_Q);
            } catch (SocketException e) {
                    e.printStackTrace();
            } catch (UnknownHostException e) {
                    e.printStackTrace();
            }
            recieveSocketThread.start();
            System.out.println("recieveSocketThread started!");        
            //FaceDetcThread
            /*
            NotUsed_RunFaceDetcTCPThread runFaceDetcThread = new NotUsed_RunFaceDetcTCPThread(Face_Recieve_Q, Face_Send_Q,localIPstrServer, localIPstrClient);
            runFaceDetcThread.start();
            */
            RunFaceDetcUDPThread runFaceDetcThread = new RunFaceDetcUDPThread(Face_Recieve_Q, Face_Send_Q,localIPstrServer, localIPstrClient);
            runFaceDetcThread.start();
            
            System.out.println("RunFaceDetcTCPThread started! " + srialFileTransfer);
        }
        
        
        //        
//        //sendSocketThread
//        SendSocketThread sendSocketThread;
//        sendSocketThread = new SendSocketThread(localIPstrServer, localIPstrClient, Face_Send_Q);
//	sendSocketThread.start();
//        System.out.println("sendSocketThread started!");
//        System.out.println();
        
    }
}
