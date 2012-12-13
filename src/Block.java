import java.util.ArrayList;


public class Block 
{
	Chunk chunk;
	short pos;
	short temp;
	short id;
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
	
	//check methods
	public void checkSides()
	{
		//check to see which sides are exposed to air
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

}
