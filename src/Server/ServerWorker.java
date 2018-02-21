package Server;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;

import a1.Command;
import a1.Constants;
import inputport.nio.manager.NIOManagerFactory;

public class ServerWorker implements Runnable {
	
	private BlockingQueue<Command> que;

	public ServerWorker(BlockingQueue<Command> que) {
		this.que = que;
	}

	@Override
	public void run() {
//		Constants.LOGGER.info("is que null = " + (que == null));
		while (true) {
			synchronized(this.que) {
				Command take;
				try {
					take = que.take();
//					Constants.LOGGER.info("DEQUED!! -- " + take.toString());
					long count = take.getSocketChannels().stream().count();
					ByteBuffer byteBuffer = take.getByteBuffer();
					Optional<Integer> nonAtomicClient = take.getNonAtomicClientHashCode();
					for(SocketChannel socketChannel : take.getSocketChannels()) {
						if ( !(nonAtomicClient.isPresent() && nonAtomicClient.get().equals(socketChannel.socket().hashCode()))) {
							NIOManagerFactory.getSingleton().write(socketChannel, byteBuffer);
						} else {
//							Constants.LOGGER.info("FOUDN THE NON ATOMIC CLINET=");
						}
					}
				//	take.getSocketChannels().forEach((socketChannel) -> NIOManagerFactory.getSingleton().write(socketChannel, byteBuffer));
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // the while (que.isEmpty can be removed)
				
			}
		}
	}

}
