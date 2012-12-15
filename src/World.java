import java.util.ArrayList;
import java.util.Collections;


public class World 
{
	Runner parent;
	ArrayList<Chunk> loaded;
	ChunkLoader cl;
	Player player;
	ArrayList<BlockSide> existing; //a list of the sides that exist!
	ArrayList<BlockSide> sides; //a list of the sides to render!
	
	public World(Runner r)
	{
		//init included
		parent = r;
		loaded = new ArrayList<Chunk>();
		player = new Player(this);
		existing = new ArrayList<BlockSide>(); 
		sides = new ArrayList<BlockSide>(); 
	}
	
	public void prepareRender()
	{
		//Collections.sort(loaded);
		for (Chunk c : loaded)
		{
			c.prepareForRender();
		}
		trimSides();
		
		parent.curr = System.currentTimeMillis();
		//System.out.println("prep "+(parent.curr-parent.last));
		parent.last = parent.curr;
		
		Collections.sort(sides);
		
		parent.curr = System.currentTimeMillis();
		//System.out.println("sort "+(parent.curr-parent.last));
		parent.last = parent.curr;
	}
	
	public Chunk getChunk(int x, int y, int z, int w)
	{
		for (Chunk e : loaded)
		{
			if (e.x == x && e.y == y && e.z == z && e.w == w)
			{
				return e;
			}
		}
		return null;
	}
	
	//takes the existing arraylist and converts it to the sides arraylist, trimming as it goes
	public void trimSides()
	{
		if (existing.size() > 0)
		{
			ArrayList<BlockSide> visited = new ArrayList<BlockSide>(); //the ids
			sides = new ArrayList<BlockSide>(); 
			
			for (BlockSide e : existing)
			{
				double a = -Math.cos(player.pitch)*Math.sin(player.yaw)*(e.parent.getPosition().add(e.getOffset()).x - player.pos.x) + Math.sin(player.pitch)*(e.parent.getPosition().add(e.getOffset()).y - player.pos.y) - Math.cos(player.pitch)*Math.cos(player.yaw)*(e.parent.getPosition().add(e.getOffset()).z - player.pos.z);
				double b = parent.alphaFunction(e.parent.getPosition());
				
				double renderdist = 50;
				if (a < 0 || a > renderdist || b == 0)
					continue;
				
				//find matching id using binary search
				
				int index = visited.indexOf(e.id);
				
				for (BlockSide o : visited)
				{
					if (e.id == o.id)
					{
						if (parent.alphaFunction(o.parent.getPosition()) > b)
							continue;
						else
							sides.remove(o);
					}
				}
				
				visited.add(e);
				
				sides.add(e);
			}
		}
	}
}



class BlockSide implements Comparable
{
	Block parent;
	byte value = -1;
	//temporary
	String id;
	//0-7: +x, -x, +y, -y, +z, -z, (+w, -w)
	
	public BlockSide(Block p, byte v)
	{
		parent = p;
		value = v;
		
		//temporary
		id = parent.getPosition().toString()+" "+v;
	}

	public int compareTo(Object o) 
	{
		if (!(o instanceof BlockSide))
			return 1;
		
		//player.pos.x -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.sin(player.yaw);
		//player.pos.y += player.movedamp * player.speed * delta * Math.sin(player.pitch);
		//player.pos.z -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.cos(player.yaw);
		
		//distance = -cos(pitch)*sin(yaw)*dx + sin(pitch)*dy + cos(pitch)*cos(yaw)*dz
		Point4D point = parent.getPosition().add(getOffset());
		Point4D other = ((BlockSide)o).parent.getPosition().add( ((BlockSide)o).getOffset() );
		
		
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
	
	public Point4D getOffset()
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
		
		//this added to make sure that the sides still render in the right place. It's messy, but necessary.
		res.w = -parent.getPosition().w;
		
		return res;
	}
}