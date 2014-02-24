/*
Theron Rabe.
Server.java

	This file represents the server side of the chat system. It behaves as a listener.
	Whenever a new connection is requested of the server, it pairs a socket to an output stream,
	places them in a hash table, and uses that pair to initiate a ServerThread.
*/

import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

public class Server {
	private Hashtable <String, ServerThread> connNames = new Hashtable <String, ServerThread> ();	//A table of name/thread pairs
	private Hashtable <String, Room> chatrooms = new Hashtable <String, Room> ();			//A list of open rooms

	/*
	Program enty point
	*/
	public static void main(String argv[]) throws Exception {
		int portNumber = Integer.parseInt(argv[0]);		//grab the port number from program arguments
		new Server(portNumber);					//start up a new server and start listening
	}

	/*
	Server constructor
	*/
	public Server(int portNumber) throws IOException {
		chatrooms.put("Secrecy Central", new Room(this, "Secrecy Central", true));
		chatrooms.put("Thieve's Guild", new Room(this, "Thieve's Guild", true));	//Initialize an entryway
		chatrooms.put("Criminal Corner", new Room(this, "Criminal Corner", true));
		chatrooms.put("Foreign Spy Forum", new Room(this, "Foriegn Spy Forum", true));

		System.out.println("___________________________________________________________________________________");
		System.out.println("________________________\"SECRET\" MESSAGE DELIVERY SERVICE__________________________");
		System.out.println("________________________________Theron Rabe. 2014._________________________________");

		listen(portNumber);					//puts Server class into listening loop
	}

	/*
	listen: infinite loop for catching and processing new connections.
	*/
	private void listen(int portNumber) throws IOException {
		ServerSocket socks = new ServerSocket(portNumber);	//make ourselves a new socket server
		Socket nextSocket;					//a Socket pointer for loop iteration

		System.out.println("Up and spying on port " + portNumber + "...");
	
		while (true) {
			nextSocket = socks.accept();				//get the next socket
			System.out.println("New connection requested from " + nextSocket.getInetAddress());
			new ServerThread(this, nextSocket, getRoom("Secrecy Central"));		//give it a thread
		}
	}

	/*
	removeClient: removes a client (by thread id) from the Server.
	*/
	public void removeClient(ServerThread client) {
		System.out.println("Logging out user "+client.getUserName());
		try {
			client.getSocket().close();				//try to close the socket
			connNames.remove(client.getUserName());			//remove from user list

		} catch (IOException ioe) {
			System.out.println("Error closing socket for"+client.getUserName());
		}
	}

	/*
	registerUser: adds a client (by name) to the user-base.
	*/
	public boolean registerUser(ServerThread connection, String name) {
		if (connNames.containsKey(name)) {
			return false;						//indicates name already used
		} else {
			connNames.put(name, connection);			//Pair username to ServerThread instance
			System.out.println(name+" has logged on.\n");
			return true;						//indicates log on success
		}
	}

	public Room getRoom(String roomName) {
		if(!chatrooms.containsKey(roomName)) {
			chatrooms.put(roomName, new Room(this, roomName, false));		//create room, if it doesn't exist
		}
		return chatrooms.get(roomName);
	}

	public void removeRoom(Room R) {
		chatrooms.remove(R.getName());
	}

	/*
	getRooms: sends a list of the available rooms on the server to a user.
	*/
	public void getRooms(ServerThread user) {
		Enumeration elms;		//an enumerator
		String nextRoom;		//an iterator

		elms = chatrooms.keys();			//grab enumeration
		while (elms.hasMoreElements()) {
			nextRoom = (String) elms.nextElement();	//grab iteration
			try {
				user.sendMessage("\t-"+nextRoom+" ("+chatrooms.get(nextRoom).getCount()+")");	//send room data

			} catch (IOException ioe) {
				System.out.println("Error sending room data.");
				ioe.printStackTrace();
			}
		}
	}

	/*
	getClient: returns the ServerThread associated to a username
	*/
	public ServerThread getClient(String name) {
		return connNames.get(name);
	}
}
