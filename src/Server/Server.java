package Server;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import a1.Command;
import a1.Constants;
import assignments.util.MiscAssignmentUtils;
import assignments.util.mainArgs.ServerArgsProcessor;
import inputport.nio.manager.NIOManagerFactory;
import inputport.nio.manager.factories.classes.AReadingAcceptCommandFactory;
import inputport.nio.manager.factories.selectors.AcceptCommandFactorySelector;
import inputport.nio.manager.listeners.SocketChannelAcceptListener;
import util.trace.bean.BeanTraceUtility;
import util.trace.factories.FactoryTraceUtility;
import util.trace.port.nio.NIOTraceUtility;
import util.trace.port.nio.SocketChannelBound;
import util.annotations.Tags;
import util.tags.DistributedTags;

@Tags({DistributedTags.SERVER})
public class Server implements SocketChannelAcceptListener{
	private ServerReceiver receiver;
	private  List<SocketChannel> clients;
	private ServerSocketChannel serverSocketChannel;
	private BlockingQueue<Command> clientMessagesQue;
	
	public void initialize(int serverPort) {
		setUpThreadWork();
		setFactories();		
		createSocketChannel(serverPort);
		createReceiver();
		makeServerConnectable();
	}

	private void setUpThreadWork() {
		this.clientMessagesQue = new ArrayBlockingQueue<>(Constants.STORAGE_SIZE);
		clients = new ArrayList<>();
	}
	
	protected void setFactories() {
		AcceptCommandFactorySelector.setFactory(new AReadingAcceptCommandFactory());
	}

	protected void createSocketChannel(int serverPort) {
		try {
			 serverSocketChannel = ServerSocketChannel.open();
			InetSocketAddress isa = new InetSocketAddress(serverPort);
			serverSocketChannel.socket().bind(isa);
			SocketChannelBound.newCase(this, serverSocketChannel, isa);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void createReceiver() {
		receiver = new ServerReceiver(this.clientMessagesQue, this.clients);
	}

	protected void makeServerConnectable() {
		NIOManagerFactory.getSingleton().enableListenableAccepts(
				serverSocketChannel, this);
	}

	protected void addListeners(SocketChannel socketChannel) {
		addReadListener(socketChannel);		
	}

	protected void addReadListener(SocketChannel socketChannel) {
		NIOManagerFactory.getSingleton().addReadListener(socketChannel,
				receiver);
	}
	
	@Override
	public void socketChannelAccepted(ServerSocketChannel serverSocketChannel,
			SocketChannel socketChannel) {
		clients.add(socketChannel);
		addListeners(socketChannel);
	}

	public static void main(String[] args) {
	    FactoryTraceUtility.setTracing();
		NIOTraceUtility.setTracing();
		BeanTraceUtility.setTracing();// not really needed, but does not hurt
//		MiscAssignmentUtils.setHeadless(true);
		launch(args);
	}
	
	public static void launch(String[] args) {
		Server server = new Server();
		server.initialize(ServerArgsProcessor.getServerPort(args));
	}
}
