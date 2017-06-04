package me.alexander.mcbot;

import java.net.Proxy;

import org.apache.log4j.Logger;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.game.ClientRequest;
import com.github.steveice10.mc.protocol.data.game.MessageType;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.data.message.TranslationMessage;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.PacketSentEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;
import com.github.steveice10.packetlib.event.session.SessionListener;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;

public class Bot {

	private final Logger logger = Logger.getRootLogger();
	private final String username, password;
	private Client client;
	private boolean autoReconnect = true;

	public Bot(String username) {
		this(username, "");
	}

	public Bot(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void connect(String host) {
		if (host.contains(":")) {
			connect(host.split(":")[0], Integer.valueOf(host.split(":")[1]));
		} else {
			connect(host, 25565);
		}
	}

	public void connect(final String host, final int port) {
		logger.info("Connnecting to " + host + ":" + port);
		MinecraftProtocol protocol = null;
		if (!password.equals("")) {
			try {
				protocol = new MinecraftProtocol(username, password, false);
			} catch (Exception ex) {
				protocol = new MinecraftProtocol(username);
			}
		} else {
			protocol = new MinecraftProtocol(username);
		}

		client = new Client(host, port, protocol, new TcpSessionFactory(Proxy.NO_PROXY));
		client.getSession().addListener(new SessionAdapter() {

			@Override
			public void packetReceived(PacketReceivedEvent event) {
				if (event.getPacket() instanceof ServerJoinGamePacket) {
					logger.info("Connected to " + host + ":" + port);
				} else if (event.getPacket() instanceof ServerChatPacket) {
					Message message = event.<ServerChatPacket>getPacket().getMessage();
					if (message instanceof TranslationMessage) {
						logger.info("Chat message: { Server: " + host + ":" + port + ", Message: "
								+ ((TranslationMessage) message).getTranslationParams()[0] + " => "
								+ ((TranslationMessage) message).getTranslationParams()[1] + " }");
					} else if (event.<ServerChatPacket>getPacket().getType().equals(MessageType.SYSTEM)) {
						logger.info(event.getPacket());
					}
				}
			}

			@Override
			public void packetSent(PacketSentEvent event) {

			}

			@Override
			public void disconnected(DisconnectedEvent event) {
				logger.info("Disconnected from " + event.getSession().getHost() + ":" + event.getSession().getPort()
						+ ", reason: " + Message.fromString(event.getReason()).getFullText());
				if (autoReconnect) {
					new Thread(() -> {
						logger.info("Reconnecting to " + event.getSession().getHost() + ":"
								+ event.getSession().getPort() + " in 5 seconds...");
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						connect(host, port);
					}).start();
				}
			}

		});

		client.getSession().connect();
	}

	/*
	 * Handlers
	 */

	/**
	 * Disconnects from the current server
	 * 
	 * @param reason
	 */
	public void disconnect(String reason) {
		if (client.getSession().isConnected()) {
			client.getSession().disconnect(reason);
		}
	}

	/**
	 * Sends a chat message
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		if (client.getSession().isConnected()) {
			client.getSession().send(new ClientChatPacket(message));
		}
	}

	/**
	 * Adds a session listener to the bot
	 * 
	 * @param listener
	 */
	public void addListener(SessionListener listener) {
		client.getSession().addListener(listener);
	}

	/**
	 * 
	 * Returns null if not connected, returns a Connection object with the
	 * current server data
	 * 
	 * @return
	 */
	public boolean isConnected() {
		if (client != null) {
			return client.getSession().isConnected();
		}
		return false;
	}

	/**
	 * Sends a packet
	 * 
	 * @param packet
	 */
	public void sendPacket(Packet packet) {
		if (isConnected()) {
			client.getSession().send(packet);
		}
	}

	/**
	 * Respawns the bot
	 */
	public void respawnBot() {
		if (isConnected()) {
			sendPacket(new ClientRequestPacket(ClientRequest.RESPAWN));
		}
	}

}
