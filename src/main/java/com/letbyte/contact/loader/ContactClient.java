package com.letbyte.contact.loader;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class ContactClient {
	
	private LinkedBlockingQueue<Command> queue;
	private ArrayList<Command> runningCommandList;
	private static ContactClient contactClient;
	
	private ContactClient() {
		queue = new LinkedBlockingQueue<>(128);
		runningCommandList = new ArrayList<>();
	}
	
	public static synchronized ContactClient getInstance() {
		if(contactClient == null)
			contactClient = new ContactClient();
		return contactClient;
	}
	
	public synchronized boolean addCommand(Command command) {
		if(command == null)
			return false;
		queue.add(command);
		if(runningCommandList.isEmpty())
			return executeCommand();
		return false;
	}
	
	private synchronized boolean executeCommand() {
		Command command = queue.peek();
		if(command == null)
			return false;
		queue.poll();
		command.execute();
		runningCommandList.add(command);
		return true;
	}
	
	public boolean finishCommand(Command command) {
		boolean isRemoved = runningCommandList.remove(command);//can be removeAll
		executeCommand();
		return isRemoved;
	}
}
