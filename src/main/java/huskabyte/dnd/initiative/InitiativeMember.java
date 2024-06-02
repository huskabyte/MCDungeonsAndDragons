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
	
	/**
	 * Get name to display in initiative order
	 * @return Initiative name
	 */
	public String getName();
	
	default void add(int init, int dex) {
		InitiativeTracker.add(this, init, dex);
	}
	
	/**
	 * Remove self from initiative tracker
	 */
	default void remove() {
		InitiativeTracker.remove(this);
	}
	
	/**
	 * Destruct self
	 */
	public void clean();
}
