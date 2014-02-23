/*
Theron Rabe
ServerThread.java

	This file represents a thread for our Server. Each thread correlates to a Socket.
*/
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

public class ServerThread extends Thread {
	private Server process;			//A parent process to this thread. Instance of Server class.
	private Socket sock;			//Socket encapsulated by this instance
	private DataOutputStream send;		//An output stream
	private BufferedReader recv;		//An input stream
	private String userName;		//A name associated with this connection thread
	private Room currentRoom;		//A room I am located in

	public ServerThread(Server process, Socket sock, Room entrance) throws IOException {
		this.process = process;					//Set values for class instance
		this.sock = sock;					//...

		recv = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		try {
			send = new DataOutputStream(sock.getOutputStream());	//Grab an output stream to communicate both ways
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		login();						//Ask the user to log in
		currentRoom = entrance;					//Enter somewhere
		entrance.join(userName, this);				//Sign the room registry
		start();						//Off we go!
	}

	public void sendMessage(String msg) throws IOException {
		try {
			send.writeChars(msg);

		} catch (IOException ioe) {
			System.out.println("Error sending message.");
			ioe.printStackTrace();
		}
	}


	private void login() throws IOException {
		String name;				//a place to keep a name
		
		try {
			sendMessage("\nWelcome to server XYZ.\nWho are you? ");		//Send greeting
			userName = recv.readLine();					//read name

			while (!process.registerUser(this, userName)) {			//Correct name-redundancy
				sendMessage("Name taken. Try something else: ");
				userName = recv.readLine();
			}

			sendMessage("Welcome, "+userName+". Have a chat...\n\n");	//Regreet

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void run() {
		String msg;

		try {
			while (true) {
				msg = recv.readLine();			//read input message
				System.out.println(userName + " broadcasting message: " + msg);

				if(msg == null) return;
				else currentRoom.broadcast(userName, msg+"\n");	//spread the word
			}

		} catch (EOFException eofe) {
		} catch (IOException ioe) {
			System.out.println("I/O Error:\n");
			ioe.printStackTrace();
		} finally {
			process.closeSocket(sock);				//Our infinite loop has halted, time to stop paying attention
			process.removeUser(userName);				//We no longer own a username
		}
	}
}
