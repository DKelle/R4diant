import java.util.ArrayList;


public class World 
{
	ArrayList<Chunk> loaded;
	ChunkLoader cl;
	Player player;
	
	public World()
	{
		loaded = new ArrayList<Chunk>();
		player = new Player(this);
	}
	
	
}
