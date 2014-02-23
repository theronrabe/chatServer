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
		chatrooms = new ArrayList <Room> ();			//instantiate room list
		entrance = new Room("Entrance");			//create our entryway
		chatrooms.add(entrance);				//Remember it in list
		listen(portNumber);					//puts Server class into listening loop
	}

	/*
	listen: infinite loop for catching and processing new connections.
	*/
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

	/*
	removeClient: removes a client (by thread id) from the Server.
	*/
	public void removeClient(ServerThread client) {
		System.out.println("Logging out "+client.getUserName());
		try {
			client.getSocket().close();				//try to close the socket
			connNames.remove(client.getUserName());

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
		return entrance;
	}

	/*
	getRooms: sends a list of the available rooms on the server to a user.
	*/
	public void getRooms(ServerThread user) {
		try {
			for (Room R : chatrooms) {
				user.sendMessage("\t-"+R.getName());
			}
		} catch (IOException ioe) {
			System.out.println("Error sending room data.");
			ioe.printStackTrace();
		}
	}

	/*
	getClient: returns the ServerThread associated to a username
	*/
	public ServerThread getClient(String name) {
		return connNames.get(name);
	}
}
