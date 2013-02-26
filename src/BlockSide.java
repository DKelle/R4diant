

public class BlockSide implements Comparable
{
	Block parent;
	byte value = -1;
	String id;
	//0-7: +x, -x, +y, -y, +z, -z, (+w, -w)
	
	/**
	 * Constructs a new block side object
	 * @param p - the block that is the parent of the side.
	 * @param v - the side number. 0 = +x, 1 = -x, 2 = +y, 3 = -y, 4 = +z, 5 = -z
	 */
	public BlockSide(Block p, byte v)
	{
		parent = p;
		value = v;
		
		id = getPosition().toString();
	}
	
	/**
	 * Gives the position of this side, including the offset.
	 * @return the position of the side, without the w part
	 */
	public Point4D getPosition()
	{
		return parent.getPosition().add(getOffset(false));
	}
	
	/**
	 * Compares two sides
	 * @param o - the other side to compare
	 * @return 1 if this side is closer to the player than the other, -1 if it is farther
	 */
	public int compareTo(Object o) 
	{
		if (!(o instanceof BlockSide))
			return 1;
		
		//player.pos.x -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.sin(player.yaw);
		//player.pos.y += player.movedamp * player.speed * delta * Math.sin(player.pitch);
		//player.pos.z -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.cos(player.yaw);
		
		//distance = -cos(pitch)*sin(yaw)*dx + sin(pitch)*dy + cos(pitch)*cos(yaw)*dz
		Point4D point = getPosition();
		Point4D other = ((BlockSide)o).getPosition();
		
		
		//double distance = -Math.cos(player.pitch)*Math.sin(player.yaw)*(point.x - player.pos.x) + Math.sin(player.pitch)*(point.y - player.pos.y) + Math.cos(player.pitch)*Math.cos(player.yaw)*(point.z - player.pos.z)						
		Player player = parent.chunk.world.player; //reference
		
		double dista = -Math.cos(player.pitch)*Math.sin(player.yaw)*(point.x - player.pos.x) + Math.sin(player.pitch)*(point.y - player.pos.y) - Math.cos(player.pitch)*Math.cos(player.yaw)*(point.z - player.pos.z);						
		double distb = -Math.cos(player.pitch)*Math.sin(player.yaw)*(other.x - player.pos.x) + Math.sin(player.pitch)*(other.y - player.pos.y) - Math.cos(player.pitch)*Math.cos(player.yaw)*(other.z - player.pos.z);						

		//double dista = parent.getPosition().add(getOffset()).dist(new Point4D(playerpos.x, playerpos.y, playerpos.z, 0));
		//double distb = ((BlockSide)o).parent.getPosition().add(getOffset()).dist(new Point4D(playerpos.x, playerpos.y, playerpos.z, 0));
		
		//this large number makes the distinction higher
		//the negative sorts in reverse order
		return (int)(-1000*1000*1000*(dista - distb));
		
	}
	
	/**
	 * Gives the offset from the position given by getPosition()
	 * @param includeW - Whether the w part of the offset should be included. If false, w will be 0.
	 * @return the offset from the position of this face.
	 */
	public Point4D getOffset(boolean includeW)
	{
		Point4D res = new Point4D(0,0,0,0);
		
		if (value == 0)
			res.x = 0.5;
		else if (value == 1)
			res.x = -0.5;
		else if (value == 2)
			res.y = 0.5;
		else if (value == 3)
			res.y = -0.5;
		else if (value == 4)
			res.z = 0.5;
		else if (value == 5)
			res.z = -0.5;
		
		if (!includeW)
			//this added to make sure that the sides still render in the right place. It's messy, but necessary.
			res.w = -parent.getPosition().w;
		
		return res;
	}
	
	/**
	 * Compares this face with another face.
	 * @param other - The point you're referring to
	 * @return 1 if the point is in front, -1 if behind, 0 if on
	 */
	public int comparePlane(BlockSide other)
	{
		if (value == 0)
		{
			if (getPosition().x == other.getPosition().x) return 0;
			if (getPosition().x > other.getPosition().x) return -1;
			if (getPosition().x < other.getPosition().x) return 1;
			//parallel to y and z, postive x
		}
		if (value == 1)
		{
			if (getPosition().x == other.getPosition().x) return 0;
			if (getPosition().x > other.getPosition().x) return 1;
			if (getPosition().x < other.getPosition().x) return -1;
			//parallel to y and z, negative x
		}
		if (value == 2)
		{
			if (getPosition().y == other.getPosition().y) return 0;
			if (getPosition().y > other.getPosition().y) return -1;
			if (getPosition().y < other.getPosition().y) return 1;
			//parallel to x and z, positive y
		}
		if (value == 3)
		{
			if (getPosition().y == other.getPosition().y) return 0;
			if (getPosition().y > other.getPosition().y) return 1;
			if (getPosition().y < other.getPosition().y) return -1;
			//parallel to x and z, negative y
		}
		if (value == 4)
		{
			if (getPosition().z == other.getPosition().z) return 0;
			if (getPosition().z > other.getPosition().z) return -1;
			if (getPosition().z < other.getPosition().z) return 1;
			//parallel to x and y, positive z
		}
		if (value == 5)
		{
			if (getPosition().z == other.getPosition().z) return 0;
			if (getPosition().z > other.getPosition().z) return 1;
			if (getPosition().z < other.getPosition().z) return -1;
			//parallel to x and y, negative z
		}
		return 0;
	}
	
	/**
	 * Compares this face with a point
	 * @param other - The point you're referring to
	 * @return 1 if the point is in front, -1 if behind, 0 if on
	 */
	public int comparePlane(Point4D other)
	{
		if (value == 0)
		{
			if (getPosition().x == other.x) return 0;
			if (getPosition().x > other.x) return -1;
			if (getPosition().x < other.x) return 1;
			//parallel to y and z, positive x
		}
		if (value == 1)
		{
			if (getPosition().x == other.x) return 0;
			if (getPosition().x > other.x) return 1;
			if (getPosition().x < other.x) return -1;
			//parallel to y and z, negative x
		}
		if (value == 2)
		{
			if (getPosition().y == other.y) return 0;
			if (getPosition().y > other.y) return -1;
			if (getPosition().y < other.y) return 1;
			//parallel to x and z, positive y
		}
		if (value == 3)
		{
			if (getPosition().y == other.y) return 0;
			if (getPosition().y > other.y) return 1;
			if (getPosition().y < other.y) return -1;
			//parallel to x and z, negative y
		}
		if (value == 4)
		{
			if (getPosition().z == other.z) return 0;
			if (getPosition().z > other.z) return -1;
			if (getPosition().z < other.z) return 1;
			//parallel to x and y, positive z
		}
		if (value == 5)
		{
			if (getPosition().z == other.z) return 0;
			if (getPosition().z > other.z) return 1;
			if (getPosition().z < other.z) return -1;
			//parallel to x and y, negative z
		}
		return 0;
	}
}