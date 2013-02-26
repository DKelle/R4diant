

public class World 
{
	/** The distance, in chunks, to be loaded in each direction. Must be an odd number because the player is at the center. */
	int loaddistance = 3;
	Runner parent;
	/** The 4D array of loaded chunks. */
	Chunk[][][][] loaded;
	ChunkLoader cl;
	Player player;
	
	Point4D prevdir = new Point4D(0,0,0,0);
	Point4D currdir = new Point4D(0,0,0,0);
	
	Point4D prevpos = new Point4D(0,0,0,0);
	Point4D currpos = new Point4D(0,0,0,0);
	
	public World(Runner r)
	{
		//init included
		parent = r;
		loaded = new Chunk[loaddistance][loaddistance][loaddistance][loaddistance];
		player = new Player(this);
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
	
	/**
	 * Goes through all the chunks in the loaded array and adds their sides to BSP trees 
	 */
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
		
		prevdir = currdir;
		prevpos = currpos;
	}
	
	//double a = -Math.cos(player.pitch)*Math.sin(player.yaw)*(e.parent.getPosition().add(e.getOffset(false)).x - player.pos.x) 
	//+ Math.sin(player.pitch)*(e.parent.getPosition().add(e.getOffset(false)).y - player.pos.y) 
	//- Math.cos(player.pitch)*Math.cos(player.yaw)*(e.parent.getPosition().add(e.getOffset(false)).z - player.pos.z);	
	//double b = parent.alphaFunction(e.parent.getPosition());
	//double c = Math.sqrt( Math.pow( new Point4D(player.pos.x, player.pos.y, player.pos.z, 0).dist(e.parent.getPosition().add(e.getOffset(false))), 2) - a*a);

	//This function is no longer necessary, but we might need it again
	/* 
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
	*/
	
	
}