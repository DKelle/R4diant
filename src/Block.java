import java.util.ArrayList;


public class Block implements Comparable
{
	Chunk chunk;
	/** The position, (512*w + 64*z + 8*y + x) */
	short pos;
	/** Will be the temperature */
	short temp;
	//default for existing
	//by the way, id's now start at 1. 0 is air, or "nothing", if you will (no block)
	short id = 1;
	/** The sides. 1 if obstructed, 0 if open */
	byte sides;
	byte light; //If I need it?
	ArrayList<Byte> elements;
	ArrayList<Ore> ores;
	
	//final constants which only exist in code
	//final String name;
	//final short lowStateChange;
	//final short highStateChange;
	//final short lowState;
	//final short highState;
	//final byte density;
	//final byte lightStrength;
	
	
	/**
	 * Constructs a blank block. Probably shouldn't use this one.
	 */
	public Block()
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
	}
	
	/**
	 * Constructs a block in a certain chunk and position in the chunk
	 * @param ch - the chunk
	 * @param position - the position, (512*w + 64*z + 8*y + x)
	 */
	public Block(Chunk ch, short position)
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
		chunk = ch;
		pos = position;
	}
	
	/**
	 * This is not the best constructor, to be honest.
	 */
	public Block(Chunk ch, Point4D p)
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
		chunk = ch;
		pos = (short)( ((short)p.x) | ((short)p.y << 3) | ((short)p.z << 6) | ((short)p.w << 9) );
	}
	
	/**
	 * Checks the sides around the block and sets the sides variable to the value.
	 * Sides are marked as 1 if there's something in the way and 0 otherwise.
	 */
	public void checkSides()
	{
		//check the sides
		//sides are marked as 1 if there's something in the way, zero otherwise.
		sides = 0;
		int x = pos & 7;
		int y = (pos >> 3) & 7;
		int z = (pos >> 6) & 7;
		int w = (pos >> 9) & 7;
		
		if (x == 7)
		{
			if (chunk.ix+1 >= chunk.world.loaddistance)
			{
				//sides += 1;
			}
			else
			{
				//check other chunk (x+1)
				Chunk c = chunk.world.loaded[chunk.ix+1][chunk.iy][chunk.iz][chunk.iw];
				if (c != null)
				{
					if (c.data[0][y][z][w] != null)
					{
						sides += 1;
					}
				}
			}
		}
		else
		{
			if (chunk.data[x+1][y][z][w] != null)
			{
				sides += 1;
			}
		}
		
		if (x == 0)
		{
			if (chunk.ix-1 < 0)
			{
				//sides += 2;
			}
			else
			{
				//check other chunk (x-1)
				Chunk c = chunk.world.loaded[chunk.ix-1][chunk.iy][chunk.iz][chunk.iw];
				if (c != null)
				{
					if (c.data[7][y][z][w] != null)
					{
						sides += 2;
					}
				}
			}
		}
		else
		{
			if (chunk.data[x-1][y][z][w] != null)
			{
				sides += 2;
			}
		}
		
		if (y == 7)
		{
			if (chunk.iy+1 >= chunk.world.loaddistance)
			{
				//sides += 4;
			}
			else
			{
				//check other chunk (y+1)
				Chunk c = chunk.world.loaded[chunk.ix][chunk.iy+1][chunk.iz][chunk.iw];			
				if (c != null)
				{
					if (c.data[x][0][z][w] != null)
					{
						sides += 4;
					}
				}
			}
		}
		else
		{
			if (chunk.data[x][y+1][z][w] != null)
			{
				sides += 4;
			}
		}
		
		if (y == 0)
		{
			if (chunk.iy-1 < 0)
			{
				//sides += 8;
			}
			else
			{
				//check other chunk (y-1)
				Chunk c = chunk.world.loaded[chunk.ix][chunk.iy-1][chunk.iz][chunk.iw];
				if (c != null)
				{
					if (c.data[x][7][z][w] != null)
					{
						sides += 8;
					}
				}
			}
		}
		else
		{
			if (chunk.data[x][y-1][z][w] != null)
			{
				sides += 8;
			}
		}
		
		if (z == 7)
		{
			if (chunk.iz+1 >= chunk.world.loaddistance)
			{
				//sides += 16;
			}
			else
			{
				//check other chunk (z+1)
				Chunk c = chunk.world.loaded[chunk.ix][chunk.iy][chunk.iz+1][chunk.iw];
				if (c != null)
				{
					if (c.data[x][y][0][w] != null)
					{
						sides += 16;
					}
				}
			}
		}
		else
		{
			if (chunk.data[x][y][z+1][w] != null)
			{
				sides += 16;
			}
		}
		
		if (z == 0)
		{
			if (chunk.iz-1 < 0)
			{
				//sides += 32;
			}
			else
			{
				//check other chunk (z-1)
				Chunk c = chunk.world.loaded[chunk.ix][chunk.iy][chunk.iz-1][chunk.iw];
				if (c != null)
				{
					if (c.data[x][y][7][w] != null)
					{
						sides += 32;
					}
				}
			}
		}
		else
		{
			if (chunk.data[x][y][z-1][w] != null)
			{
				sides += 32;
			}
		}
		
		if (w == 7)
		{
			if (chunk.iw+1 >= chunk.world.loaddistance)
			{
				//sides += 64;
			}
			else
			{
				//check other chunk (w+1)
				Chunk c = chunk.world.loaded[chunk.ix][chunk.iy][chunk.iz][chunk.iw+1];
				if (c != null)
				{
					if (c.data[x][y][z][0] != null)
					{
						sides += 64;
					}
				}
			}
		}
		else
		{
			if (chunk.data[x][y][z][w+1] != null)
			{
				sides += 64;
			}
		}
		
		if (w == 0)
		{
			if (chunk.iw-1 < 0)
			{
				//sides += 128;
			}
			else
			{
				//check other chunk (w-1)
				Chunk c = chunk.world.loaded[chunk.ix][chunk.iy][chunk.iz][chunk.iw-1];
				if (c != null)
				{
					if (c.data[x][y][z][7] != null)
					{
						sides += 128;
					}
				}
			}
		}
		else
		{
			if (chunk.data[x][y][z][w-1] != null)
			{
				sides += 128;
			}
		}
		
	}
	
	/**
	 * Does nothing yet.
	 */
	public void getLight()
	{
		//calculate lighting (if I need it?)
	}
	
	/**
	 * Does nothing yet.
	 * @param ore - the ore to add
	 */
	public void addOre(Ore ore)
	{
		ores.add(ore);
	}
	
	//final access methods
	public String getName()
	{
		return "Block";
	}
	
	public short lowStateChange()
	{
		return 0;
	}
	
	public short highStateChange()
	{
		return (short)1000000;
	}
	
	public short lowState()
	{
		return 0;
	}
	
	public short highState()
	{
		return 0;
	}
	
	public byte density()
	{
		return 0;
	}
	
	public byte lightStrength()
	{
		return 0;
	}
	
	/**
	 * Compares by distance to the player
	 */
	public int compareTo(Object o) 
	{
		//sort by xyzw position
		//return pos;
		
		if (!(o instanceof Block))
			return 0;
		
		//sort by distance to player
		//negative for descending order (to render farthest things first)
		//*1000*1000 so that finer details can be there. Might need more.
		
		return (int)(-1000*1000*(getPlayerDistance()-((Block)o).getPlayerDistance()));
	}
	
	/**
	 * Gets the absolute position
	 * @return the absolute position
	 */
	public Point4D getPosition()
	{
		Point4D res = new Point4D(chunk.x*8+(pos & 7), chunk.y*8+((pos >> 3) & 7), chunk.z*8+((pos >> 6) & 7), chunk.w*8+((pos >> 9) & 7) );
		//System.out.println(res);
		return res;
	}
	
	/**
	 * Gets the distance to the player
	 * @return the distance to the player
	 */
	public double getPlayerDistance()
	{
		return getPosition().dist(chunk.world.player.pos);
	}
	
	/**
	 * Gets the texture coordinates based on the id
	 * @param side - the side of the block to use
	 * @return the texture coordinates
	 */
	public float[] getTextureCoordinates(byte side)
	{
		if (side >= 0 && side <= 5)
		{
			//potentially things for different sides go here
			//assuming for now that the texture map is 16x16 ******
			
			float[] res = new float[8];
			res[0] = (id%16)/16.0f;
			res[1] = (id/16)/16.0f;
			res[2] = (id%16)/16.0f;
			res[3] = ((id/16) + 1)/16.0f;
			res[4] = ((id%16) + 1)/16.0f;
			res[5] = ((id/16) + 1)/16.0f;
			res[6] = ((id%16) + 1)/16.0f;
			res[7] = (id/16)/16.0f;
			return res;
		}
		return null;
	}
}
