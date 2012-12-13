import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
 
public class Runner 
{
	//TODO: convert current degrees measures into radians for easier access
	//Use player position and rotation values instead of ones in this class
	//note: OpenGL actually uses degrees for their matrix transforms.
	
	//a list of blocks to use / world reference
	World world;
	Player player;
 
	/** time at last frame */
	long lastFrame;
 
	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;
	
	/** is VSync Enabled */
	boolean vsync;
	
	GUICanvas canvas;
 
	public void start() {
		try {
			world = new World(this);
			player = world.player;
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		JFrame f = new JFrame();
		canvas = new GUICanvas(this);
		f.add(canvas);
		f.setLocation(200,100);
		f.setSize(50,200);
		f.setDefaultCloseOperation(3);
		f.setVisible(true);
 
		initGL(); // init OpenGL
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer
 
		while (!Display.isCloseRequested()) 
		{
			int delta = getDelta();
 
			update(delta);
			
			if (Display.isActive() || Display.isDirty() || Display.isVisible())
				renderGL();
			
			canvas.paint(canvas.getGraphics());
 
			Display.update();
			Display.sync(60); // cap fps to 60fps
		}
 
		Display.destroy();
	}
 
	public void update(int delta) 
	{
		//player.yaw -= (player.rotspeed*delta)*Mouse.getDX()*.15;
		//player.pitch += (player.rotspeed*delta)*Mouse.getDY()*.15;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_I)) 
		{
			player.pitch += player.rotspeed * delta;
			if (player.pitch > Math.PI/2)
				player.pitch = (float)Math.PI/2;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_K)) 
		{
			player.pitch -= player.rotspeed * delta;
			if (player.pitch < -Math.PI/2)
				player.pitch = -(float)Math.PI/2;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_J)) player.yaw += player.rotspeed * delta;
		if (Keyboard.isKeyDown(Keyboard.KEY_L)) player.yaw -= player.rotspeed * delta;
		
		if (player.yaw < -Math.PI) player.yaw += 2*(float)Math.PI;
		if (player.yaw > Math.PI) player.yaw -= 2*(float)Math.PI;
		
		//if (Keyboard.isKeyDown(Keyboard.KEY_U)) roll -= rotspeed * delta;
		//if (Keyboard.isKeyDown(Keyboard.KEY_O)) roll += rotspeed * delta;
 
		/*
			x' = x cos a cos b - y (cos a cos c sin b + sin a sin c) + z (cos a sin b sin c - cos c sin a)
			y' = x sin b + y cos b cos c - z cos b sin c
			z' = x cos b sin a + y (cos a sin c - cos c sin a sin b) + z (cos a cos c + sin a sin b sin c)
			
			x' = cos a cos b
			y' = sin b
			z' = cos b sin a
		 */
		
		if ( ( ( Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D) ) ) 
		  && ( ( Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S) ) )
		  && ( ( Keyboard.isKeyDown(Keyboard.KEY_Q) || Keyboard.isKeyDown(Keyboard.KEY_E) ) ) )
			
			player.movedamp = 1.0f/(float)Math.sqrt(3);
		
		else if ( ( (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D)) 
		    && ( Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S) ) )
		  || ( ( Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D) ) 
		    && ( Keyboard.isKeyDown(Keyboard.KEY_Q) || Keyboard.isKeyDown(Keyboard.KEY_E) ) )
		  || ( ( Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S) ) 
			&& ( Keyboard.isKeyDown(Keyboard.KEY_Q) || Keyboard.isKeyDown(Keyboard.KEY_E) ) ) )
			
			player.movedamp = 1.0f/(float)Math.sqrt(2);
		
		else 
			player.movedamp = 1.0f/1.0f;
		
		//dampening: multiply by the dampening factor.
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) 
		{
			player.pos.z += player.movedamp * player.speed * Math.sin( player.yaw ) * delta;
			player.pos.x -= player.movedamp * player.speed * Math.cos( player.yaw ) * delta;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_D)) 
		{
			player.pos.z -= player.movedamp * player.speed * delta * Math.sin( player.yaw );
			player.pos.x += player.movedamp * player.speed * delta * Math.cos( player.yaw );
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) 
		{
			player.pos.x -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.sin(player.yaw);
			player.pos.y += player.movedamp * player.speed * delta * Math.sin(player.pitch);
			player.pos.z -= player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.cos(player.yaw);
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_S)) 
		{
			player.pos.x += player.movedamp * player.speed * delta * Math.cos(player.pitch) * Math.sin(player.yaw);
			player.pos.y -= player.movedamp * player.speed * delta * Math.sin(player.pitch);
			player.pos.z += player.movedamp * player.speed * delta * Math.cos(player.yaw) * Math.cos(player.pitch);
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			player.pos.y += delta*player.speed;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			player.pos.y -= delta*player.speed;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_Q))
		{
			player.pos.w -= delta*player.speed;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E))
		{
			player.pos.w += delta*player.speed;
		}
		//W/S
		//z -= speed * Math.cos( Math.toRadians(yaw) ) * delta;
		//x -= speed * Math.sin( Math.toRadians(yaw) ) * delta;
		//A/D
		//x -= speed * Math.cos( Math.toRadians(yaw) ) * delta;
		//z -= speed * Math.sin( Math.toRadians(yaw) ) * delta;
		
		
		//if (Keyboard.isKeyDown(Keyboard.KEY_Q)) z += 0.05f * delta;
		//if (Keyboard.isKeyDown(Keyboard.KEY_E)) z -= 0.05f * delta;
 
		
		while (Keyboard.next()) {
		    if (Keyboard.getEventKeyState()) {
		        if (Keyboard.getEventKey() == Keyboard.KEY_F) {
		        	setDisplayMode(800, 600, !Display.isFullscreen());
		        }
		        else if (Keyboard.getEventKey() == Keyboard.KEY_V) {
		        	vsync = !vsync;
		        	Display.setVSyncEnabled(vsync);
		        }
		    }
		}
		
		// keep quad on the screen
 
		updateFPS(); // update FPS Counter
	}
 
	/**
	 * Set the display mode to be used 
	 * 
	 * @param width The width of the display required
	 * @param height The height of the display required
	 * @param fullscreen True if we want fullscreen mode
	 */
	public void setDisplayMode(int width, int height, boolean fullscreen) {

		// return if requested DisplayMode is already set
                if ((Display.getDisplayMode().getWidth() == width) && 
			(Display.getDisplayMode().getHeight() == height) && 
			(Display.isFullscreen() == fullscreen)) {
			return;
		}
		
		try {
			DisplayMode targetDisplayMode = null;
			
			if (fullscreen) {
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;
				
				for (int i=0;i<modes.length;i++) {
					DisplayMode current = modes[i];
					
					if ((current.getWidth() == width) && (current.getHeight() == height)) {
						if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against the 
						// original display mode then it's probably best to go for this one
						// since it's most likely compatible with the monitor
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel()) &&
						    (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else {
				targetDisplayMode = new DisplayMode(width,height);
			}
			
			if (targetDisplayMode == null) {
				System.out.println("Failed to find value mode: "+width+"x"+height+" fs="+fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);
			
		} catch (LWJGLException e) {
			System.out.println("Unable to setup mode "+width+"x"+height+" fullscreen="+fullscreen + e);
		}
	}
	
	/** 
	 * Calculate how many milliseconds have passed 
	 * since last frame.
	 * 
	 * @return milliseconds passed since last frame 
	 */
	public int getDelta() {
	    long time = getTime();
	    int delta = (int) (time - lastFrame);
	    lastFrame = time;
 
	    return delta;
	}
 
	/**
	 * Get the accurate system time
	 * 
	 * @return The system time in milliseconds
	 */
	public long getTime() {
	    return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}
 
	/**
	 * Calculate the FPS and set it in the title bar
	 */
	public void updateFPS() {
		if (getTime() - lastFPS > 1000) {
			Display.setTitle("FPS: " + fps);
			fps = 0;
			lastFPS += 1000;
		}
		fps++;
	}
 
	public void initGL() 
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		//GL11.glOrtho(-4, 4, -3, 3, 0.1, 100);
		//GL11.glFrustum(-1.0, 1.0, -1.0, 1.0, 1, 100);
		GLU.gluPerspective(45.0f, 4/3.0f, 0.1f, 1000);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		GL11.glEnable(GL11.GL_BLEND); 
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glShadeModel(GL11.GL_SMOOTH); 
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClearDepth(1.0f);                         // Depth Buffer Setup
		GL11.glEnable(GL11.GL_DEPTH_TEST);                        // Enables Depth Testing
		GL11.glDepthFunc(GL11.GL_LEQUAL);                         // The Type Of Depth Test To Do
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
	}
 
	///***LOOK!**////
	Byte[] order;
	
	public void renderGL() {
		
		// Clear The Screen And The Depth Buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 
		// R,G,B,A Set The Color To Blue One Time Only
		
		world.prepareRender();
		
		//render!!
		//make sure not to render things that are outside of the relevant view
		
		GL11.glLoadIdentity();
		GL11.glRotatef(-(float)Math.toDegrees(player.roll), 0, 0, 1);
		GL11.glRotatef(-(float)Math.toDegrees(player.pitch), 1, 0, 0);
		GL11.glRotatef(-(float)Math.toDegrees(player.yaw), 0, 1, 0);
		GL11.glTranslatef(-(float)player.pos.x, -(float)player.pos.y, -(float)player.pos.z);
		
		//order = getSideOrder();
		
		for (Chunk c : world.loaded)
		{	
			/*
			for (Byte o : order)
			{	
				if (o.intValue() == 0)
					for (BlockSide e : c.right)
						renderBlockFace(e);
				if (o.intValue() == 1)
					for (BlockSide e : c.left)
						renderBlockFace(e);
				if (o.intValue() == 2)
					for (BlockSide e : c.up)
						renderBlockFace(e);
				if (o.intValue() == 3)
					for (BlockSide e : c.down)
						renderBlockFace(e);
				if (o.intValue() == 4)
					for (BlockSide e : c.front)
						renderBlockFace(e);
				if (o.intValue() == 5)
					for (BlockSide e : c.back)
						renderBlockFace(e);
			}
			*/
			
			for (BlockSide e : c.sides)
			{
				renderBlockFace(e);
			}
		}
		
		GL11.glTranslatef((float)player.pos.x, (float)player.pos.y, (float)player.pos.z);
	}
	
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
		
        DoubleComparator comp =  new DoubleComparator(a);
        TreeMap<Byte,Double> sorted = new TreeMap<Byte,Double>(comp);
        sorted.putAll(a);
        
        Byte[] res = new Byte[6];
        sorted.keySet().toArray(res);
		return res;
	}
	
	public float alphaFunction(Point4D p)
	{
		float alpha = 1.0f - (1.0f/player.wdepth)*(float)Math.abs(p.w - player.pos.w ); //VERY TEMPORARY ALPHA FUNCTION
		if (alpha < 0 )
			alpha = 0;
		if (alpha > 1)
			alpha = 1;
		
		return alpha;
	}
	
	public void renderBlockFace(BlockSide side)
	{
		Point4D p = side.parent.getPosition();
		GL11.glTranslatef((float)p.x +0.5f, (float)p.y +0.5f, (float)p.z +0.5f);
		float alpha = alphaFunction(p);
		
		GL11.glBegin(GL11.GL_QUADS);
		
		if (side.value == 2)
		{
			GL11.glColor4f(1, 0.5f, 0.5f, alpha);
			GL11.glVertex3f( 0.5f, 0.5f,-0.5f);          // Top Right Of The Quad (Top)
			GL11.glVertex3f(-0.5f, 0.5f,-0.5f);          // Top Left Of The Quad (Top)
			GL11.glVertex3f(-0.5f, 0.5f, 0.5f);          // Bottom Left Of The Quad (Top)
			GL11.glVertex3f( 0.5f, 0.5f, 0.5f);          // Bottom Right Of The Quad (Top)
		}
		
		if (side.value == 3)
		{
			GL11.glColor4f(1, 1, 0.5f, alpha);
			GL11.glVertex3f( 0.5f,-0.5f, 0.5f);          // Top Right Of The Quad (Bottom)
			GL11.glVertex3f(-0.5f,-0.5f, 0.5f);          // Top Left Of The Quad (Bottom)
			GL11.glVertex3f(-0.5f,-0.5f,-0.5f);          // Bottom Left Of The Quad (Bottom)
			GL11.glVertex3f( 0.5f,-0.5f,-0.5f);          // Bottom Right Of The Quad (Bottom)
		}
		
		if (side.value == 4)
		{
			GL11.glColor4f(0.5f, 1, 0.5f, alpha);
			GL11.glVertex3f( 0.5f, 0.5f, 0.5f);          // Top Right Of The Quad (Front)
			GL11.glVertex3f(-0.5f, 0.5f, 0.5f);          // Top Left Of The Quad (Front)
			GL11.glVertex3f(-0.5f,-0.5f, 0.5f);          // Bottom Left Of The Quad (Front)
			GL11.glVertex3f( 0.5f,-0.5f, 0.5f);          // Bottom Right Of The Quad (Front)
		}
		
		if (side.value == 5)
		{
			GL11.glColor4f(0.5f, 1, 1, alpha);
			GL11.glVertex3f( 0.5f,-0.5f,-0.5f);          // Bottom Left Of The Quad (Back)
			GL11.glVertex3f(-0.5f,-0.5f,-0.5f);          // Bottom Right Of The Quad (Back)
			GL11.glVertex3f(-0.5f, 0.5f,-0.5f);          // Top Right Of The Quad (Back)
			GL11.glVertex3f( 0.5f, 0.5f,-0.5f);          // Top Left Of The Quad (Back)
		}
		
		if (side.value == 1)
		{
			GL11.glColor4f(0.5f, 0.5f, 1, alpha);
			GL11.glVertex3f(-0.5f, 0.5f, 0.5f);          // Top Right Of The Quad (Left)
			GL11.glVertex3f(-0.5f, 0.5f,-0.5f);          // Top Left Of The Quad (Left)
			GL11.glVertex3f(-0.5f,-0.5f,-0.5f);          // Bottom Left Of The Quad (Left)
			GL11.glVertex3f(-0.5f,-0.5f, 0.5f);          // Bottom Right Of The Quad (Left)
		}
		
		if (side.value == 0)
		{
			GL11.glColor4f(1, 0.5f, 1, alpha);
			GL11.glVertex3f( 0.5f, 0.5f,-0.5f);          // Top Right Of The Quad (Right)
			GL11.glVertex3f( 0.5f, 0.5f, 0.5f);          // Top Left Of The Quad (Right)
			GL11.glVertex3f( 0.5f,-0.5f, 0.5f);          // Bottom Left Of The Quad (Right)
			GL11.glVertex3f( 0.5f,-0.5f,-0.5f);          // Bottom Right Of The Quad (Right)
		}
		
		//note: might have to add something here for w sides
		GL11.glEnd();
		
		GL11.glTranslatef(-(float)p.x -0.5f, -(float)p.y -0.5f, -(float)p.z -0.5f);
	}
 
	public static void main(String[] argv) {
		Runner runner = new Runner();
		runner.start();
	}
}

class GUICanvas extends Canvas
{
	Runner parent;
	Image mem;
	Graphics2D g2;
	
	public GUICanvas(Runner a)
	{ parent = a; }
	
	public void paint(Graphics g)
	{
		mem = createImage(getWidth(), getHeight());
		g2 = (Graphics2D)mem.getGraphics();
		
		g2.setColor(Color.white);
		g2.fillRect(0, 0, 100, 300);
		g2.setColor(Color.black);
		g2.drawString(""+parent.player.movedamp,0,0);
		g2.drawString(""+parent.player.pos.x,0,10);
		g2.drawString(""+parent.player.pos.y,0,20);
		g2.drawString(""+parent.player.pos.z,0,30);
		g2.drawString(""+parent.player.pos.w,0,40);
		g2.drawString(""+parent.player.yaw,0,50);
		g2.drawString(""+parent.player.pitch,0,60);
		//g2.drawString(""+parent.player.roll,0,70);
		//g2.drawString(""+parent.player.wane,0,80);
		
		/*
		String order = "";
		for (int i = 0; i < parent.order.length; i++)
			order = order + parent.order[i].toString() + ", ";
		
		g2.drawString(""+order,0,70);
		*/
		g.drawImage(mem, 0,0, this);
	}
}