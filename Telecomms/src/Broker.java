import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

public class Broker extends Machine {
	final static int BROKER_SOCKET = 50000;
	
	Broker(int port){
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {e.printStackTrace();}
		listener.go();
	}
	
	public synchronized void onReceipt(DatagramPacket recievedPacket) {
		try {
			PacketContent recievedData = new PacketContent(recievedPacket);
			String recievedString = recievedData.toString();

			System.out.println(recievedString);
			// You could test here if the String says "end" and terminate the
			// program with a "this.notify()" that wakes up the start() method.
			// TODO
			DatagramPacket response;
			response= (new PacketContent("Acknowledged")).toDatagramPacket();
			sendPacket(response);
			
		}
		catch(Exception e) {e.printStackTrace();}
	}
		
	public void sendPacket(DatagramPacket packetToSend) { // look into what it does
		try {
			packetToSend.setSocketAddress(packetToSend.getSocketAddress()); // set address yourself
			socket.send(packetToSend);
		} catch (IOException e) {	e.printStackTrace(); }
		
	}
	
	public int decideDestinationSocket() // TODO
	{
		return 0;
	}
	
	public synchronized void start() throws Exception {
		System.out.println("Waiting for contact");
		this.wait();
	}
	
	public static void main(String[] args) {
		try {					
			new Broker(BROKER_SOCKET).start();
			System.out.println("Program completed");
		} catch(java.lang.Exception e) {e.printStackTrace();}
	}

}
