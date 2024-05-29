package huskabyte.dnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import huskabyte.dnd.player.DungeonsAndDragonsPlayer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;

public class DungeonsAndDragons implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("dnd");

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

		/*
		 * Wayfinder = Pivot 
		 * Raiser = Pivot without memory 
		 * Shaper = Move 
		 * Host = Clear last pivot 
		 * Silence = Return to position and clear pivots
		 */
		UseItemCallback.EVENT.register((player, world, hand) -> {
			DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer
					.getDndPlayerFromServerPlayer((ServerPlayerEntity) player);
			if (player.getStackInHand(hand).getItem() == Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE) {
				dnd.updateMeasurePoint(true);
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			if (player.getStackInHand(hand).getItem() == Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE) {
				dnd.updateMeasurePoint(false);
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			if (player.getStackInHand(hand).getItem() == Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE) {
				dnd.updatePosition();
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			if (player.getStackInHand(hand).getItem() == Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE) {
				dnd.home();
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			if (player.getStackInHand(hand).getItem() == Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE) {
				dnd.popMeasurePoint();
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			return TypedActionResult.pass(player.getStackInHand(hand));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("gm")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					dnd.makeGM();
					context.getSource().sendFeedback(() -> Text.literal("Privilege level set to GM"), true);
					return 1;
				})
				.then(CommandManager.argument("target", EntityArgumentType.players())
				.executes(context -> {
					ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					dnd.makeGM();
					context.getSource().sendFeedback(() -> Text.literal("Privilege level set to GM for " + player.getName().getString()), true);
					return 1;
				}))));
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("spec")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					dnd.makeSpectator();
					context.getSource().sendFeedback(() -> Text.literal("Privilege level set to Spectator"), true);
					return 1;
				})
				.then(CommandManager.argument("target", EntityArgumentType.players())
				.executes(context -> {
					ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					dnd.makeSpectator();
					context.getSource().sendFeedback(() -> Text.literal("Privilege level set to Spectator for " + dnd.getName()), true);
					return 1;
				}))));
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("player")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					dnd.makePlayer();
					context.getSource().sendFeedback(() -> Text.literal("Privilege level set to Player"), true);
					return 1;
				})
				.then(CommandManager.argument("target", EntityArgumentType.players())
				.executes(context -> {
					ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					dnd.makePlayer();
					context.getSource().sendFeedback(() -> Text.literal("Privilege level set to Player for " + player.getName().getString()), true);
					return 1;
				}))));
		
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("color")
				.then(CommandManager.argument("red", IntegerArgumentType.integer())
				.then(CommandManager.argument("green", IntegerArgumentType.integer())
				.then(CommandManager.argument("blue", IntegerArgumentType.integer())
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					int red = IntegerArgumentType.getInteger(context, "red");
					int green = IntegerArgumentType.getInteger(context, "green");
					int blue = IntegerArgumentType.getInteger(context, "blue");
					
					if(red > 255) red = 255;
					if(green > 255) green = 255;
					if(blue > 255) blue = 255;
					
					final int redd = red;
					final int greenn = green;
					final int bluee = blue;
					
					dnd.setColor(new int[] {red, green, blue});
					context.getSource().sendFeedback(() -> Text.literal("Color set to " + redd + " " + greenn + " " + bluee + "."), true);
					return 1;
				}))))));
	}
		
}