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

	/*
	Room constructor
	*/
	public Room(String roomName) {
		members = new Hashtable <String, ServerThread> ();	//Instantiate members
		name = roomName;					//Name the room
	}

	/*
	join: adds a user to this room
	*/
	public void join(String userName, ServerThread conn) {
		members.put(userName, conn);
	}

	/*
	leave: removes a user from this room
	*/
	public void leave(String userName) {
		members.remove(userName);
	}

	public String getName() {
		return name;
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
