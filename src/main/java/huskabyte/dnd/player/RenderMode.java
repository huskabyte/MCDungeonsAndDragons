package huskabyte.dnd.player;

public enum RenderMode {
	MEASURE(0),
	SPHERE(1),
	CONE(2);

	private int id;
	private RenderMode(int id) {
		this.id = id;
	}
	public RenderMode next() {
		return RenderMode.values()[(this.id + 1) % RenderMode.values().length];
	}
}
