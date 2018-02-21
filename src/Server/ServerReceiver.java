package Server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

import a1.Command;
import a1.Constants;
import assignments.util.MiscAssignmentUtils;
import assignments.util.inputParameters.SimulationParametersListener;
import inputport.nio.manager.listeners.SocketChannelReadListener;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;

public class ServerReceiver implements SocketChannelReadListener{
	private BlockingQueue<Command> que;
	private Runnable serverWorker;
	private List<SocketChannel> clients;
	private boolean atomic;

	public ServerReceiver(BlockingQueue<Command> que, List<SocketChannel> clients) {
		this.que = que;
		serverWorker = new ServerWorker(this.que);
		this.clients = clients;
		atomic = true;
		setUpThread();
	}

	private void setUpThread() {
		Thread thread = new Thread(this.serverWorker);
		thread.setName(Constants.READ_THREAD_NAME);
		thread.start();
	}

	@Override
	public void socketChannelRead(SocketChannel socketChannel,
			ByteBuffer message, int length) {
		String request = new String(message.array(), message.position(),length);
		Optional<Integer> hashCode = Optional.empty();
		Constants.LOGGER.info("From Cleint: " + request);
		if (request.contains(Constants.NON_ATOMIC_DELIMITER)) {
			String[] command = request.split(Constants.NON_ATOMIC_DELIMITER);
			hashCode = Optional.of(socketChannel.socket().hashCode());
			request = command[0];
		}
		ByteBuffer echoToClient = ByteBuffer.wrap((request).getBytes());
		Command command = new Command(request, clients, MiscAssignmentUtils.deepDuplicate(echoToClient), hashCode);
			this.que.add(command);

	}
}
