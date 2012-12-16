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
		
		if (sides.size() != 11)
			System.out.println("Failed: "+sides.size());
		
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
			//Add objects in sorted position (linear time)
			ArrayList<BlockSide> visited = new ArrayList<BlockSide>(); 
			
			sides = new ArrayList<BlockSide>(); 
			
			for (BlockSide e : existing)
			{
				double a = -Math.cos(player.pitch)*Math.sin(player.yaw)*(e.parent.getPosition().add(e.getOffset(false)).x - player.pos.x) 
						+ Math.sin(player.pitch)*(e.parent.getPosition().add(e.getOffset(false)).y - player.pos.y) 
						- Math.cos(player.pitch)*Math.cos(player.yaw)*(e.parent.getPosition().add(e.getOffset(false)).z - player.pos.z);
				double b = parent.alphaFunction(e.parent.getPosition());
				
				double renderdist = 50;
				if (a < 0 || a > renderdist || b == 0)
					continue;
				
				
				
				//find matching id using binary search
				//binary search (logarithmic time)
				int index = binarySearch(0, visited.size()-1, e.id, visited);
				if (index != -1)
				{	
					BlockSide o = visited.get(index);
					//System.out.println("a");
					if (parent.alphaFunction(o.parent.getPosition()) > b)
					{
						continue;
					}
					else
					{
						if (sides.indexOf(o) != -1) 
							sides.remove(sides.indexOf(o));
					}
				}
				
				
				//add to the visited array (linear time)
				for (int  i = 0; i < visited.size(); i++)
				{
					//special case
					if (i == 0 && e.id.compareTo(visited.get(i).id) < 0)
					{
						visited.add(0, e);
					}
					
					//base case
					if (e.id.compareTo(visited.get(i).id) > 0)
					{
						visited.add(i+1, e);
						break;
					}
				}
				
				//starting case
				if (visited.size() == 0)
					visited.add(e);
				
				sides.add(e);
				
				/*
				//add sides in sorted order (attempt)
				for (int  i = 0; i < sides.size(); i++)
				{
					//special case
					if (i == 0 && e.compareTo(sides.get(i)) < 0)
					{
						sides.add(0, e);
					}
					
					//base case
					if (e.compareTo(sides.get(i)) > 0)
					{
						sides.add(i+1, e);
						break;
					}
				}
				
				//starting case
				if (sides.size() == 0)
					sides.add(e);
				*/
				
				
				
				/* Previous code:
				int index = visited.indexOf(e.id);
				
				for (BlockSide o : visited)
				{
					if (e.id.equals(o.id))
					{
						if (parent.alphaFunction(o.parent.getPosition()) > b)
							continue;
						else
							sides.remove(o);
					}
				}
				
				visited.add(e);
				sides.add(e);
				*/
			}
		}
	}

	public int binarySearch(int startIndex, int endIndex, String key, ArrayList<BlockSide> array) 
	{
		if (startIndex < 0 || startIndex >= array.size() || endIndex < 0 || endIndex >= array.size())
		{
			//indices out of bounds.
			//System.out.println("Indices out of bounds..?");
			return -1;
		}
		if (startIndex > endIndex)
		{
			System.out.println("What happened?");
		}
		
		
		int middleIndex = (startIndex + endIndex)/2;
		
		if (startIndex == middleIndex && !array.get( middleIndex ).id.equals(key))
		{
			return -1;
		}
		
		if (array.get( middleIndex ).id.equals(key))
		{
			return middleIndex;
		}
		else if (key.compareTo(array.get( middleIndex ).id) < 0) //key is less than index
		{
			return binarySearch(startIndex, middleIndex-1, key, array);
		}
		else if (key.compareTo(array.get( middleIndex ).id) > 0) //key is greater than index
		{
			return binarySearch(middleIndex+1, endIndex, key, array);
		}
		return -1;
	}
}



class BlockSide implements Comparable
{
	Block parent;
	byte value = -1;
	String id;
	//0-7: +x, -x, +y, -y, +z, -z, (+w, -w)
	
	public BlockSide(Block p, byte v)
	{
		parent = p;
		value = v;
		
		id = parent.getPosition().add(getOffset(false)).toString();
	}

	public int compareTo(Object o) 
	{
		if (!(o instanceof BlockSide))
			return 1;
		
		//player.pos.x -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.sin(player.yaw);
		//player.pos.y += player.movedamp * player.speed * delta * Math.sin(player.pitch);
		//player.pos.z -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.cos(player.yaw);
		
		//distance = -cos(pitch)*sin(yaw)*dx + sin(pitch)*dy + cos(pitch)*cos(yaw)*dz
		Point4D point = parent.getPosition().add(getOffset(false));
		Point4D other = ((BlockSide)o).parent.getPosition().add( ((BlockSide)o).getOffset(false) );
		
		
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
}