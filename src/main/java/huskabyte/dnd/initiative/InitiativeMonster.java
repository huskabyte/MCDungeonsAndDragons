package huskabyte.dnd.initiative;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import huskabyte.dnd.player.DungeonsAndDragonsPlayer;

public class InitiativeMonster implements InitiativeMember{
	
	private static final ArrayList<InitiativeMonster> monsterlist = new ArrayList<InitiativeMonster>();
	
	private String name;
	@Nullable
	private DungeonsAndDragonsPlayer controller;
	
	public InitiativeMonster(String name) {
		this(name, null);
	}
	
	public InitiativeMonster(String name, DungeonsAndDragonsPlayer controller) {
		this.name = name;
		monsterlist.add(this);
		this.controller = controller;
	}

	@Override
	public boolean isPlayer() {
		return false;
	}

	@Override
	public @Nullable DungeonsAndDragonsPlayer getController() {
		return controller;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void clean() {
		monsterlist.remove(this);
	}

}
