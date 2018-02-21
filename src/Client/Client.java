package Client;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

import a1.Constants;
import assignments.util.inputParameters.ASimulationParametersController;
import assignments.util.mainArgs.ClientArgsProcessor;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.factories.classes.AConnectCommandFactory;
import inputport.nio.manager.factories.classes.AReadingWritingConnectCommandFactory;
import inputport.nio.manager.factories.selectors.ConnectCommandFactorySelector;
import inputport.nio.manager.listeners.SocketChannelConnectListener;
import main.BeauAndersonFinalProject;
import stringProcessors.HalloweenCommandProcessor;
import util.annotations.Tags;
import util.interactiveMethodInvocation.SimulationParametersController;
import util.tags.DistributedTags;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.port.nio.NIOTraceUtility;

/**
 * This class sets up the connection to the server and connects 
 * it with the appropriate listeners and receivers.
 * @author parth96
 *
 */
@Tags({DistributedTags.CLIENT})
public class Client implements SocketChannelConnectListener{	
	private HalloweenCommandProcessor simulation;
	private String clientName;
	private ClientSender sender;
	private ClientReceiver receiver;
	private SocketChannel socketChannel;
	private BlockingQueue<String> serverMessageQue;

	public Client(String aClientName) {
		clientName = aClientName;
	}
	
	public void initialize(String serverHost, int serverPort) {	
		setUpThreadWork();
		createSimulation();
		setFactories();
		socketChannel = createSocketChannel();
		createReceiver();
		addReadListeners();
		connectToServer(serverHost, serverPort);
	}

	private void setUpThreadWork() {
		// que used to store command messages to interact with worker thread.s
		this.serverMessageQue = new ArrayBlockingQueue<>(Constants.STORAGE_SIZE);
	}

	private void createSimulation() {
		this.simulation = makeSimulation();
		simulation.setConnectedToSimulation(false);
	}
	
	private void createInputListener() {
		SimulationParametersController simulationParametersController = 
				new ASimulationParametersController();
		simulationParametersController.addSimulationParameterListener(this.sender);
		simulationParametersController.processCommands();
	}
	
	private HalloweenCommandProcessor makeSimulation() {
		return BeauAndersonFinalProject.createSimulation(
				Constants.SIMULATION1_PREFIX,
				Constants.SIMULATION1_X_OFFSET, 
				Constants.SIMULATION_Y_OFFSET, 
				Constants.SIMULATION_WIDTH, 
				Constants.SIMULATION_HEIGHT, 
				Constants.SIMULATION1_X_OFFSET, 
				Constants.SIMULATION_Y_OFFSET);
	}
	protected void setFactories() {		
		ConnectCommandFactorySelector.setFactory(new AReadingWritingConnectCommandFactory());

	}

	protected SocketChannel createSocketChannel() {
		try {
			return SocketChannel.open();
		} catch (Exception e) {
//			Constants.LOGGER.log(Level.SEVERE, "could not connect", e);
			return null;
		}
	}
	
	protected void connectToSocketChannel(String serverHost, int serverPort) {
		try {
			InetAddress serverAddr = InetAddress.getByName(serverHost);
			NIOManagerFactory.getSingleton().connect(socketChannel,
					serverAddr, serverPort, this);
		} catch (IOException e) {
//			Constants.LOGGER.log(Level.SEVERE, "could not connect to socket channel", e);
		}
	}
	
	private void createReceiver() {
		receiver = new ClientReceiver(this.simulation, serverMessageQue);
	}
	
	protected void addReadListeners() {
		addReadListener();
	}
	
	public void connectToServer(String serverHost, int serverPort) {
		Constants.LOGGER.info("in this method");
		connectToSocketChannel(serverHost, serverPort);
		createCommunicationObjects();
	}
	
	protected void createCommunicationObjects() {
		createSender();
		setSimulationListener();
		createInputListener();
	}
	
	protected void createSender() {
		sender = new ClientSender(socketChannel,simulation);
	}
	
	private void addReadListener() {
		NIOManagerFactory.getSingleton().addReadListener(socketChannel,
				receiver);
	}
	private void setSimulationListener() {
		simulation.addPropertyChangeListener(this.sender);
	}

	@Override
	public void connected(SocketChannel aSocketChannel) {
		System.out.println("Ready to send messages to server");
	}
	
	@Override
	public void notConnected(SocketChannel aSocketChannel, Exception e) {
//		Constants.LOGGER.log(Level.SEVERE, "Could not connect", e);
	    System.out.println("could not connect");
	}
	
	/**
	 * Connect the client with the specified name to the specified server.
	 */
	public static void launchClient(String serverHost, int serverPort,
			String clientName) {
	    
		Client client = new Client(
				clientName);
		client.initialize(serverHost, serverPort);	
	}

	public static void main(String[] args) {	
	    /*
		 * Put these two in your clients also
		 */
		FactoryTraceUtility.setTracing();
		BeanTraceUtility.setTracing();
		NIOTraceUtility.setTracing();
		launchClient(ClientArgsProcessor.getServerHost(args),
				ClientArgsProcessor.getServerPort(args),
				ClientArgsProcessor.getClientName(args));
	}
	
}
