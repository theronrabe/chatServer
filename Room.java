/*
Theron Rabe
Room.java

	This class represents a single room within the chat server. Users can join,
	leave, and broadcast to rooms.
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class Room {
	private Hashtable <String, ServerThread> members;		//Table of usernames to Threads in the room
	private String name;						//Name of room
	private Server process;						//parent server process
	private boolean keep;						//should this be auto pruned?

	/*
	Room constructor
	*/
	public Room(Server S, String roomName, boolean permanent) {
		process = S;						//Assign server reference
		members = new Hashtable <String, ServerThread> ();	//Instantiate members
		name = roomName;					//Name the room
		this.keep = permanent;
	}

	/*
	join: adds a user to this room
	*/
	public void join(String userName, ServerThread conn) {
		members.put(userName, conn);				//contribute member to list
		broadcast("Delivery Service", userName+" has strayed into "+name);	//alert everyone of new person
		getMembers(conn);					//alert user of everyone else
	}

	/*
	leave: removes a user from this room
	*/
	public void leave(String userName) {
		members.remove(userName);
		broadcast("Delivery Service", userName+" has ditched "+name);
		if(members.size() == 0 && !this.keep) process.removeRoom(this);	//auto-prune empty rooms
	}

	public String getName() {
		return name;
	}

	/*
	getSize: returns the number of users in this room
	*/
	public int getCount() {
		return members.size();
	}

	/*
	getMembers: provides a user (by thread id) a list of other members in this room.
	*/
	public void getMembers(ServerThread dst) {
		Enumeration elms;		//an enumeration of elements
		String nextUser;		//a member iterator

		synchronized(members) {
			elms = members.keys();						//get usernames
			while (elms.hasMoreElements()) {
				nextUser = (String) elms.nextElement();			//grab the next name
				try {
					dst.sendMessage("\t-"+nextUser);		//send it to destination

				} catch (IOException ioe) {
					System.out.println("Error getting usernames.");
					ioe.printStackTrace();
				}
			}
		}
	}

	/*
	broadcast: sends a message from a user to every other member of the room.
	*/
	public void broadcast(String userName, String msg) {
		Enumeration elms;			//Enumeration of members
		ServerThread nextUser;			//Member iterator
		
		synchronized(members) {
			elms = members.elements();					//grab members
			while(elms.hasMoreElements()) {					//cycle through them
				nextUser = (ServerThread) elms.nextElement();		//grab next member
				try {
					nextUser.sendMessage(userName + ":\t" + msg);	//send them the message

				} catch (IOException ioe) {				//report errors
					System.out.println("Broadcast error.");
					ioe.printStackTrace();
				}
			}
		}
	}
}
