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
	private List <Room> chatrooms;									//A list of open rooms
	private Room entrance;										//A first room

	public static void main(String argv[]) throws Exception {
		int portNumber = Integer.parseInt(argv[0]);		//grab the port number from program arguments
		new Server(portNumber);					//start up a new server and start listening
	}

	public Server(int portNumber) throws IOException {
		chatrooms = new ArrayList <Room> ();			//instantiate room list
		entrance = new Room("Foyer");				//create our entryway
		chatrooms.add(entrance);				//Remember it in list
		listen(portNumber);					//puts Server class into listening loop
	}

	private void listen(int portNumber) throws IOException {
		ServerSocket socks = new ServerSocket(portNumber);	//make ourselves a new socket server
		Socket nextSocket;					//a Socket pointer for loop iteration
	
		System.out.println("Listening with socket server " + socks);
		
		while (true) {
			nextSocket = socks.accept();				//get the next socket
			System.out.println("Connection from " + nextSocket);
			new ServerThread(this, nextSocket, entrance);		//give it a thread
		}
	}

	public void closeSocket(Socket sock) {
		System.out.println("Closing socket "+sock);
		try {
			sock.close();				//try to close the socket

		} catch (IOException ioe) {
			System.out.println("Error closing socket "+sock);
		}
	}

	public boolean registerUser(ServerThread connection, String name) {
		if (connNames.containsKey(name)) {
			return false;						//indicates name already used
		} else {
			connNames.put(name, connection);			//Pair username to ServerThread instance
			System.out.println(name+" has logged on.\n");
			return true;						//indicates log on success
		}
	}

	public void removeUser(String name) {
		connNames.remove(name);
	}

	public Enumeration getRooms() {
		return chatrooms.elements();
	}
}
