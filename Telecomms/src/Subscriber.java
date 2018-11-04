import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
/**
 * 
 * @author Jerzy Jaskuc
 * 
 * IMPORTANT NOTE 
 * Broker Default port address is set at 50000;
 * 
 * TODO recieve messages while having console open. I.e i can write subscribe but also recieve messages
 *
 */
public class Subscriber extends Machine {
	
	final static int SUBSCRIBER_SOCKET = 51000;
	final static String CONNECT_HEADER = "000MQTT|";
	final static String CONNACK_HEADER = "001MQTT|";
	final static String SUBACK_HEADER = "101MQTT|";
	final static String PUBLISH_HEADER = "010MQTT|";
	private SubscriberConsoleThread s;
	public int generateSubscriberSocket(int offset)
	{
		return SUBSCRIBER_SOCKET+offset;
	}

	Subscriber(int port){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) 
		{
			if(port >= 60000)
				e.printStackTrace();
			else
			{
				port++;
				try {
					new Subscriber(port).start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		listener.go();
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();
			if(recievedString.contains(CONNACK_HEADER))
			{
				System.out.println("Connection with broker established");
				this.notify();
			}
			else if(recievedString.contains(SUBACK_HEADER))
			{
				System.out.println("Subscribed successfully!");
				synchronized(this.s) { // lock on thread to notify it .
					this.s.notify();
				}
			}
			else if(recievedString.contains(PUBLISH_HEADER))
			{
				String[] publisherMessage = recievedString.split("[|]");
				Thread outThread = new PublishedOutput(publisherMessage);
				outThread.start();

			}
			else
			{
				System.out.println("Recieved unidentified packet");
				System.out.println(recievedString);
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}
		
	public void sendPacket(DatagramPacket packetToSend, InetSocketAddress destination) { 
		try {
			packetToSend.setSocketAddress(destination); 
			socket.send(packetToSend);
		} catch (IOException e) {	e.printStackTrace(); }
		
	}
	
	
	public synchronized void start() throws Exception {
		Timer timer = new Timer(true);
		
		DatagramPacket connectPacket = new PacketContent(CONNECT_HEADER).toDatagramPacket();
		InetAddress localHost;
		localHost = InetAddress.getLocalHost();
		InetSocketAddress destination = new InetSocketAddress(localHost,50000);// manually set
		sendPacket(connectPacket, destination);
		System.out.println("Connection request sent!");
		
		TimeoutTimer task = new TimeoutTimer(this,connectPacket, destination);
		timer.schedule(task, 7000,7000); // 7 sec timeout timer
		this.wait();
		task.cancel();
		this.s = new SubscriberConsoleThread(this);
		this.s.start();
		this.wait();
		
	}
	
	public static void main(String[] args) {
		try {	
			new Subscriber(SUBSCRIBER_SOCKET).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}
		
}


