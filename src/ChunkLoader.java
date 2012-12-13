import java.util.ArrayList;

//Loads the chunks based on the player's position

public class ChunkLoader 
{
	World world;
	Player player;
	ArrayList<Point4D> loaded;
	
	public ChunkLoader(World w, Player p)
	{
		world = w;
		player = p;
		loaded = new ArrayList<Point4D>();
	}
	
	public void checkStatus()
	{
		//checks player's location, and decides if new chunks need to be loaded
	}
}
