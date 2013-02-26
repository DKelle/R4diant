import java.awt.Font;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.util.glu.GLU.*;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
 
public class Runner 
{
	//note: OpenGL actually uses degrees for their matrix transforms.
	
	World world;
	Player player;
	Texture terrain;
	long lastFrame;
	int fps;
	long lastFPS;
	boolean vsync;
	boolean isCloseRequested;
	private TrueTypeFont font;
	
	//stuff that should be moved
	//Clean this up, please.
	boolean jump;
	boolean onGround = true;
	boolean removeChunk;
	boolean removeBlock;
	boolean switched;
	byte canMoves; //(z-)(z+)(y-)(y+)(x-)(x+) 
	Block playerBlock;
	double velX;
	double velY;
	double velZ;
	double velW;
	double gravity = .005;
	short tempX;
	short tempY;
	short tempZ;
	short tempW;
	short temp2X;
	short temp2Y;
	short temp2Z;
	short temp2W;
	
	long curr = 0, last = 0; //for timing debug
	/* place this anywhere for a time debug
	curr = System.currentTimeMillis();
	System.out.println((curr-last)/1000.0);
	last = curr;
	*/
	
	private enum GameState
	{
		PAUSED, IN_GAME, MAIN_MENU;
	}
	
	GameState state = GameState.IN_GAME;
 
	public void start() 
	{
		//Mouse.setGrabbed(true);
		try 
		{
			world = new World(this);
			player = world.player;
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} 
		catch (LWJGLException e) 
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		init();
		getDelta(); // call once before loop to initialize lastFrame
		lastFPS = getTime(); // call before loop to initialize fps timer
 
		while (!isCloseRequested) 
		{
			int delta = getDelta();
			update(delta); //Use this function for tasks every frame
			
			if (Display.isActive() || Display.isDirty() || Display.isVisible())
				renderGL(); //Redirect to rendering
 
			Display.update();
			Display.sync(60); //FPS stuff
		}
 
		Display.destroy();
	}
	
	/** used to refocus mouse into center of the window. */
 	int counter = 0;
 	
	public void update(int delta) 
	{
		//Unused mouse controls
		/*
		//~~~Mouse controls~~~//
		if (mouseLock)
		{
			if (Display.isActive())
			{
					player.yaw -= (player.rotspeed*delta)*Mouse.getDX()*.08;
					player.pitch += (player.rotspeed*delta)*Mouse.getDY()*.08;
					if (player.pitch > Math.PI/2)
						player.pitch = (float)Math.PI/2;
					if (player.pitch < -Math.PI/2)
						player.pitch = -(float)Math.PI/2;
			}
		}
		//~~~.Mouse controls~~~//
		
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
		{
			if (!pauseLock)
			{
				mouseLock=!mouseLock;
				Mouse.setGrabbed(mouseLock);
				pauseLock = true;
			}
			if (!Keyboard.getEventKeyState())
				pauseLock = false;
		}
		*/
		
		if (Display.isCloseRequested())
			isCloseRequested = true;
		
				
		switch (state)
		{
			case IN_GAME:
			{
				world.cl.checkStatus();
				
				//Very messy player movement. This needs to be fixed for the new chunk setup.
				//Also, I added an enum to player that should help with some of these things.
				//Make sure all of this in player later. Runner should not be a gigantic file.
				
				if(!onGround)
				{
					player.velocity -= player.gravity;
					if(player.pos.y<2)
					{
						onGround = true;
						player.velocity = 0;
					}
				}
				else
				{
					player.velocity = 0;
				}

				//else System.out.println("e is null");
				
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
					//player.pos.z += player.movedamp * player.speed * Math.sin( player.yaw ) * delta;
					//player.pos.x -= player.movedamp * player.speed * Math.cos( player.yaw ) * delta;
					velZ += player.movedamp * player.speed * Math.sin( player.yaw ) * delta;
					velX +=  -1 * player.movedamp * player.speed * Math.cos( player.yaw ) * delta;
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_D) ) 
				{

					velZ  += -1 * player.movedamp * player.speed * Math.sin( player.yaw ) * delta;
					velX += player.movedamp * player.speed * Math.cos( player.yaw ) * delta;
				}

				if (Keyboard.isKeyDown(Keyboard.KEY_W)) 
				{
					
					velX += -1 * player.movedamp * player.speed * Math.cos(player.pitch) * Math.sin(player.yaw) * delta;
					velY +=  player.movedamp * player.speed * Math.sin(player.pitch) * delta;
					velZ += -1 * player.movedamp * player.speed * Math.cos(player.pitch) * Math.cos(player.yaw) * delta;
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_S)) 
				{
					velX += player.movedamp * player.speed * Math.cos(player.pitch) * Math.sin(player.yaw) * delta;
					velY += -1 * player.movedamp * player.speed * Math.sin(player.pitch) * delta;
					velZ += player.movedamp * player.speed * Math.cos(player.pitch) * Math.cos(player.yaw) * delta;
				}
				
				if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
				{
					if(onGround){
						player.velocity = .1;
						onGround = false;
					}
					
				}
				if(Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)){
					player.pos.y += delta*player.speed;
				}
				else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
				{
					velY += -1 * delta*player.speed;
				}
				
				if (Keyboard.isKeyDown(Keyboard.KEY_Q))
				{
					velW += -1 * delta*player.speed;
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_E))
				{
					velW += delta*player.speed;
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
				
				//Checking for collisions.
				/*
				tempX = (short)((int)(player.pos.x%8));
				tempY = (short)((int)(player.pos.y%8));
				tempZ = (short)((int)(player.pos.z%8));
				tempW = (short)((int)(player.pos.w%8));
				
				if( player.pos.z < 0 ) tempZ += 7;
				if( player.pos.x < 0) tempX += 7;
				if( player.pos.y < 0 ) tempY += 7;
				if( player.pos.w < 0 ) tempW += 7;
				
				
				
				Chunk e = player.getChunk();
				removeChunk = false;
				if(e == null){
					
					player.cl.loaded.add(new Point4D(0,0,0,0));
					temp2X = (short)((int)(player.pos.x)/8);
					temp2Y = (short)((int)(player.pos.y)/8);
					temp2Z = (short)((int)(player.pos.z)/8);
					temp2W = (short)((int)(player.pos.w)/8);
					
					if(player.pos.x < 0) temp2X -=1;
					if(player.pos.y < 0) temp2Y -=1;
					if(player.pos.z < 0) temp2Z -=1;
					if(player.pos.w < 0) temp2W -=1;
					
					e = new Chunk(world, (int)(temp2X), (int)(temp2Y), (int)(temp2Z), (int)(temp2W));
					player.cl.world.loaded.add(e);
					removeChunk = true;

				}

				if(e!=null){
					//e.data[(int)(player.pos.x%8)][(int)(player.pos.y%8)][(int)(player.pos.z%8)][(int)(player.pos.w%8)] = new Block(e, (short)(512*((int)(player.pos.w%8))+64*((int)(player.pos.z%8))+8*((int)(player.pos.y%8))+((int)(player.pos.x%8))));
					//playerBlock = e.data[(int)(player.pos.x%8)][(int)(player.pos.y%8)][(int)(player.pos.z%8)][(int)(player.pos.w%8)];
					//playerBlock = e.data[(int)(player.pos.x%8)][(int)(player.pos.y%8)][(int)(player.pos.z%8)][(int)(player.pos.w%8)];
					if(e.data[(int)tempX][(int)tempY][(int)tempZ][(int)tempW] == null){
						e.data[(int)tempX][(int)tempY][(int)tempZ][(int)tempW] = new Block(e, (short)(512*((int)tempW)+64*((int)tempZ)+8*((int)tempY)+((int)tempX)));
						removeBlock = true;
					}
					playerBlock = e.data[(int)tempX][(int)tempY][(int)tempZ][(int)tempW];
					
					//if(remove) player.cl.world.loaded.add(e);
					if(playerBlock!=null){
						playerBlock.checkSides();
						if(((playerBlock.sides >> 3)&1) == 1){
						//	System.out.println("On ground");
							onGround = true;
						}	
						else{
						//	System.out.println("Not");
							onGround = false;
						}	
						
						velY = player.velocity;
						
						if(!((playerBlock.sides & 1 ) ==1) && velX > 0)
							canMoves+=1;
						else if(!((playerBlock.sides >> 1 & 1 ) ==1) && velX < 0)
							canMoves +=2;
						if(!((playerBlock.sides >> 2 & 1 ) ==1) && velY > 0){
							canMoves +=4;
						}
							
						else if(!((playerBlock.sides >> 3 & 1 ) ==1) )
							canMoves +=8;
						if(!((playerBlock.sides >> 4 & 1 ) ==1) && velZ > 0)
							canMoves +=16;
						else if(!((playerBlock.sides >> 5 & 1 ) ==1) && velZ < 0)
							canMoves += 32;
						if(!((playerBlock.sides >> 6 & 1 ) ==1) && velW > 0)
							canMoves += 64;
						else if(!((playerBlock.sides >> 7 & 1 ) ==1) && velW < 0)
							canMoves += 128;
						
						
						if(removeBlock)e.data[(int)tempX][(int)tempY][(int)tempZ][(int)tempW] = null;
						if(removeChunk) player.cl.world.loaded.remove(e);
						e = null;
					
						
						if((canMoves & 1) == 1 || (canMoves >> 1 & 1) == 1){
						//	System.out.println("can x");
							player.pos.x += velX;
						}
						if((canMoves >> 2 & 1 ) == 1 || (canMoves >> 3 & 1) == 1){
						//	System.out.print("Can y");
							player.pos.y += velY;
						}
						else{
						//	System.out.println("can moves: "+canMoves);
						}
						if((canMoves >> 4 & 1 ) == 1 || (canMoves >> 5 & 1) == 1){
						//	System.out.println("can z");
							player.pos.z += velZ;
						}
						if((canMoves >> 6 & 1) == 1 || (canMoves >> 7 & 1) == 1){
						//	System.out.println("can w");
							player.pos.w += velW;
						}
						
						velX = 0; velY = 0; velZ = 0; velW = 0;
						canMoves = 0;
					}
				}
				*/
				
					//System.out.println("e = null");
					velY = player.velocity;
					//onGround = false;
			//		player.velocity = .1;
					player.pos.x += velX;
					player.pos.y += velY;
					player.pos.z += velZ;
					player.pos.w += velW;
					velX = 0; velY = 0; velZ = 0; velW = 0;	
				
				break;
			}
			case PAUSED:
			{
				
				break;
			}
			case MAIN_MENU:
			{
				break;
			}
			
		}
		
		
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && !switched)
		{
			switched = true;
			if (state.equals(GameState.IN_GAME))
				state = GameState.PAUSED;
			else if (state.equals(GameState.PAUSED))
				state = GameState.IN_GAME;
			//System.out.println(state);
			
		}
		if(!Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
			switched = false;
		}
 
		updateFPS(); // update FPS Counter
	}
	
 
	/**
	 * Set the display mode to be used (LWJGL function)
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

	/**
	 * Initialize everything. Only run once at beginning.
	 */
	public void init() 
	{
		Font awtFont = new Font("Times New Roman", Font.BOLD, 16);
		font = new TrueTypeFont(awtFont, false);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(45.0f, 4/3.0f, 0.1f, 1000);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		
		glEnable(GL_TEXTURE_2D);
		try 
		{
			terrain = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/texture.png"), GL_NEAREST);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		terrain.bind();
		
		glEnable(GL_BLEND); 
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		glShadeModel(GL_SMOOTH); 
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		glClearDepth(1.0f);                         // Depth Buffer Setup
		glEnable(GL_DEPTH_TEST);                        // Enables Depth Testing
		glDepthFunc(GL_LEQUAL);                         // The Type Of Depth Test To Do
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	}
	
	/**
	 * Render function
	 */
	public void renderGL() 
	{
		
		// Clear The Screen And The Depth Buffer
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
		// R,G,B,A Set The Color To Blue One Time Only
		
		curr = System.currentTimeMillis();
		//System.out.println("start "+(curr-last));
		last = curr;
		
		world.prepareRender();
		//This function prepares world.sides for the correct rendering stuff, *including* a sort
		
		//curr = System.currentTimeMillis();
		//System.out.println("prep "+(curr-last));
		//last = curr;
		
		//render!!
		//make sure not to render things that are outside of the relevant view
		
		glLoadIdentity();
		glRotatef(-(float)Math.toDegrees(player.roll), 0, 0, 1);
		glRotatef(-(float)Math.toDegrees(player.pitch), 1, 0, 0);
		glRotatef(-(float)Math.toDegrees(player.yaw), 0, 1, 0);
		glTranslatef(-(float)player.pos.x, -(float)player.pos.y, -(float)player.pos.z);
		
		vertexBufferShit();
		
		glTranslatef((float)player.pos.x, (float)player.pos.y, (float)player.pos.z);

		drawDebug();
	}

	/**
	 * Writes text to the screen using witchcraft. Can merge this with render function later.
	 */
	public void drawDebug()
	{
		//For some reason this draws text
		//glEnable(GL_TEXTURE_2D);
		//glShadeModel(GL_SMOOTH);        
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_LIGHTING);                    
	 
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);                
	    glClearDepth(1);                                       
	
        //glEnable(GL_BLEND);
	    //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	 
        glViewport(0,0,800,600);
		glMatrixMode(GL_MODELVIEW);
	 
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, 800, 600, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
			
		font.drawString(10, 10, "W: " + round(3,player.pos.w) );
		font.drawString(10, 28, "X: " + round(3,player.pos.x) );
		font.drawString(10, 46, "Y: " + round(3,player.pos.y) );
		font.drawString(10, 64, "Z: " + round(3,player.pos.z) );
		font.drawString(100, 10, "YAW: " + round(3,player.yaw));
		font.drawString(100, 28, "PITCH: " + round(3,player.pitch));
		font.drawString(100, 46, "ROLL: " + round(3,player.roll));
		font.drawString(100, 64, "WANE: " + round(3,player.wane));
			
		//glDisable(GL_TEXTURE_2D);
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(45.0f, 4/3.0f, 0.1f, 1000);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		glEnable(GL_DEPTH_TEST);
		glDepthFunc(GL_LEQUAL);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
	}
	
	/**
	 * Loads the rendering buffer with things to render
	 */
	public void vertexBufferShit()
	{
		IntBuffer buffer = BufferUtils.createIntBuffer(1);
		glGenBuffers(buffer);
		int vertex_buffer_id = buffer.get(0);
		
		//count up all the sides that need to be rendered
		int sidecount = 0;
		for (int i = 0; i < world.loaddistance; i++)
		{ 
			for (int j = 0; j < world.loaddistance; j++)
			{ 
				for (int k = 0; k < world.loaddistance; k++)
				{ 
					for (int l = 0; l < world.loaddistance; l++)
					{
						sidecount += world.loaded[i][j][k][l].treesize;
					}
				}
			}	
		}
		
		FloatBuffer vertexdata = BufferUtils.createFloatBuffer(sidecount*4*9); //xyz rgba uv
		iterativeLoad(vertexdata);
		
		vertexdata.rewind();
		glBindBuffer(GL_ARRAY_BUFFER, vertex_buffer_id);
		glBufferData(GL_ARRAY_BUFFER, vertexdata, GL_DYNAMIC_DRAW);
		
		glVertexPointer(3, GL_FLOAT, 36, 0);
		glColorPointer(4, GL_FLOAT, 36, 12);
		glTexCoordPointer(2, GL_FLOAT, 36, 28);
		
	    org.newdawn.slick.Color.white.bind();
	    terrain.bind();
	    
	    glDrawArrays(GL_QUADS, 0, sidecount*4);
	}
	
	/**
	 * Load the sides by looking through each chunk
	 * @param vertexdata - the array to be loaded into
	 */
	public void iterativeLoad(FloatBuffer vertexdata)
	{
		//Render things in front of the player, farthest to nearest
		for (int chunkx = 0; chunkx < world.loaddistance; chunkx++)
		{
			for (int chunky = 0; chunky < world.loaddistance; chunky++)
			{
				for (int chunkz = 0; chunkz < world.loaddistance; chunkz++)
				{
					for (int chunkw = 0; chunkw < world.loaddistance; chunkw++)
					{
						//if (!player.isInView(world.loaded[chunkx][chunky][chunkz][chunkw]))
						//	continue;
						
						//all of the following are chunks inside the player's view frustum
						world.loaded[chunkx][chunky][chunkz][chunkw].loadFromTree(null, vertexdata);
					}
				}
			}
		}
	}
 
	/**
	 * Main function, only to run once to start the program. Do not call.
	 */
	public static void main(String[] argv) {
		Runner runner = new Runner();
		runner.start();
	}

	/**
	 * Rounds a number to a number of decimal places, for convenience.
	 * @param decPlaces - number of decimal places
	 * @param num - the number to be rounded
	 * @return - the rounded number
	 */
	public double round(int decPlaces, double num)
	{
		int temp = (int)(num*Math.pow(10,decPlaces));
		return (double)(temp/Math.pow(10,decPlaces));
	}
}
