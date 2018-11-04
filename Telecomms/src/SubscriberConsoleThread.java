import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.Timer;

public class SubscriberConsoleThread extends Thread {
	Subscriber subscriber;
	final static String SUBSCRIBE_HEADER = "100MQTT|";

	SubscriberConsoleThread(Object subscriber){
		super();
		if(subscriber instanceof Subscriber)
			this.subscriber=(Subscriber) subscriber;

	}
	@Override 
	public synchronized void run() {
		try {
		Timer timer = new Timer(true);
		TimeoutTimer task;
		InetAddress localHost = InetAddress.getLocalHost();
		DatagramPacket subscription;
		InetSocketAddress destination = new InetSocketAddress(localHost,50000);// manually set
		Scanner input = new Scanner(System.in);
		String inputString = "";
		do
		{
			System.out.println("If you wish to subscribe please type 'Subscribe', if you wish to quit then type 'q'");
			inputString = input.nextLine();
			if(inputString.equals("Subscribe"))
			{
				System.out.println("Please enter a topic you want to subscribe to.");// need to set up max
				inputString = input.nextLine();
				inputString = SUBSCRIBE_HEADER + inputString + "|"; // adding '|' at the end for proper parsing at broker
				subscription = new PacketContent(inputString).toDatagramPacket();
				subscriber.sendPacket(subscription, destination);
				
				task = new TimeoutTimer(this, subscription, destination);
				timer.schedule(task, 7000,7000); // 7 sec timeout timer
				this.wait();
				task.cancel();
			}
			else if(!inputString.equals("q"))
				System.out.println("Invalid command, please try again.");
		}while(!inputString.equals("q"));
		input.close();
		timer.cancel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized(this.subscriber) { //lock on subscriber to wake it up
			subscriber.notify();
		}
		
	}

}
