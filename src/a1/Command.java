package a1;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Command {
	private String request;
	private List<SocketChannel> clients;
	private ByteBuffer byteBuffer;
	private Optional<Integer> nonAtomicHashCode;
	public Command(String type, List<SocketChannel> clients, ByteBuffer byteBuffer, Optional<Integer> hashCode) {
		this.request = type;
		this.clients = clients;
		this.byteBuffer = byteBuffer;
		this.nonAtomicHashCode = hashCode;
	}

	public String toString() {
		return request;
	}
	
	public List<SocketChannel> getSocketChannels() {
		return Collections.unmodifiableList(clients);
	}
	
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}
	
	public Optional<Integer> getNonAtomicClientHashCode() {
		return this.nonAtomicHashCode;
	}
}
