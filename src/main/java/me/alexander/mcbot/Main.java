package me.alexander.mcbot;

import org.apache.log4j.Logger;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerJoinGamePacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionAdapter;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) {
		Utils.initLogger();
		logger.info("hey");
		final Bot b = new Bot("MrBot");
		b.connect("localhost");
		b.addListener(new SessionAdapter() {
			@Override
			public void packetReceived(PacketReceivedEvent event) {
				if (event.getPacket() instanceof ServerJoinGamePacket) {
					b.sendPacket(new ClientChatPacket("This is a bot"));
				}
			}
		});
	}

}
