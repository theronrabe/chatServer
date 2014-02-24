/*
Theron Rabe
ServerThread.java

	This file represents a thread for our Server. Each thread correlates to a user.
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
	private boolean stop = false;		//Should we disconnect?

	public ServerThread(Server process, Socket sock, Room entrance) throws IOException {
		this.process = process;					//Set values for class instance
		this.sock = sock;					//...

		recv = new BufferedReader(new InputStreamReader(sock.getInputStream()));		//Grab out input stream
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

	/*
	sendMessage: sends a string message to this particular client.
	*/
	public void sendMessage(String msg) throws IOException {
		try {
			send.writeChars(msg+"\r\n");

		} catch (IOException ioe) {
			System.out.println("Error sending message.");
			ioe.printStackTrace();
		}
	}


	/*
	login: provides a new client with a name
	*/
	private void login() throws IOException {
		String name;				//a place to keep a name
		
		try {
			sendMessage("_________________________________________________________________________________________");
			sendMessage("__________________________TOP SECRET MESSAGE DELIVERY SERVICE____________________________");
			sendMessage("___________________________________Theron Rabe. 2014.____________________________________");
			sendMessage("\r\nHelp:\t/rooms, /go [room/new room], /flee, /folks, /[codename] [secretMessage]");
			sendMessage("\nWhat is your secret codename?");
			userName = recv.readLine().replace(" ", "_");					//read name

			while (!process.registerUser(this, userName)) {					//Correct name-redundancy
				sendMessage("That one's claimed, imposter! Try something else:");
				userName = recv.readLine();
			}

			sendMessage("Welcome, "+userName+". Enjoy your totally unmonitored (trust us) secret chat...\n\n");	//Greet spy victim

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/*
	parseCommand: interprets and performs a command.
	*/
	private void parseCommand(String cmd) {
		try {
			if (cmd.indexOf("/rooms") != -1) {
				process.getRooms(this);								//provide room listing

			} else if (cmd.indexOf("/go ") != -1) {
				currentRoom.leave(userName);
				currentRoom = process.getRoom(cmd.replace("/go ", ""));				//change rooms
				currentRoom.join(userName, this);
				
			} else if (cmd.indexOf("/flee") != -1) {
				currentRoom.broadcast("Delivery Service", userName+" has fled the scene.");	//Notify room of leave
				this.stop = true;								//exit

			} else if (cmd.indexOf("/folks") != -1) {
				currentRoom.getMembers(this);							//List room members

			} else {								//Send a secret message
				int index = (cmd.indexOf(" ") > -1)? cmd.indexOf(" ") : 0;
				String name = cmd.substring(0, index).replace("/", "");		//grab recipient name
				String msg = cmd.substring(index);				//grab message
				ServerThread dst = process.getClient(name);				//grab destination thread
				if(dst != null) {
					dst.sendMessage("\nSecret message from "+userName+":\t"+msg);	//send message
					sendMessage("Message sent.");
				} else sendMessage("Delivery Service:\tuser "+name+" must be hiding somewhere else...\n");	//fail with response, if needed
				System.out.println("Private ("+userName+"->"+name+"):\t"+msg+"\n");		//log message for government surveillance
			}

		} catch (IOException ioe) {
			System.out.println("Error completing command.");
			ioe.printStackTrace();
		}
	}

	public String getUserName() {
		return userName;
	}

	public Socket getSocket() {
		return sock;
	}

	public Room getRoom() {
		return currentRoom;
	}

	/*
	run: contains our infinite loop for getting messages from the client. When this loop ends, disconnect client.
	*/
	public void run() {
		String msg;

		try {
			while (true) {
				if(this.stop) return;			//Don't crash, test early
				msg = recv.readLine();			//read input message
				if(msg == null) return;			//prevent any null-pointer exceptions. Client disconnected.
				if(msg.length() > 0) {
					System.out.println(userName + " in "+currentRoom.getName()+":\t" + msg);
	
					if(msg.charAt(0) == '/') parseCommand(msg);	//Is this a command?
					else currentRoom.broadcast(userName, msg);	//spread the word
				}
			}

		} catch (EOFException eofe) {
		} catch (IOException ioe) {
			System.out.println("I/O Error:\n");
			ioe.printStackTrace();
		} finally {
			currentRoom.leave(userName);				//Nor are we in a room
			process.removeClient(this);				//We no longer own a username
		}
	}
}
