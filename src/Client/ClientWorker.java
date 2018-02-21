package Client;

import java.util.concurrent.BlockingQueue;
import stringProcessors.HalloweenCommandProcessor;

/**
 * This class is the worker class that processes command messages on the simulation.
 * When the server sends a messages to the client it is intercepted by {@link ClientReceier.class}
 * which adds the message to the que. This class continuously checks the que for new messages and processes
 * them.
 * @author parth96
 *
 */
public class ClientWorker implements Runnable {
	private HalloweenCommandProcessor simulation;
	private BlockingQueue<String> que;
	
	public ClientWorker(HalloweenCommandProcessor simulation, BlockingQueue<String> que) {
		this.simulation = simulation;
		this.que = que;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				String simulationCommand = que.take();
				simulation.processCommand(simulationCommand);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public long getTime() {
		return System.currentTimeMillis();
	}

}
