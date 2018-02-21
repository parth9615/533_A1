package Client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import a1.Constants;
import assignments.util.inputParameters.SimulationParametersListener;
import inputport.nio.manager.NIOManagerFactory;
import stringProcessors.HalloweenCommandProcessor;
import util.interactiveMethodInvocation.ConsensusAlgorithm;
import util.interactiveMethodInvocation.IPCMechanism;
import util.trace.port.PerformanceExperimentEnded;
import util.trace.port.PerformanceExperimentStarted;

/**
 * This class listens for input from the simulation and the console regarding state change.
 * Depending on the state on the simulation (atomic, not atomic, local,..etc) it sends the message
 * to the server. The server intercepts these message in its receiver {@link ServerReceiver.class}
 * @author parth96
 *
 */
public class ClientSender implements PropertyChangeListener, SimulationParametersListener {

	private SocketChannel socketChannel;
	private boolean atomic;
	private Boolean local;
	private HalloweenCommandProcessor simulation;

	public ClientSender(SocketChannel socketChannel, HalloweenCommandProcessor simulation) {
		this.socketChannel = socketChannel;
		this.atomic = true;
		this.simulation = simulation;
		this.local =false;
	}
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		ByteBuffer byteBuffer = createByteBuffer(event.getNewValue().toString());
		if (!Constants.NEW_COMMAND_STRING.equals(event.getPropertyName())) {
			return;
		}	
		Constants.LOGGER.info(" SIMULATION CHANGE ------------- " + event.getNewValue().toString());
		if (local) {
			simulation.processCommand(event.getNewValue().toString());
		} else {
			NIOManagerFactory.getSingleton().write(socketChannel, byteBuffer);
		}
	}

	private ByteBuffer createByteBuffer(String value) {
		StringBuilder messageToServer = new StringBuilder(value);
		if (!this.atomic) {
			messageToServer = messageToServer.append(Constants.NON_ATOMIC_DELIMITER);
		}
		return ByteBuffer.wrap(messageToServer.toString().getBytes());
	}

	@Override
	public void atomicBroadcast(boolean newValue) {
			atomic = newValue;
			simulation.setConnectedToSimulation(!atomic);
	}
	
	@Override
	public void experimentInput() {		
		long currentTimeMillis = System.currentTimeMillis();
		PerformanceExperimentStarted.newCase(this, currentTimeMillis, Constants.EXPERIMENT_INPUT_SIZE);
		for(int i=0; i<Constants.EXPERIMENT_INPUT_SIZE; i++) {
			if (i%2 == 0) {
				this.simulation.setInputString(Constants.MOVE_FORWARD);
			} else {
				this.simulation.setInputString(Constants.MOVE_BACKWARD);
			}
		}
		PerformanceExperimentEnded.newCase(this, currentTimeMillis, System.currentTimeMillis(), 5, Constants.EXPERIMENT_INPUT_SIZE);
	}
	
	@Override
	public void localProcessingOnly(boolean newValue) {	
		this.local = newValue;
		// when local processing set simulation to false. exec command in propert change method
		// when local is false set simulation to prev state denoted by !atomic
		if (local) {
			simulation.setConnectedToSimulation(false);
		} else {
			simulation.setConnectedToSimulation(!atomic);
		}
	}
	@Override
	public void ipcMechanism(IPCMechanism newValue) {
	}
	@Override
	public void broadcastBroadcastMode(boolean newValue) {
	}
	@Override
	public void waitForBroadcastConsensus(boolean newValue) {
	}
	@Override
	public void broadcastIPCMechanism(boolean newValue) {
	}
	@Override
	public void waitForIPCMechanismConsensus(boolean newValue) {
	}
	@Override
	public void consensusAlgorithm(ConsensusAlgorithm newValue) {	
	}
	@Override
	public void quit(int aCode) {
	    System.exit(aCode);
	}
	@Override
	public void simulationCommand(String aCommand) {
		simulation.setInputString(aCommand);
		
	}


}
