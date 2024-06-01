package huskabyte.dnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import huskabyte.dnd.player.DungeonsAndDragonsPlayer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class DungeonsAndDragons implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("dnd");

	/**
	 * Scale factor to convert between minecraft distance (blocks) and dnd distance (feet).
	 * TODO: Make this configurable via a command.
	 */
	public static double DISTANCE_SCALE = 2.5;

	@Override
	public void onInitialize() {

		LOGGER.info("Initializing Dungeons and Dragons...");
		/*
		 * Wrap players as D&D players on join
		 */
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			DungeonsAndDragonsPlayer dnd = new DungeonsAndDragonsPlayer(handler.getPlayer());
			dnd.updatePosition();
		});
		/*
		 * Respawn changes the ServerPlayerEntity you are. Change reference in DNDP
		 */
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(oldPlayer);
			dnd.replacePlayer(newPlayer);
		});
		/*
		 * Avoid duplication and nullpointer players
		 */
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(handler.player);
			dnd.destruct();
		});
		
		UIHandler.initializeItemUI();
		UIHandler.initializeCommands();
	}
		
}