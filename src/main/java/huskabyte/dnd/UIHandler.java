package huskabyte.dnd;

import java.util.ArrayList;
import java.util.HashMap;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import huskabyte.dnd.initiative.InitiativeMember;
import huskabyte.dnd.initiative.InitiativeMonster;
import huskabyte.dnd.initiative.InitiativeTracker;
import huskabyte.dnd.player.DungeonsAndDragonsPlayer;
import huskabyte.dnd.player.DungeonsAndDragonsRenderer;
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
				dnd.setRenderMode(dnd.getRenderMode().next());
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
				.then(CommandManager.argument("red", IntegerArgumentType.integer(0, 255))
				.then(CommandManager.argument("green", IntegerArgumentType.integer(0, 255))
				.then(CommandManager.argument("blue", IntegerArgumentType.integer(0, 255))
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					final int red = IntegerArgumentType.getInteger(context, "red");
					final int green = IntegerArgumentType.getInteger(context, "green");
					final int blue = IntegerArgumentType.getInteger(context, "blue");
					
					dnd.setColor(new int[] {red, green, blue});
					context.getSource().sendFeedback(() -> Text.literal("Color set to " + red + " " + green + " " + blue + "."), true);
					return 1;
				}))))));

		/*
		 * Command to set radius of circles and cone hitboxes
		 * radius = radius in feet.
		 * 
		 * Circle hitboxes are anchored to the last waypoint.
		 * Cones always follow the player, including orientation.
		 */
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("radius")
				.then(CommandManager.argument("r", DoubleArgumentType.doubleArg(0))
				.executes(context -> {
					ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
					DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
					double radius = DoubleArgumentType.getDouble(context, "r");
					
					dnd.setRadius(radius / DungeonsAndDragons.DISTANCE_SCALE);
					context.getSource().sendFeedback(() -> Text.literal("Radius set to " + radius + " feet."), true);
					return 1;
				}))));

		/*
		 * Command to set particle density
		 * radius = radius in feet.
		 * 
		 * Circle hitboxes are anchored to the last waypoint.
		 * Cones always follow the player, including orientation.
		 */
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("particles")
				.then(CommandManager.argument("linear", DoubleArgumentType.doubleArg(0.01, 10))
				.executes(context -> {
					double linearDensity = DoubleArgumentType.getDouble(context, "linear");
					
					DungeonsAndDragonsRenderer.LINE_SPACING = linearDensity;
					context.getSource().sendFeedback(() -> Text.literal("Linear particle density set to " + linearDensity + "."), true);
					return 1;
				})
				.then(CommandManager.argument("sphere", DoubleArgumentType.doubleArg(0.001, 0.1))
				.executes(context -> {
					double sphereDensity = DoubleArgumentType.getDouble(context, "sphere");

					DungeonsAndDragonsRenderer.SPHERE_DENSITY = sphereDensity;
					context.getSource().sendFeedback(() -> Text.literal("Sphere particle density set to " + sphereDensity + "."), true);
					return 1;
				})))));
		
		/*
		 * Swap between line broadcast modes
		 * OFF = no one
		 * SELF = only you
		 * ALL = everyone
		 * GM = you and all GMs and Spectators
		 * 
		 * TODO turn them into subcommands
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
		
		/*
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
		
		/*
		 * Initiative Tracker Commands (init)
		 * 
		 * ADMIN
		 * start = clean and start initiative
		 * end = end and clean initiative
		 * toggle = activate/deactivate initiative
		 * clear = clear initiative
		 * 
		 * ORDER
		 * join <init> <dex> [target] = join initiative with init roll and dex mod; OPT target D&D player to add
		 * madd <name> <init> <dex> = join initiative as a monster with name
		 * remove <name> = remove first target with name
		 * removeat <turn> = remove target at index
		 * 
		 * TRACKING
		 * next = go next in initiative order
		 * list = list all items in initiative order, with turn indication and init count
		 */
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher
				.register(CommandManager.literal("init")
				.then(CommandManager.literal("start")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							InitiativeTracker.start();
							context.getSource().sendFeedback(() -> Text.literal("Started new initiative."), true);
							return 1;
						}
						))
				.then(CommandManager.literal("end")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							InitiativeTracker.end();
							context.getSource().sendFeedback(() -> Text.literal("Initiative cleared and ended."), true);
							return 1;
						}
						))
				.then(CommandManager.literal("toggle")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							InitiativeTracker.toggleActive();
							context.getSource().sendFeedback(() -> Text.literal("Initiative state toggled."), true);
							return 1;
						}
						))
				.then(CommandManager.literal("clear")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							InitiativeTracker.clear();
							context.getSource().sendFeedback(() -> Text.literal("Cleared Initiative"), true);
							return 1;
						}
						))
				//TODO join function - last 3 lines can be abstracted
				.then(CommandManager.literal("join")
						.then(CommandManager.argument("init", IntegerArgumentType.integer())
						.then(CommandManager.argument("dex", IntegerArgumentType.integer())
						.executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
							DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
							InitiativeTracker.add(dnd, IntegerArgumentType.getInteger(context, "init"), IntegerArgumentType.getInteger(context, "dex"));
							context.getSource().sendFeedback(() -> Text.literal("Added " + dnd.getName() + " at Initiative " + IntegerArgumentType.getInteger(context, "init")), true);
							return 1;
						}
						)
						.then(CommandManager.argument("target", EntityArgumentType.player())
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "target");
							DungeonsAndDragonsPlayer dnd = DungeonsAndDragonsPlayer.getDndPlayerFromServerPlayer(player);
							InitiativeTracker.add(dnd, IntegerArgumentType.getInteger(context, "init"), IntegerArgumentType.getInteger(context, "dex"));
							context.getSource().sendFeedback(() -> Text.literal("Added " + dnd.getName() + " at Initiative " + IntegerArgumentType.getInteger(context, "init")), true);
							return 1;
						})
						))))
				.then(CommandManager.literal("madd")
						.then(CommandManager.argument("name", StringArgumentType.string())
						.then(CommandManager.argument("init", IntegerArgumentType.integer())
						.then(CommandManager.argument("dex", IntegerArgumentType.integer())
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							InitiativeTracker.add(new InitiativeMonster(StringArgumentType.getString(context, "name")), IntegerArgumentType.getInteger(context, "init"), IntegerArgumentType.getInteger(context, "dex"));
							context.getSource().sendFeedback(() -> Text.literal("Added " + StringArgumentType.getString(context, "name") + " at Initiative " + IntegerArgumentType.getInteger(context, "init")), true);
							return 1;
						}
						)))))
				.then(CommandManager.literal("remove")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("name", StringArgumentType.string())
						.executes(context -> {
							ArrayList<InitiativeMember> order = InitiativeTracker.getOrder();
							String name = StringArgumentType.getString(context, "name");
							for(int i = 0; i < order.size(); i++) {
								if(order.get(i).getName().equals(name)) {
									InitiativeTracker.remove(i);
									context.getSource().sendFeedback(() -> Text.literal("Removed " + name + " from initiative order."), true);
									break;
								}	
							}
							return 1;
						}
						)))
				.then(CommandManager.literal("removeat")
						.requires(source -> source.hasPermissionLevel(2))
						.then(CommandManager.argument("zeroindexedposition", IntegerArgumentType.integer(0, InitiativeTracker.getOrder().size()-1))
						.executes(context -> {
							InitiativeTracker.remove(IntegerArgumentType.getInteger(context, "zeroindexedposition"));
							context.getSource().sendFeedback(() -> Text.literal("Removed position " + IntegerArgumentType.getInteger(context, "zeroindexedposition") + " from initiative order."), true);
							return 1;
						}
						)))
				.then(CommandManager.literal("next")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> {
							InitiativeTracker.next();
							context.getSource().sendFeedback(() -> Text.literal("Moved combat up by 1 turn. " + InitiativeTracker.getOrder().get(InitiativeTracker.turn()).getName() + "'s turn is up."), true);
							return 1;
						}
						)
						.then(CommandManager.argument("turns", IntegerArgumentType.integer())
						.executes(context -> {
							InitiativeTracker.next(IntegerArgumentType.getInteger(context, "turns"));
							context.getSource().sendFeedback(() -> Text.literal("Moved combat up by " + IntegerArgumentType.getInteger(context, "turns") + " turns. " + InitiativeTracker.getOrder().get(InitiativeTracker.turn()).getName() + "'s turn is up."), true);
							return 1;
						})))
				.then(CommandManager.literal("list")
						.executes(context -> {
							ArrayList<InitiativeMember> order = InitiativeTracker.getOrder();
							HashMap<InitiativeMember, int[]> props = InitiativeTracker.getProperties();
							int turn = InitiativeTracker.turn();
							String s = "\n";
							for(int i = 0; i < order.size(); i++) {
								if(i == turn) {
									s+="-->";
								}
								s+=(props.get(order.get(i))[InitiativeTracker.INIT] + " " + order.get(i).getName()+"\n");
							}
							final String out = s;
							context.getSource().sendFeedback(() -> Text.literal(out), false);
							return 1;
						}
						))
				));
	}
}
