
public class Entity 
{
	World world;
	Chunk chunk;
	Point4D pos = new Point4D(0,0,0,0);
	float yaw = 0;
	float pitch = 0;
	float wane = 0;
	short age = 0;
	byte falltime = 0;
	boolean onGround = false;
	
	public Entity() {}
}
