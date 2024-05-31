package huskabyte.dnd;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import huskabyte.dnd.initiative.InitiativeTracker;
import huskabyte.dnd.player.DungeonsAndDragonsPlayer;
import huskabyte.dnd.player.PlayerType;
import huskabyte.dnd.player.ShowMeasure;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;

public class UIHandler {
	/**
	 * Register callback on item use <br></br>
	 * 
	 * NOTE: All other item interactions seem to require you to mix in, which needs its own file.
	 */
	public static void initializeItemUI() {
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
	}
	
	/**
	 * Register all commands
	 */
	public static void initializeCommands() {
		
		/*
		 * Privilege level command
		 * GM = game master privileges
		 * SPEC = spectator privileges
		 * PLAYER = player privileges
		 */
		
		//GM
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
					context.getSource().sendFeedback(() -> Text.literal("Privilege level set to GM for " + dnd.getName()), true);
					return 1;
				}))));
		
		//SPEC
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
		
		//PLAYER
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
					context.getSource().sendFeedback(() -> Text.literal("Privilege level set to Player for " + dnd.getName()), true);
					return 1;
				}))));
		
		/*
		 * Command to set color
		 * red = R of rgb
		 * green = G of rgb
		 * blue = B of rgb
		 * 
		 * Also clamps at 255 and 0
		 */
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
					
					if(red < 0) red = 0;
					if(green < 0) green = 0;
					if(blue < 0) blue = 0;
					
					//L apparently Text.literal() needs final (or effectively final) args except the other ones get changed.
					final int redd = red;
					final int greenn = green;
					final int bluee = blue;
					
					dnd.setColor(new int[] {red, green, blue});
					context.getSource().sendFeedback(() -> Text.literal("Color set to " + redd + " " + greenn + " " + bluee + "."), true);
					return 1;
				}))))));
		
		/*
		 * Swap between line broadcast modes
		 * OFF = no one
		 * SELF = only you
		 * ALL = everyone
		 * GM = you and all GMs and Spectators
		 * 
		 * TODO mix into tab completion event to send options
		 */
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("mode")
				.requires(source -> source.hasPermissionLevel(2))
				.then(CommandManager.argument("measuremode", StringArgumentType.word())
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					String mode = StringArgumentType.getString(context, "measuremode");
					mode = mode.toLowerCase();
					if(mode.equals("off")) {
						dnd.setMode(ShowMeasure.OFF);
						context.getSource().sendFeedback(() -> Text.literal("Mode set to OFF"), true);
						return 1;
					}
					if(mode.equals("self")) {
						dnd.setMode(ShowMeasure.SELF);
						context.getSource().sendFeedback(() -> Text.literal("Mode set to SELF"), true);
						return 1;
					}
					if(mode.equals("all")) {
						context.getSource().sendFeedback(() -> Text.literal("Mode set to ALL"), true);
						dnd.setMode(ShowMeasure.ALL);
						return 1;
					}
					context.getSource().sendFeedback(() -> Text.literal("Mode set to GM"), true);
					dnd.setMode(ShowMeasure.GM);
					return 1;
				})
				.then(CommandManager.argument("target", EntityArgumentType.player())
				.executes(context -> {
					ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					String mode = StringArgumentType.getString(context, "measuremode");
					mode = mode.toLowerCase();
					if(mode.equals("off")) {
						dnd.setMode(ShowMeasure.OFF);
						context.getSource().sendFeedback(() -> Text.literal("Mode set to OFF for " + dnd.getName()), true);
						return 1;
					}
					if(mode.equals("self")) {
						dnd.setMode(ShowMeasure.SELF);
						context.getSource().sendFeedback(() -> Text.literal("Mode set to SELF for " + dnd.getName()), true);
						return 1;
					}
					if(mode.equals("all")) {
						context.getSource().sendFeedback(() -> Text.literal("Mode set to ALL for " + dnd.getName()), true);
						dnd.setMode(ShowMeasure.ALL);
						return 1;
					}
					context.getSource().sendFeedback(() -> Text.literal("Mode set to GM for " + dnd.getName()), true);
					dnd.setMode(ShowMeasure.GM);
					return 1;
				})))));
		
		/**
		 * Initiative-smart on/off toggle for line visibility without op
		 */
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("toggleline")
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					if(dnd.getMode() != ShowMeasure.OFF) {
						dnd.setMode(ShowMeasure.OFF);
						context.getSource().sendFeedback(() -> Text.literal("Mode set to OFF"), true);
						return 1;
					}
					if(dnd.getType() == PlayerType.SPECTATOR) {
						dnd.setMode(ShowMeasure.SELF);
						Text.literal("Mode set to SELF");
						return 1;
					}
					if(InitiativeTracker.getActive() && InitiativeTracker.turn(dnd) && dnd.getType() == PlayerType.PLAYER) {
						dnd.setMode(ShowMeasure.ALL);
						Text.literal("Mode set to ALL");
						return 1;
					}
					dnd.setMode(ShowMeasure.GM);
					Text.literal("Mode set to GM");
					return 1;
				})
				.then(CommandManager.argument("target", EntityArgumentType.player())
				.executes(context -> {
					ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					if(dnd.getMode() != ShowMeasure.OFF) {
						dnd.setMode(ShowMeasure.OFF);
						context.getSource().sendFeedback(() -> Text.literal("Mode set to OFF for " + dnd.getName()), true);
						return 1;
					}
					if(dnd.getType() == PlayerType.SPECTATOR) {
						dnd.setMode(ShowMeasure.SELF);
						Text.literal("Mode set to SELF for " + dnd.getName());
						return 1;
					}
					if(InitiativeTracker.getActive() && InitiativeTracker.turn(dnd) && dnd.getType() == PlayerType.PLAYER) {
						dnd.setMode(ShowMeasure.ALL);
						Text.literal("Mode set to ALL for " + dnd.getName());
						return 1;
					}
					dnd.setMode(ShowMeasure.GM);
					Text.literal("Mode set to GM for " + dnd.getName());
					return 1;
				}))));
	}
}
