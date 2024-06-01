package huskabyte.dnd.player;

import java.util.List;

import org.joml.Vector3f;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Class exposing render functions for drawing objects used by DungeonsAndDragonsPlayer.
 * 
 * Ex. draws the waypoints, lines, circle, cone, etc.
 */
public class DungeonsAndDragonsRenderer {
	private List<ServerPlayerEntity> viewers;
	private ServerWorld world;

	/**
	 * Construct a renderer with the given list of players to render to.
	 * @param viewers List of players that should receive the render results.
	 * @param world World to render objects into
	 */
	public DungeonsAndDragonsRenderer(List<ServerPlayerEntity> viewers, ServerWorld world) {
		this.viewers = viewers;
		this.world = world;
	}
	
	/**
	 * Render a waypoint.
	 * @param waypoint Triplet (x, y, z), location to render waypoint at.
	 */
	public void renderWaypoint(double[] waypoint) {
		for(ServerPlayerEntity viewer : viewers) {
			world.spawnParticles(viewer,
					ParticleTypes.END_ROD, 
					true,
					waypoint[0],
					waypoint[1],
					waypoint[2],
					3, 0, 1, 0, 0);
		}
	}
	
	/**
	 * Render a line between two points, given as triples (x, y, z).
	 * The line is a dotted line with one point on the end point.
	 * @param start Starting point.
	 * @param end End point.
	 */
	public void renderLine(double[] start, double[] end, int[] color) {
		double dist = Math.sqrt(Math.pow(start[0] - end[0], 2)
				+ Math.pow(start[1] - end[1], 2)
				+ Math.pow(start[2] - end[2], 2));
		
		for (int j = 0; j < dist; j++) {
			for(ServerPlayerEntity viewer : viewers) {
				world.spawnParticles(viewer,
						new DustParticleEffect(new Vector3f(color[0]/255F, color[1]/255F, color[2]/255F), 1.0f), true,
						end[0] + (start[0] - end[0]) / dist * j,
						end[1] + (start[1] - end[1]) / dist * j,
						end[2] + (start[2] - end[2]) / dist * j,
						1, 0, 0, 0, 0);
			}
		}
	}
}