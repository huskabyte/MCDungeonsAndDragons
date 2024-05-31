package huskabyte.dnd.initiative;

import java.util.ArrayList;
import java.util.HashMap;

import huskabyte.dnd.player.DungeonsAndDragonsPlayer;
import huskabyte.dnd.player.PlayerType;
import huskabyte.dnd.player.ShowMeasure;

public class InitiativeTracker {
	static boolean ACTIVE = false;
	private static final ArrayList<InitiativeMember> ORDER = new ArrayList<InitiativeMember>();
	private static final HashMap<InitiativeMember, int[]> TEMP_PROPERTIES = new HashMap<InitiativeMember, int[]>();
	private static int turn = 0;
	
	private static final int INIT = 0;
	private static final int DEX = 1;
	
	/**
	 * Get whether initiative is active
	 * @return Whether initiative is active
	 */
	public static boolean getActive() {
		return ACTIVE;
	}
	
	/**
	 * Check whether it is member's turn
	 * 
	 * @param member Member to check for current turn
	 * @return Whether it is member's turn
	 */
	public static boolean turn(InitiativeMember member) {
		return ORDER.get(turn).equals(member);
	}
	
	/**
	 * Get a deep copy of the initiative order
	 * @return deepcopy of private order list
	 */
	public ArrayList<InitiativeMember> getOrder(){
		return new ArrayList<InitiativeMember>(ORDER);
	}
	
	/**
	 * Add a new member and set the turn counter to 0
	 * 
	 * @param member InitiativeMember to add
	 * @param init Initiative count
	 * @param dex Dex mod (for tiebreakers)
	 */
	public static void add(InitiativeMember member, int init, int dex) {
		add(member, init, dex, 0);
	}
	
	/**
	 * Add a new member and set the turn counter
	 * 
	 * @param member InitiativeMember to add
	 * @param init Initiative count
	 * @param dex Dex mod (for tiebreakers)
	 * @param turn New turn position
	 */
	public static void add(InitiativeMember member, int init, int dex, int turn) {
		if(ORDER.contains(member))return;
		TEMP_PROPERTIES.put(member, new int[] {init, dex});
		for(int i = 0; i < ORDER.size(); i++) {
			if(init > TEMP_PROPERTIES.get(ORDER.get(i))[INIT]) {
				ORDER.add(i, member);
				break;
			}
			if(init == TEMP_PROPERTIES.get(ORDER.get(i))[INIT]) {
				if( dex > TEMP_PROPERTIES.get(ORDER.get(i))[DEX]) {
					ORDER.add(i, member);
					break;
				}
				if(i == ORDER.size() - 1 || TEMP_PROPERTIES.get(ORDER.get(i+1))[INIT] > init) {
					ORDER.add(i+1, member);
					break;
				}
			}
		}
		setTurn(turn);
	}
	
	/**
	 * Safely remove a member from the initiative order
	 * 
	 * @param member InitiativeMember to remove from the list
	 */
	public static void remove(InitiativeMember member) {
		int index = ORDER.indexOf(member);
		if(ORDER.remove(member)) {
			if(index < turn) {
				setTurn(turn - 1);
			}else {
				InitiativeTracker.updateLineVisibility(ORDER.get(turn));
			}
		}
		TEMP_PROPERTIES.remove(member);
	}
	
	/**
	 * Reroll an initiative and set turn counter to 0
	 * 
	 * @param member InitiativeMember to reroll for
	 * @param init Initiative count
	 * @param dex Dex mod (for tiebreakers)
	 */
	public static void replace(InitiativeMember member, int init, int dex) {
		replace(member, init, dex, 0);
	}
	
	/**
	 * Reroll an initiative and set turn counter
	 * 
	 * @param member InitiativeMember to reroll for
	 * @param init Initiative count
	 * @param dex Dex mod (for tiebreakers)
	 * @param turn New turn position
	 */
	public static void replace(InitiativeMember member, int init, int dex, int turn) {
		remove(member);
		add(member, init, dex, turn);
	}
	
	/**
	 * Activate initiative
	 */
	public static void start() {
		clear();
		toggleActive(true);
	}
	
	/**
	 * Deactivate Initiative
	 */
	public void end() {
		clear();
		toggleActive(false);
	}
	
	/**
	 * Toggle initiative
	 */
	public static void toggleActive() {
		toggleActive(!ACTIVE);
	}
	
	/**
	 * Safely set the turn
	 * 
	 * @param turn turn counter to move to
	 */
	public static void setTurn(int turn) {
		InitiativeTracker.turn = turn;
		for(InitiativeMember i : ORDER) {
			InitiativeTracker.updateLineVisibility(i);
		}
	}
	
	/**
	 * Safely clean the initiative for new values
	 */
	public static void clear() {
		ORDER.clear();
		TEMP_PROPERTIES.clear();
		setTurn(0);
	}
	
	/**
	 * Set initiative to active or inactive
	 * 
	 * @param active TRUE = active
	 */
	private static void toggleActive(boolean active) {
		ACTIVE = active;
		if(active) {
			for(InitiativeMember i : ORDER) {
				InitiativeTracker.updateLineVisibility(i);
			}
		}
	}
	
	/**
	 * Update line visibility for an InitiativeMember
	 * 
	 * @param member
	 */
	private static void updateLineVisibility(InitiativeMember member) {
		if(!ACTIVE || member.getController() == null || member.getController().getType() != PlayerType.PLAYER) return;
		DungeonsAndDragonsPlayer dnd = member.getController();
		if(dnd.getMode() == ShowMeasure.OFF) return;
		
		if(dnd.getType() == PlayerType.SPECTATOR) {
			dnd.setMode(ShowMeasure.SELF);
			return;
		}
		if(InitiativeTracker.getActive() && InitiativeTracker.turn(dnd) && dnd.getType() == PlayerType.PLAYER) {
			dnd.setMode(ShowMeasure.ALL);
			return;
		}
		dnd.setMode(ShowMeasure.GM);
	}
}
