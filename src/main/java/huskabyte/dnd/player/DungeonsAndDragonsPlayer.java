package huskabyte.dnd.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.jetbrains.annotations.Nullable;

import huskabyte.dnd.DungeonsAndDragons;
import huskabyte.dnd.initiative.InitiativeMember;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;

/**
 * D&D Wrapper for Minecraft ServerPlayerEntity
 */
public class DungeonsAndDragonsPlayer implements InitiativeMember {
	public static final HashMap<ServerPlayerEntity, DungeonsAndDragonsPlayer> playermap = new HashMap<ServerPlayerEntity, DungeonsAndDragonsPlayer>();
	ServerPlayerEntity player;
	PlayerType type;
	double[] position = { 0, 0, 0 };
	ArrayList<double[]> waypoints = new ArrayList<double[]>();
	double motionDistance = 0;
	String name;
	int[] color = {255, 255, 0};
	ShowMeasure mode;
	
	RenderMode drawMode;
	/**
	 * Radius for circle and cone drawing.
	 */
	double radius;

	/**
	 * Wrap a ServerPlayerEntity as a Dungeons and Dragons player
	 * 
	 * @param player ServerPlayerEntity that is being wrapped
	 */
	public DungeonsAndDragonsPlayer(ServerPlayerEntity player) {
		this(player, PlayerType.PLAYER);
	}

	/**
	 * Wrap a ServerPlayerEntity as a Dungeons and Dragons player with privilege
	 * level
	 * 
	 * @param player ServerPlayerEntity that is being wrapped
	 * @param type   Privilege Level
	 */
	public DungeonsAndDragonsPlayer(ServerPlayerEntity player, PlayerType type) {
		this.player = player;
		this.type = type;
		playermap.put(player, this);
		player.getAbilities().allowFlying = true;
		player.sendAbilitiesUpdate();
		name = player.getName().getString();
		mode = ShowMeasure.GM;
		drawMode = RenderMode.MEASURE;
		radius = 30 / DungeonsAndDragons.DISTANCE_SCALE;
	}

	/**
	 * Update token position to current player XYZ
	 */
	public void updatePosition() {
		position[0] = this.player.getX();
		position[1] = this.player.getY();
		position[2] = this.player.getZ();

		resetMeasurePoint();
	}

	/**
	 * Return to saved positon
	 */
	public void home() {
		player.teleport(position[0], position[1], position[2]);
		resetMeasurePoint();
	}

	/**
	 * Update player's measuring "home" location
	 * 
	 * @param preserveMotion Set to true to preserve distance already moved.
	 */
	public void updateMeasurePoint(boolean preserveMotion) {
		double d = 0D;
		if (preserveMotion) {
			d = measure();
		}
		waypoints.add(new double[] { player.getX(), player.getY(), player.getZ() });
		motionDistance = d;
	}

	/**
	 * Remove last measure point and measure from the previous one instead
	 */
	public void popMeasurePoint() {
		if(waypoints.size() <= 1) return;
		waypoints.remove(waypoints.size() - 1);
		motionDistance = 0D;
		for(int i = 1; i < waypoints.size(); i++) {
			motionDistance+=(Math.sqrt(Math.pow((waypoints.get(i)[0] - waypoints.get(i-1)[0]), 2)
					+ Math.pow((waypoints.get(i)[1] - waypoints.get(i-1)[1]), 2)
					+ Math.pow((waypoints.get(i)[2] - waypoints.get(i-1)[2]), 2)));
		}
	}

	/**
	 * Reset player measure point and motion distances to token position
	 */
	public void resetMeasurePoint() {
		motionDistance = 0;
		waypoints.clear();
		waypoints.add(getPosition());
	}
	
	/**
	 * Convenience function for getting distance to the last waypoint.
	 * @return Distance between player current position and the last waypoint in the array.
	 */
	public double distanceToLastWaypoint() {
		return Math.sqrt(Math.pow((waypoints.get(waypoints.size() - 1)[0] - player.getX()), 2)
				+ Math.pow((waypoints.get(waypoints.size() - 1)[1] - player.getY()), 2)
				+ Math.pow((waypoints.get(waypoints.size() - 1)[2] - player.getZ()), 2));
	}

	/**
	 * Account for existing motion distance and measure distance to current location
	 * 
	 * @return Measured distance in D&D feet
	 */
	public double measure() {
		if (waypoints.isEmpty()) {
			return 0D;
		}
		return motionDistance + (Math.sqrt(Math.pow((waypoints.get(waypoints.size() - 1)[0] - player.getX()), 2)
				+ Math.pow((waypoints.get(waypoints.size() - 1)[1] - player.getY()), 2)
				+ Math.pow((waypoints.get(waypoints.size() - 1)[2] - player.getZ()), 2)));
	}

	/**
	 * TODO test Update player action bar with distance
	 */
	public void updateActionBar() {
		String messageFormat = "Movement: %.1f ft (%.1f to previous waypoint)";
		double totalDistance = measure() * DungeonsAndDragons.DISTANCE_SCALE;
		double singleDistance = distanceToLastWaypoint() * DungeonsAndDragons.DISTANCE_SCALE;
		String message = String.format(messageFormat, totalDistance, singleDistance);
		this.player.sendMessage(MutableText.of(new LiteralTextContent(message)), true);
	}

	/**
	 * Invisibility hook
	 * 
	 * @param invisible Whether the player should be invisible
	 */
	public void invisible(boolean invisible) {
		player.setInvisible(invisible);
	}

	/**
	 * Give the player the GM privilege level
	 */
	public void makeGM() {
		this.type = PlayerType.GM;
		invisible(false);
		player.getAbilities().allowFlying = true;
		player.sendAbilitiesUpdate();
	}

	/**
	 * Give the player the Spectator privilege level
	 */
	public void makeSpectator() {
		this.type = PlayerType.SPECTATOR;
		invisible(true);
		player.getAbilities().allowFlying = true;
		player.sendAbilitiesUpdate();
		setMode(ShowMeasure.SELF);
	}

	/**
	 * Give the player the Player privilege level
	 */
	public void makePlayer() {
		this.type = PlayerType.PLAYER;
		invisible(false);
		player.getAbilities().allowFlying = true;
		player.sendAbilitiesUpdate();
	}
	
	/**
	 * Get privilege level of player
	 * @return PlayerType privilege level
	 */
	public PlayerType getType() {
		return type;
	}
	
	public ArrayList<double[]> getWaypoints(){
		return new ArrayList<double[]>(waypoints);
	}
	
	public double[] getPosition() {
		return Arrays.copyOf(position, position.length);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public int[] getColor() {
		return color;
	}
	
	public ServerPlayerEntity getEntity() {
		return player;
	}
	
	public ShowMeasure getMode() {
		return mode;
	}
	
	public void setColor(int[] color) {
		this.color = color;
	}
	
	public void setMode(ShowMeasure mode) {
		this.mode = mode;
	}

	/**
	 * @param newplayer Player to replace with
	 */
	public void replacePlayer(ServerPlayerEntity newplayer) {
		this.player = newplayer;
	}

	/**
	 * Called on player leave to safely remove the player from the game.
	 */
	public void destruct() {
		playermap.remove(this.player);
	}

	/**
	 * Finds the corresponding D&D player given a ServerPlayerEntity, returns null
	 * if not in game
	 * 
	 * @param player The player to find
	 */
	@Nullable
	public static DungeonsAndDragonsPlayer getDndPlayerFromServerPlayer(ServerPlayerEntity player) {
		return playermap.get(player);
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public @Nullable DungeonsAndDragonsPlayer getController() {
		return this;
	}
	
	@Override
	public void clean() {
		
	}
	
	/**
	 * Perform rendering for this dnd player.
	 * Draws its lines/waypoints/whatever we want to onto the world.
	 * @param renderer Renderer that performs the actual draw commands.
	 */
	public void doRender(DungeonsAndDragonsRenderer renderer) {
		double[] player_pos = new double[] {player.getX(), player.getY(), player.getZ()};
		switch(drawMode) {
		case MEASURE:
			double[] prev_point = waypoints.get(0);
			renderer.renderWaypoint(prev_point);
			for (int i = 1; i < waypoints.size(); ++i) {
				double[] point = waypoints.get(i);
				renderer.renderLine(prev_point, point, color);
				renderer.renderWaypoint(point);
				prev_point = point;
			}
			renderer.renderLine(prev_point, player_pos, color);
			break;
		case SPHERE:
			prev_point = waypoints.get(waypoints.size()-1);
			renderer.renderWaypoint(prev_point);
			renderer.renderCircle(prev_point, radius, color);
			renderer.renderSparseSphere(prev_point, radius, color);
			break;
		case CONE:
			final double CONE_HALF_ANGLE = Math.atan(1/2);
			player.getHeadYaw();
			break;
		}
	}
	
	/**
	 * Get the current drawing mode of the player.
	 * The player can either be in normal draw mode (measuring paths),
	 * sphere draw mode (sphere of determined radius from previous waypoint),
	 * or cone draw mode (cone from the player's position).
	 * @return
	 */
	public RenderMode getRenderMode() {
		return drawMode;
	}
	
	/**
	 * Set the player's drawing mode.
	 * @see DungeonsAndDragonsPlayer#getRenderMode()
	 * @param mode
	 */
	public void setRenderMode(RenderMode mode) {
		this.drawMode = mode;
	}
}
