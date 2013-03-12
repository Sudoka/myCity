/**
 * 
 */
package edu.ucsd.mycity.chat;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.util.Log;

import edu.ucsd.mycity.GTalkHandler;
import edu.ucsd.mycity.buddy.BuddyEntry;

/**
 * @author Aaron
 *
 */

public class ChatRoom implements PacketListener, MessageListener {
	
	public class ChatMessage {
		private BuddyEntry from;
		private String msg;
		
		public ChatMessage( BuddyEntry from, String msg ) {
			this.from = from;
			this.msg = msg;
		}
		
		public BuddyEntry getFrom() {
			return from;
		}
		public String getMsg() {
			return msg;
		}
	}
	
	private String title;
	private boolean isMultiUser;
	
	private Chat sc = null;
	private MultiUserChat muc = null;

	private BuddyEntry participant;					// Single user mode: user addr, Multi-user mode: chatroom addr
	private ArrayList<BuddyEntry> participants;		// Multi-user mode
	private ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();
	
	/**
	 * Single user mode
	 */
	public ChatRoom(Chat newsc, BuddyEntry participant) {
		isMultiUser = false;
		sc = newsc;
		sc.addMessageListener(this);
		
		this.participant = participant;
		this.title = participant.getName();
	}
	
	/**
	 * XMPPConnect needed in order to create 
	 * @param connection
	 */
	public ChatRoom(MultiUserChat newmuc, String title) {
		isMultiUser = true;
		muc = newmuc;
		//try {
		//	muc.join( GTalkHandler.getUserBareAddr() );
		//} catch (XMPPException e) {
		//	Log.e("ChatRoom", "Error in create room: "+e.toString());
		//}
		
		muc.addMessageListener(this);
		this.title = title;
	}
	
	public boolean isMultiUser() {
		return isMultiUser;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	// Add a message to the message list
	public void addMessage(BuddyEntry contact, String msg) {
		Log.d("ChatRoom", "addMessage init.");
		messages.add( new ChatMessage(contact, msg) );
		Log.d("ChatRoom", "addMessage done.");
	}
	
	public void setChat(Chat newsc) {
		if (newsc == null)
			return;
		
		sc = newsc;
	}
	
	public void setMulChat(MultiUserChat newmuc) {
		if (newmuc == null)
			return;
		
		muc = newmuc;
	}

	// Return boolean indicates if send is successful
	public boolean sendMessage(String message) {
		addMessage(null, message);	// contact = null indicates myself
		
		if ( isMultiUser ) {
			try {
				muc.sendMessage("<from>"+GTalkHandler.getUserBareAddr()+"</from>"+message);
			} catch (XMPPException e) {
				return false;
			}
			return true;
		}
		
		// Single-user mode
		if ( !participant.getPresence().isAvailable() )
			addMessage(null, "(The recipient is offline, message will be delivered when the user is online.)");
		
		Message msg = new Message(participant.getUser(), Message.Type.chat);
		msg.setBody(message);
		return GTalkHandler.sendMessage(msg);
	}
	
	@Override
	// For multi-user only
	public void processPacket(Packet packet) {
		/***** Disabled
		Message message = (Message) packet;
		
		if (message.getBody() == null)
			return;
		
		addMessage( BuddyHandler.getBuddy( BuddyHandler.getBareAddr(message.getFrom()) ), message.getBody() );
		*/
	}

	@Override
	// For single-user only
	public void processMessage(Chat chat, Message message) {

		/***** Disabled
		if (message.getBody() == null)
			return;
		
		// New incoming message
		addMessage( participant, message.getBody() );
		 */
	}

	public boolean addParticipants(ArrayList<BuddyEntry> buddies, String invitemsg) {
		// Ignore request if in single-user mode
		if ( !isMultiUser || muc == null )
			return false;
		
		for ( BuddyEntry buddy : buddies )
			muc.invite(buddy.getUser(), invitemsg);
		
		return true;
	}

	public BuddyEntry getParticipant() {
		return participant;
	}
	
	public ArrayList<BuddyEntry> getParticipants() {
		return participants;
	}
	
	public ArrayList<ChatMessage> getMessages() {
		return messages;
	}


	


}
