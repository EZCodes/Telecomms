import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class PublishedOutput extends Thread {
	String[] publisherMessage;
	
	
	PublishedOutput(String[] publisherMessage)
	{
		super();
		this.publisherMessage = publisherMessage;
	}
	
	@Override
	public void run()
	{
		JOptionPane message = new JOptionPane(publisherMessage[2]); 
		JDialog dialog = message.createDialog(null,"Message from publisher with topic: " + publisherMessage[1]);
		dialog.setModal(false);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}
}
