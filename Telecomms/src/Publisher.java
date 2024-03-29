import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

public class Publisher extends Machine {
	final static int PUBLISHER_SOCKET = 50100;
	final static String CONNECT_HEADER = "000MQTT|";
	final static String CONNACK_HEADER = "001MQTT|";
	final static String PUBLISH_HEADER = "010MQTT|";
	final static String PUBACK_HEADER = "011MQTT|";
	Publisher(int port){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) 
		{
			if(port >= 51000)
				e.printStackTrace();
			else
			{
				port++;
				try {
					new Publisher(port).start();
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
				System.out.println("Connection with broker established!");
				this.notify();
			}
			else if(recievedString.contains(PUBACK_HEADER))
			{
				System.out.println("Publishing successfull");
				this.notify();
			}
		}
		catch(Exception e) {e.printStackTrace();}
	}
		
	public void sendPacket(DatagramPacket packetToSend, InetSocketAddress destination) { 
		try {
			packetToSend.setSocketAddress(destination); // set address
			socket.send(packetToSend);
		} catch (IOException e) {	e.printStackTrace(); }
		
	}
	
	
	public synchronized void start() throws Exception {
		Timer timer = new Timer(true);
		DatagramPacket connectPacket = new PacketContent(CONNECT_HEADER).toDatagramPacket();;
		DatagramPacket publishPacket;
		InetAddress localHost = InetAddress.getLocalHost();
		InetSocketAddress destination = new InetSocketAddress(localHost,50000); // manually set broker address	
		sendPacket(connectPacket,destination);
		System.out.println("Connection request sent!");
		TimeoutTimer task = new TimeoutTimer(this,connectPacket, destination);
		timer.schedule(task, 7000,7000); // 7 sec timeout timer
		this.wait();
		task.cancel();
		Scanner input = new Scanner(System.in);	
		String inputString = "";
		do
		{
			System.out.println("If you wish to publish please type 'Publish', if you wish to quit then type 'q'");
			inputString = input.nextLine();
			if(inputString.equals("Publish"))
			{
				System.out.println("Please enter a topic you want to Publish.");
				inputString = input.nextLine();
				inputString = PUBLISH_HEADER + inputString;
				System.out.println("Please enter a message to your topic.");
				String message = input.nextLine();
				inputString = inputString + "|" + message;
				publishPacket = new PacketContent(inputString).toDatagramPacket();
				sendPacket(publishPacket, destination);
				task = new TimeoutTimer(this, publishPacket, destination);
				timer.schedule(task, 7000,7000); // 7 sec timeout timer
				this.wait();
				task.cancel();
			}
			else if(!inputString.equals("q"))
				System.out.println("Invalid command, please try again.");
		}while(!inputString.equals("q"));
		input.close();
		timer.cancel();
		System.out.println("Program finished");
	}
	
	public static void main(String[] args) {
		try {					
			new Publisher(PUBLISHER_SOCKET).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
