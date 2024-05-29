package huskabyte.dnd.mixin;

import java.util.ArrayList;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import huskabyte.dnd.player.DungeonsAndDragonsPlayer;
import huskabyte.dnd.player.PlayerType;
import huskabyte.dnd.player.ShowMeasure;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

	/*
	 * Update player HUD every player tick
	 * 
	 * TODO Send client particles every tick 
	 * TODO Persist free flight on gamemode change (may require other mixin)
	 */
	@Inject(at = @At("HEAD"), method = "playerTick()V")
	private void updateMeasureOnTick(CallbackInfo info) {
		// Apparently standard practice to intermediate cast to object when trying to
		// use this from a mixin.
		DungeonsAndDragonsPlayer player = DungeonsAndDragonsPlayer
				.getDndPlayerFromServerPlayer((ServerPlayerEntity) (Object) this);
		player.updateActionBar();
		ArrayList<ServerPlayerEntity> viewers;
		if(player.getMode() != ShowMeasure.OFF) {
				viewers = new ArrayList<ServerPlayerEntity>();
				for(DungeonsAndDragonsPlayer i : DungeonsAndDragonsPlayer.playermap.values()) {
					if((i.getType() == PlayerType.GM || i.getType() == PlayerType.SPECTATOR || player.getMode() == ShowMeasure.ALL) 
							&& player.getMode() != ShowMeasure.SELF 
							&& !player.getEntity().equals((ServerPlayerEntity)(Object)this)) {
						viewers.add(i.getEntity());
					}
				}
				viewers.add((ServerPlayerEntity) (Object) this);
			
			ArrayList<double[]> waypoints = player.getWaypoints();
			waypoints.add(0, player.getPosition());
			waypoints.add(new double[] { ((ServerPlayerEntity) (Object) this).getX(),
					((ServerPlayerEntity) (Object) this).getY(), ((ServerPlayerEntity) (Object) this).getZ() });
			for (int i = 1; i < waypoints.size(); i++) {
				double dist = Math.sqrt(Math.pow((waypoints.get(i - 1)[0] - waypoints.get(i)[0]), 2)
						+ Math.pow((waypoints.get(i - 1)[1] - waypoints.get(i)[1]), 2)
						+ Math.pow((waypoints.get(i - 1)[2] - waypoints.get(i)[2]), 2));
				
				for (int j = 0; j < dist; j++) {
					for(ServerPlayerEntity viewer : viewers) {
						((ServerPlayerEntity) (Object) this).getServerWorld().spawnParticles(viewer,
								new DustParticleEffect(new Vector3f(((float)player.getColor()[0])/255F, 
										((float)player.getColor()[1])/255F, 
										((float)player.getColor()[2])/255F), 
										1.0f), true,
								waypoints.get(i)[0] + (waypoints.get(i-1)[0] - waypoints.get(i)[0]) / dist * j,
								waypoints.get(i)[1] + (waypoints.get(i-1)[1] - waypoints.get(i)[1]) / dist * j,
								waypoints.get(i)[2] + (waypoints.get(i-1)[2] - waypoints.get(i)[2]) / dist * j,
								1, 0, 0, 0, 0);
					}
				}
			}
		}
	}
}