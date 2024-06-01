package huskabyte.dnd.mixin;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import huskabyte.dnd.player.DungeonsAndDragonsPlayer;
import huskabyte.dnd.player.DungeonsAndDragonsRenderer;
import huskabyte.dnd.player.PlayerType;
import huskabyte.dnd.player.ShowMeasure;
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
		ServerPlayerEntity thiz = (ServerPlayerEntity) (Object) this;
		DungeonsAndDragonsPlayer player = DungeonsAndDragonsPlayer
				.getDndPlayerFromServerPlayer(thiz);
		player.updateActionBar();
		ArrayList<ServerPlayerEntity> viewers;
		if(player.getMode() != ShowMeasure.OFF) {
			viewers = new ArrayList<ServerPlayerEntity>();
			for(DungeonsAndDragonsPlayer i : DungeonsAndDragonsPlayer.playermap.values()) {
				if((i.getType() == PlayerType.GM || i.getType() == PlayerType.SPECTATOR || player.getMode() == ShowMeasure.ALL) 
						&& player.getMode() != ShowMeasure.SELF 
						&& !i.getEntity().equals((ServerPlayerEntity)(Object)this)) {
					viewers.add(i.getEntity());
				}
			}
			viewers.add(thiz);
			
			player.doRender(new DungeonsAndDragonsRenderer(viewers, thiz.getServerWorld()));
		}
	}
}