package huskabyte.dnd.player;

import java.util.List;
import java.util.Random;

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
	/**
	 * Distance between points on lines/circles.
	 */
	public static double LINE_SPACING = 1;

	/**
	 * Area density of points on rendered spheres.
	 */
	public static double SPHERE_DENSITY = 0.01;

	private List<ServerPlayerEntity> viewers;
	private ServerWorld world;
	private Random random;

	/**
	 * Construct a renderer with the given list of players to render to.
	 * @param viewers List of players that should receive the render results.
	 * @param world World to render objects into
	 */
	public DungeonsAndDragonsRenderer(List<ServerPlayerEntity> viewers, ServerWorld world) {
		this.viewers = viewers;
		this.world = world;
		this.random = new Random();
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
	 * @param color 3-vector of integers (0-255)
	 */
	public void renderLine(double[] start, double[] end, int[] color) {
		double dist = Math.sqrt(Math.pow(start[0] - end[0], 2)
				+ Math.pow(start[1] - end[1], 2)
				+ Math.pow(start[2] - end[2], 2));
		DustParticleEffect particle = new DustParticleEffect(new Vector3f(color[0]/255F, color[1]/255F, color[2]/255F), 1.0f);
		for (double j = 0; j < dist; j += LINE_SPACING) {
			for(ServerPlayerEntity viewer : viewers) {
				world.spawnParticles(viewer,
						particle, true,
						end[0] + (start[0] - end[0]) / dist * j,
						end[1] + (start[1] - end[1]) / dist * j,
						end[2] + (start[2] - end[2]) / dist * j,
						1, 0, 0, 0, 0);
			}
		}
	}
	
	/**
	 * Render a circle from a center and radius.
	 * @param center
	 * @param radius
	 * @param color
	 */
	public void renderCircle(double[] center, double radius, int[] color) {
		renderArc(center, radius, 0, 2*Math.PI, color);
	}

	/**
	 * Render an arc from a center and radius between two angles.
	 * @param center
	 * @param radius
	 * @param angle1 Start angle, in radians. Must be less than angle2.
	 * @param angle2 End angle, in radians. Must be greater than angle1.
	 * @param color
	 */
	public void renderArc(double[] center, double radius,
						  double angle1, double angle2, int[] color) {
		DustParticleEffect particle = new DustParticleEffect(new Vector3f(color[0]/255F, color[1]/255F, color[2]/255F), 1.0f);
		for (double angle = angle1; angle < angle2; angle += LINE_SPACING / radius) {
			for(ServerPlayerEntity viewer : viewers) {
				world.spawnParticles(viewer,
						particle, true,
						center[0] + radius * Math.cos(angle),
						center[1],
						center[2] + radius * Math.sin(angle),
						1, 0, 0, 0, 0);
			}
		}
	}
	
	/**
	 * Render a sphere from a center and radius.
	 * Uses sampling -- end result is very sparse.
	 * @param center
	 * @param radius
	 * @param color
	 */
	public void renderSparseSphere(double[] center, double radius, int[] color) {
		double area = 4*Math.PI*radius*radius;
		
		DustParticleEffect particle = new DustParticleEffect(new Vector3f(color[0]/255F, color[1]/255F, color[2]/255F), 1.0f);
		for (int i = 0; i < area*SPHERE_DENSITY; ++i) {
			// Sample a random point on the unit 3D sphere uniformly.
			// https://stats.stackexchange.com/questions/7977/how-to-generate-uniformly-distributed-points-on-the-surface-of-the-3-d-unit-sphe
			double z = 2 * random.nextDouble() - 1;
			double theta = 2*Math.PI * random.nextDouble();
			double horizontal = Math.sqrt(1 - z*z);
			double x = horizontal * Math.cos(theta);
			double y = horizontal * Math.sin(theta);
					
			for(ServerPlayerEntity viewer : viewers) {
				world.spawnParticles(viewer,
						particle, false,
						center[0] + radius * x,
						center[1] + radius * y,
						center[2] + radius * z,
						1, 0, 0, 0, 0);
			}
		}
	}
}