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
	
}
