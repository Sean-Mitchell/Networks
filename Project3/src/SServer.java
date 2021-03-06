/* Server program for the Scribble app

   @author Jonathan Ly, Sean Mitchell

   @version CS 391 - Spring 2018 - A3
*/

import java.net.*;
import java.io.*;

public class SServer {

    static ServerSocket serverSocket = null;   // listening socket
    static int portNumber = 55555;             // port on which server listens
    static int seed;                           // seed for controlling the
                                               // randomness of the game

    /* Start the server, then repeatedly wait for and accept two connection
       requests and start a new Scribble thread to handle one game between
       the two corresponding players. Before starting the thread, this method
       sends a <welcome + wait prompt> message to both players. Each successive
       thread is passed a seed value, starting with the seed 0 for the first
       thread, the seed 1 for the second thread, etc.
       The output sent to the console by this method is described in the handout.
    */
    public static void main(String[] args) {
    	
    	seed = 0;
    	
    	try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server Started: " + serverSocket);
			while (true) {
				System.out.println("Waiting for a client...");
				
				Socket client1 = serverSocket.accept();
				DataOutputStream out1 = new DataOutputStream(client1.getOutputStream());
				out1.writeUTF("Welcome to Scribble!\n\nPlease wait for your opponent...");
				Socket client2 = serverSocket.accept();
				DataOutputStream out2 = new DataOutputStream(client2.getOutputStream());
				out2.writeUTF("Welcome to Scribble!\n\nPlease wait for your opponent...");
				
				new Thread(new Scribble(client1, client2, seed++)).start();
			}
		} catch (SocketException e) {
			System.out.println("Server encountered an error. Shutting down...");
		} catch (IOException e) {
			System.out.println("Server encountered an error. Shutting down...");
		}
    }// main method

}// SServer class
