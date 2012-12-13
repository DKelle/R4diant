import java.util.ArrayList;
import java.util.Collections;


public class World 
{
	Runner parent;
	ArrayList<Chunk> loaded;
	ChunkLoader cl;
	Player player;
	
	public World(Runner r)
	{
		//init included
		parent = r;
		loaded = new ArrayList<Chunk>();
		player = new Player(this);
	}
	
	public void prepareRender()
	{
		Collections.sort(loaded);
		for (Chunk c : loaded)
		{
			c.prepareForRender();
		}
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
	
}
