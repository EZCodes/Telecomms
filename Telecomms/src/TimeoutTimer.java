import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.TimerTask;

public class TimeoutTimer extends TimerTask {
	
	Object machine;
	DatagramPacket packet;
	InetSocketAddress address;
	TimeoutTimer(Object machine, DatagramPacket packet, InetSocketAddress address)
	{
		super();
		this.machine = machine;
		this.packet = packet;
		this.address = address;
	}

	@Override
	public void run() {
		
		System.out.println("Connection timeout! Trying to resend.");
		Subscriber subTmp;
		Publisher pubTmp;
		if(machine instanceof Subscriber)
		{
			subTmp = (Subscriber) machine;
			subTmp.sendPacket(packet, address);
		}
		else if(machine instanceof Publisher)
		{
			pubTmp = (Publisher) machine;
			pubTmp.sendPacket(packet, address);
		}

					
	}

	
}
