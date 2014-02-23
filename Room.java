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

	public Room(String roomName) {
		members = new Hashtable <String, ServerThread> ();
		name = roomName;
	}

	public void join(String userName, ServerThread conn) {
		members.put(userName, conn);
	}

	public void leave(String userName) {
		members.remove(userName);
	}

	public void broadcast(String userName, String msg) throws IOException {
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
