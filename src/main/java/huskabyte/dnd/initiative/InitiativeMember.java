package huskabyte.dnd.initiative;

import org.jetbrains.annotations.Nullable;

import huskabyte.dnd.player.DungeonsAndDragonsPlayer;

public interface InitiativeMember {
	
	/**
	 * Check whether the initiative member is a player character
	 * controlled by a player on the server.
	 * @return If the enclosed type is a player
	 */
	public boolean isPlayer();
	
	/**
	 * Returns the controlling player if they are in the game, and
	 * null if the object is GM-controlled
	 * @return Controlling player
	 */
	@Nullable
	public DungeonsAndDragonsPlayer getController();
	
	public String getName();
	
	default void add(int init, int dex) {
		InitiativeTracker.add(this, init, dex);
	}
	
	default void remove() {
		InitiativeTracker.remove(this);
	}
	
	public void clean();
}
