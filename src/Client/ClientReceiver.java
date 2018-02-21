package Client;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

import a1.Constants;
import inputport.nio.manager.listeners.SocketChannelReadListener;
import stringProcessors.HalloweenCommandProcessor;

/**
 * This class receives messages that are send by the server {@link ServerWorker.class} . This 
 * receiver then adds these messages to the message que. When the message que is not empty it 
 * is continually processed by the client worker thread located in {@link ClientWorker.class} 
 * @author parth96
 *
 */
public class ClientReceiver implements SocketChannelReadListener{
	private HalloweenCommandProcessor simulation;
	private BlockingQueue<String> que;
	private Runnable clientWorker;

	public ClientReceiver(HalloweenCommandProcessor simulation, BlockingQueue<String> serverMessageQue) {
		this.simulation = simulation;
		this.que = serverMessageQue;
		this.clientWorker = new ClientWorker(this.simulation, this.que);
		setUpThread();
	}

	private void setUpThread() {
		Thread thread = new Thread(this.clientWorker);
		thread.setName(Constants.READ_THREAD_NAME);
		thread.start();
	}

	@Override
	public void socketChannelRead(SocketChannel socketChannel,
			ByteBuffer message, int aLength) {
		String command = new String(message.array(), message.position(),
				aLength);
//		Constants.LOGGER.info("MESSAGE FROM SERVER ---------------" + command);
		// adds the message to the que which will be processed by the client worker.
		que.add(command);

	}
}
