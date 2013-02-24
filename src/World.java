import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


public class World 
{
	int loaddistance = 3;
	Runner parent;
	Chunk[][][][] loaded;
	ChunkLoader cl;
	Player player;
	ArrayList<BlockSide> existing; //a list of the sides that exist!
	ArrayList<BlockSide> sides; //a list of the sides to render!
	//Tree<Chunk> worldtree;
	boolean removeBackFaces = false; //this doesn't belong here, but temporarily. Also it doesn't work.
	//boolean removeTransparentBackFaces = true;
	
	public World(Runner r)
	{
		//init included
		parent = r;
		loaded = new Chunk[loaddistance][loaddistance][loaddistance][loaddistance];
		player = new Player(this);
		existing = new ArrayList<BlockSide>(); 
		sides = new ArrayList<BlockSide>(); 
	}
	
	/*
	public void addToTree(Chunk e, Tree<Chunk> tree)
	{
		if (tree == null)
			tree = worldtree;
		
		if (tree == null)
		{
			worldtree = new Tree<Chunk>(e);
			tree = worldtree;
		}
		
		if (e.getPosition().compareTo((tree.head.getPosition())) < 0)
		{
			if (tree.leafs.size() > 0)
			{
				if (tree.leafs.get(0).head.world == null)
					tree.setLeaf(0, e);
				else 
					addToTree(e, tree.leafs.get(0));
			}
			else
				tree.addLeaf(e);
		}
		if (e.getPosition().compareTo((tree.head.getPosition())) > 0)
		{
			if (tree.leafs.size() > 1)
				addToTree(e, tree.leafs.get(1));
			
			else if (tree.leafs.size() < 1)
				tree.addLeaf(new Chunk(null, 0,0,0,0));
			else
				tree.addLeaf(e);
		}
	}
	*/
	
	Point4D prevdir = new Point4D(0,0,0,0);
	Point4D currdir = new Point4D(0,0,0,0);
	
	Point4D prevpos = new Point4D(0,0,0,0);
	Point4D currpos = new Point4D(0,0,0,0);
	
	public void prepareRender()
	{
		currdir = new Point4D(player.yaw, player.pitch, player.roll, player.wane);
		currpos = new Point4D(player.pos.x, player.pos.y, player.pos.z, player.pos.w);
		
		if (currdir.compareTo(prevdir) == 0 && currpos.compareTo(prevpos) == 0)
			return;
		
		//Collections.sort(loaded);
		for (int i = 0; i < loaddistance; i++)
		{
			for (int j = 0; j < loaddistance; j++)
			{
				for (int k = 0; k < loaddistance; k++)
				{
					for (int l = 0; l < loaddistance; l++)
					{
						loaded[i][j][k][l].prepareForRender();
					}
				}
			}
		}
		//trimSides();
		
		//Tree<BlockSide> bsp = new Tree<BlockSide>();
		
		//if (sides.size() != 11)
		//	System.out.println("Failed: "+sides.size());
		
		//parent.curr = System.currentTimeMillis();
		//System.out.println("prep "+(parent.curr-parent.last));
		//parent.last = parent.curr;
		
		//Collections.sort(sides);
		
		//parent.curr = System.currentTimeMillis();
		//System.out.println("sort "+(parent.curr-parent.last));
		//parent.last = parent.curr;
		
		prevdir = currdir;
		prevpos = currpos;
	}
	
	//This function might not be used
	public Byte[] getSideOrder()
	{
		//create a linear interpolation of the user's current direction
		//let it collide with the 1x1x1 box of the player
		//at that collision point, find the order of the sides, closest to farthest
		//this gives the list of sides that are farthest to closest.
		
		Point4D test = new Point4D(-Math.cos(player.pitch) * Math.sin(player.yaw), Math.sin(player.pitch), -Math.cos(player.pitch) * Math.cos(player.yaw), 0);
		
		Map<Byte, Double> a = new HashMap<Byte, Double>();
		a.put(new Byte((byte)0), test.dist(new Point4D(1,0,0,0)));
		a.put(new Byte((byte)1), test.dist(new Point4D(-1,0,0,0)));
		a.put(new Byte((byte)2), test.dist(new Point4D(0,1,0,0)));
		a.put(new Byte((byte)3), test.dist(new Point4D(0,-1,0,0)));
		a.put(new Byte((byte)4), test.dist(new Point4D(0,0,1,0)));
		a.put(new Byte((byte)5), test.dist(new Point4D(0,0,-1,0)));
		
		class DoubleComparator implements Comparator<Byte>
		{
			Map<Byte, Double> base;
			public DoubleComparator(Map<Byte, Double> base) {
				this.base = base;
			}
		
			// Note: this comparator imposes orderings that are inconsistent with equals.
			public int compare(Byte a, Byte b) {
				if (base.get(a) >= base.get(b)) {
					return 1;
				} else {
					return -1;
				} // returning 0 would merge keys
			//Note that this will create a reverse ordering because the farthest objects must be rendered first for blending to work
			}
		}
		
        DoubleComparator comp = new DoubleComparator(a);
        TreeMap<Byte,Double> sorted = new TreeMap<Byte,Double>(comp);
        sorted.putAll(a);
        
        Byte[] res = new Byte[6];
        sorted.keySet().toArray(res);
        return res;
	}
	
	/*
	//takes the existing arraylist and converts it to the sides arraylist, trimming as it goes
	public void trimSides()
	{
		if (existing.size() > 0)
		{
			//Add objects in sorted position (linear time)
			ArrayList<BlockSide> visited = new ArrayList<BlockSide>(); 
			
			sides = new ArrayList<BlockSide>(); 
			
			//if side order stuff is on
			Byte[] order;
			int rem1 = 0, rem2 = 0, rem3 = 0;
			if (removeBackFaces)
			{
				order = getSideOrder();
				rem1 = order[0];
				rem2 = order[1];
				rem3 = order[2];
			}
			
			
			
			for (BlockSide e : existing)
			{	
				double a = -Math.cos(player.pitch)*Math.sin(player.yaw)*(e.parent.getPosition().add(e.getOffset(false)).x - player.pos.x) 
						+ Math.sin(player.pitch)*(e.parent.getPosition().add(e.getOffset(false)).y - player.pos.y) 
						- Math.cos(player.pitch)*Math.cos(player.yaw)*(e.parent.getPosition().add(e.getOffset(false)).z - player.pos.z);
				double b = parent.alphaFunction(e.parent.getPosition());
				double c = Math.sqrt( Math.pow( new Point4D(player.pos.x, player.pos.y, player.pos.z, 0).dist(e.parent.getPosition().add(e.getOffset(false))), 2) - a*a);
				
				double minimumoffset = -1;
				double renderdist = 50;
				if (a < minimumoffset || a > renderdist || b == 0)
					continue;
				
				//The other side of the triangle that is in the direction of the player is:
				//sqrt( (distance to block)^2 - (distance along view)^2 )
				//let y be this value, and x be a
				// y > slope|x-offset|
				
				double slope = 2;
				if (c > slope*Math.abs(a-minimumoffset))
					continue;
				
				//If side order stuff is on
				if (removeBackFaces && b > 0.75 && ( e.value == rem1 || e.value == rem2 ||e.value == rem3 ) )
					continue;
				
				//if (removeTransparentBackFaces && ( e.value == rem1 || e.value == rem2 ||e.value == rem3 ) )
				//	continue;
				
				//find matching id using binary search
				//binary search (logarithmic time)
				int index = binarySearch(0, visited.size()-1, e, visited);
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
						if (visited.indexOf(o) != -1) 
							visited.remove(visited.indexOf(o));
					}
				}
				
				
				//add to the visited array (linear time)
				for (int  i = 0; i < visited.size(); i++)
				{	
					//special case
					if (e.getPosition().compareTo(visited.get(i).getPosition()) <= 0)
					{
						visited.add(i, e);
						break;
					}
					
					if (i == visited.size()-1)
					{
						visited.add(e);
						break;
					}
				}
				
				//starting case
				if (visited.size() == 0)
					visited.add(e);
				
				sides.add(e);
				
				
				
				
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
			//}
		//}
	//}
	

	public int binarySearch(int startIndex, int endIndex, BlockSide key, ArrayList<BlockSide> array) 
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
		
		if (startIndex == endIndex)
		{
			return (key.getPosition().compareTo(array.get( middleIndex ).getPosition()) == 0) ? middleIndex : -1;
		}
		else if (key.getPosition().compareTo(array.get( middleIndex ).getPosition()) < 0) //key is less than index
		{
			return binarySearch(startIndex, middleIndex, key, array);
		}
		else if (key.getPosition().compareTo(array.get( middleIndex ).getPosition()) > 0) //key is greater than index
		{
			return binarySearch(middleIndex+1, endIndex, key, array);
		}
		return middleIndex;
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
		
		id = getPosition().toString();
	}
	
	public Point4D getPosition()
	{
		return parent.getPosition().add(getOffset(false));
	}

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
	
	public int comparePlane(BlockSide other)
	{
		if (value == 0 || value == 1)
		{
			if (getPosition().x == other.getPosition().x)
				return 0;
			if (getPosition().x > other.getPosition().x)
				return 1;
			if (getPosition().x < other.getPosition().x)
				return -1;
			//parallel to y and z
		}
		if (value == 2 || value == 3)
		{
			if (getPosition().y == other.getPosition().y)
				return 0;
			if (getPosition().y > other.getPosition().y)
				return 1;
			if (getPosition().y < other.getPosition().y)
				return -1;
			//parallel to x and z
		}
		if (value == 4 || value == 5)
		{
			if (getPosition().z == other.getPosition().z)
				return 0;
			if (getPosition().z > other.getPosition().z)
				return 1;
			if (getPosition().z < other.getPosition().z)
				return -1;
			//parallel to x and y
		}
		return 0;
	}
}