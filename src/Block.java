import java.util.ArrayList;
import java.util.Collections;


public class Block implements Comparable
{
	Chunk chunk;
	short pos;
	short temp;
	short id;
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
	
	
	
	public Block()
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
	}
	
	public Block(Chunk ch, short position)
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
		chunk = ch;
		pos = position;
	}
	
	public Block(Chunk ch, Point4D p)
	{
		elements = new ArrayList<Byte>();
		ores = new ArrayList<Ore>();
		chunk = ch;
		pos = (short)( ((short)p.x) | ((short)p.y << 3) | ((short)p.z << 6) | ((short)p.w << 9) );
	}
	
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
			//check other chunk (x+1)
			Chunk c = chunk.world.getChunk(chunk.x+1, chunk.y, chunk.z, chunk.w);
			if (c != null)
			{
				if (c.data[0][y][z][w] != null)
				{
					sides += 1;
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
			//check other chunk (x-1)
			Chunk c = chunk.world.getChunk(chunk.x-1, chunk.y, chunk.z, chunk.w);
			if (c != null)
			{
				if (c.data[7][y][z][w] != null)
				{
					sides += 2;
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
			//check other chunk (y+1)
			Chunk c = chunk.world.getChunk(chunk.x, chunk.y+1, chunk.z, chunk.w);
			if (c != null)
			{
				if (c.data[x][0][z][w] != null)
				{
					sides += 4;
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
			//check other chunk (y-1)
			Chunk c = chunk.world.getChunk(chunk.x, chunk.y-1, chunk.z, chunk.w);
			if (c != null)
			{
				if (c.data[x][7][z][w] != null)
				{
					sides += 8;
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
			//check other chunk (z+1)
			Chunk c = chunk.world.getChunk(chunk.x, chunk.y, chunk.z+1, chunk.w);
			if (c != null)
			{
				if (c.data[x][y][0][w] != null)
				{
					sides += 16;
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
			//check other chunk (z-1)
			Chunk c = chunk.world.getChunk(chunk.x, chunk.y, chunk.z-1, chunk.w);
			if (c != null)
			{
				if (c.data[x][y][7][w] != null)
				{
					sides += 32;
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
			//check other chunk (w+1)
			Chunk c = chunk.world.getChunk(chunk.x, chunk.y, chunk.z, chunk.w+1);
			if (c != null)
			{
				if (c.data[x][y][z][0] != null)
				{
					sides += 64;
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
			//check other chunk (w-1)
			Chunk c = chunk.world.getChunk(chunk.x, chunk.y, chunk.z, chunk.w-1);
			if (c != null)
			{
				if (c.data[x][y][z][7] != null)
				{
					sides += 128;
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
	
	public void getLight()
	{
		//calculate lighting (if I need it?)
	}
	
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
	
	public Point4D getPosition()
	{
		Point4D res = new Point4D(chunk.x*8+(pos & 7), chunk.y*8+((pos >> 3) & 7), chunk.z*8+((pos >> 6) & 7), chunk.w*8+((pos >> 9) & 7) );
		//System.out.println(res);
		return res;
	}
	
	public double getPlayerDistance()
	{
		return getPosition().dist(chunk.world.player.pos);
	}
	
	public byte[] getSideRenderOrder()
	{
		//based on farthest sides from the player
		ArrayList<Double> temp = new ArrayList<Double>();
		
		Point4D p = getPosition();
		temp.add( p.add(new Point4D(0.5,0,0,0)) .dist(chunk.world.player.pos));
		temp.add( p.add(new Point4D(-0.5,0,0,0)) .dist(chunk.world.player.pos));
		temp.add( p.add(new Point4D(0,0.5,0,0)) .dist(chunk.world.player.pos));
		temp.add( p.add(new Point4D(0,-0.5,0,0)) .dist(chunk.world.player.pos));
		temp.add( p.add(new Point4D(0,0,0.5,0)) .dist(chunk.world.player.pos));
		temp.add( p.add(new Point4D(0,0,-0.5,0)) .dist(chunk.world.player.pos));
		
		byte[] res = new byte[6];
		for (int i = 0; i < 6; i++)
		{
			res[i] = (byte)temp.indexOf(Collections.max(temp));
			temp.set(res[i], -1.0);
		}
		
		return res;
	}

}
