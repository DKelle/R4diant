import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JFrame;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL41;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;
 
public class Runner 
{
	//note: OpenGL actually uses degrees for their matrix transforms.
	
	//a list of blocks to use / world reference
	World world;
	Player player;
	Texture terrain;
 
	/** time at last frame */
	long lastFrame;
 
	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;
	
	/** is VSync Enabled */
	boolean vsync;
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
	
	boolean flying;

	private enum GameState
	{
		PAUSED,IN_GAME,MAIN_MENU;
	}
	
	GameState state = GameState.IN_GAME;
	
	/* place this anywhere for a time debug
	curr = System.currentTimeMillis();
	System.out.println((curr-last)/1000.0);
	last = curr;
	*/
	
	boolean isCloseRequested;
	
	private TrueTypeFont font;
 
	public void start() {
		//Mouse.setGrabbed(true);
		try {
			world = new World(this);
			player = world.player;
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		initGL(); // init OpenGL
		init();
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer
 
		while (!isCloseRequested) 
		{
			int delta = getDelta();
 
			update(delta);
			
			if (Display.isActive() || Display.isDirty() || Display.isVisible())
				renderGL();
 
			Display.update();
			Display.sync(60); // cap fps to 60fps
		}
 
		Display.destroy();
	}
	
	//used to refocus mouse into center of the window.
 	int counter = 0;
 	
	public void update(int delta) 
	{
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
				
				if(!onGround){
					player.velocity -= player.gravity;
					if(player.pos.y<2){
						onGround = true;
						player.velocity = 0;
					}
				}
				else{
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
			/*	else{
					//System.out.println("e = null");
					velY = player.velocity;
					//onGround = false;
			//		player.velocity = .1;
					player.pos.x += velX;
					player.pos.y += velY;
					player.pos.z += velZ;
					player.pos.w += velW;
					velX = 0; velY = 0; velZ = 0; velW = 0;	
				} */
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
 
	public void init()
	{
		Font awtFont = new Font("Times New Roman", Font.BOLD, 16);
		font = new TrueTypeFont(awtFont, false);
	}

	public void initGL() 
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45.0f, 4/3.0f, 0.1f, 1000);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		try 
		{
			terrain = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/texture.png"), GL11.GL_NEAREST);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		terrain.bind();
		
		GL11.glEnable(GL11.GL_BLEND); 
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glShadeModel(GL11.GL_SMOOTH); 
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GL11.glClearDepth(1.0f);                         // Depth Buffer Setup
		GL11.glEnable(GL11.GL_DEPTH_TEST);                        // Enables Depth Testing
		GL11.glDepthFunc(GL11.GL_LEQUAL);                         // The Type Of Depth Test To Do
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
	}
	
	public void renderGL() {
		
		// Clear The Screen And The Depth Buffer
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 
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
		
		GL11.glLoadIdentity();
		GL11.glRotatef(-(float)Math.toDegrees(player.roll), 0, 0, 1);
		GL11.glRotatef(-(float)Math.toDegrees(player.pitch), 1, 0, 0);
		GL11.glRotatef(-(float)Math.toDegrees(player.yaw), 0, 1, 0);
		GL11.glTranslatef(-(float)player.pos.x, -(float)player.pos.y, -(float)player.pos.z);
		
		vertexBufferShit();
		
		GL11.glTranslatef((float)player.pos.x, (float)player.pos.y, (float)player.pos.z);

		drawDebug();
	}

	public void drawDebug()
	{
		//For some reason this draws text
		//GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glShadeModel(GL11.GL_SMOOTH);        
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);                    
	 
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);                
	    GL11.glClearDepth(1);                                       
	
        //GL11.glEnable(GL11.GL_BLEND);
	    //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	 
        GL11.glViewport(0,0,800,600);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	 
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 600, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
			
		font.drawString(10, 10, "W: " + round(3,player.pos.w) );
		font.drawString(10, 28, "X: " + round(3,player.pos.x) );
		font.drawString(10, 46, "Y: " + round(3,player.pos.y) );
		font.drawString(10, 64, "Z: " + round(3,player.pos.z) );
		font.drawString(100, 10, "YAW: " + round(3,player.yaw));
		font.drawString(100, 28, "PITCH: " + round(3,player.pitch));
		font.drawString(100, 46, "ROLL: " + round(3,player.roll));
		font.drawString(100, 64, "WANE: " + round(3,player.wane));
			
		//GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GLU.gluPerspective(45.0f, 4/3.0f, 0.1f, 1000);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
		
		//initGL(); //DO NOT CALL THIS!
	}
	
	//float prevalpha = 1.0f;
	
	public void vertexBufferShit()
	{
		//if (world.sides != null && world.sides.size() > 0)
		//{	
			IntBuffer buffer = BufferUtils.createIntBuffer(1);
			GL15.glGenBuffers(buffer);
			int vertex_buffer_id = buffer.get(0);
			
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
			
			//dataIterate(world.worldtree, vertexdata);	
			
			iterativeLoad(vertexdata);
			
			/*
			for (BlockSide e : world.sides)
			{
				loadDataFromBlockFace(e, vertexdata);
			}
			*/
			
			vertexdata.rewind();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertex_buffer_id);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexdata, GL15.GL_DYNAMIC_DRAW);
		    
			//current size: 3 + 4 + 2 = 9
			
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 36, 0);
			GL11.glColorPointer(4, GL11.GL_FLOAT, 36, 12);
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 36, 28);
			
		    org.newdawn.slick.Color.white.bind();
		    terrain.bind();
		    
		    GL11.glDrawArrays(GL11.GL_QUADS, 0, sidecount*4);
		    
		    //Finished rendering
		    int a = 0;
		//}
	}
	
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
	
	/*
	public void dataIterate(Tree<Chunk> tree, FloatBuffer vertexdata)
	{
		//loadDataFromBlockFace(e, vertexdata);
		if (tree.leafs.size() == 0)
		{
			loadRecursive(tree.head, );
		}
		
		if (tree.head.getPosition().compareTo(player.pos) > 0)
		{
			dataIterate(tree.leafs.get(0), vertexdata);
		}
		if (tree.head.getPosition().compareTo(player.pos) < 0)
		{
			dataIterate(tree.leafs.get(1), vertexdata);
		}
		
	}
	*/
	
	/*
	//ALL WHITE ***** (textures actually look correct)
	public void loadDataFromBlockFace(BlockSide side, FloatBuffer list)
	{
		//loads data about the face into the buffer arrays
		Point4D p = side.parent.getPosition();
		float alpha = alphaFunction(p);
		
		float[] tc = side.parent.getTextureCoordinates(side.value);
		
		if (side.value == 2)
		{
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			//normal here (x, y, z)
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
		
		if (side.value == 3)
		{
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
		
		if (side.value == 4)
		{
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
		
		if (side.value == 5)
		{
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
		
		if (side.value == 1)
		{
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
		
		if (side.value == 0)
		{
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[0]);
			list.put(tc[1]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y+1));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[2]);
			list.put(tc[3]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z+1));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[4]);
			list.put(tc[5]);
			
			
			list.put(new Float(p.x+1));
			list.put(new Float(p.y));
			list.put(new Float(p.z));
			
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(1));
			list.put(new Float(alpha));
			
			list.put(tc[6]);
			list.put(tc[7]);
		}
	}
	
	public float alphaFunction(Point4D p)
	{
		float alpha = 1.0f - (1.0f/player.wdepth)*(float)Math.abs(p.w - player.pos.w ); //VERY TEMPORARY ALPHA FUNCTION
		if (alpha < 0 )
			alpha = 0;
		if (alpha > 1)
			alpha = 1;
		
		//if (alpha != prevalpha)
		//	System.out.println(prevalpha + " --> " +alpha);
			
		prevalpha = alpha;
		return alpha;
	}
	*/
	
	/*
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
	*/
 
	public static void main(String[] argv) {
		Runner runner = new Runner();
		runner.start();
	}

	public double round(int decPlaces, double num)
	{
		int temp = (int)(num*Math.pow(10,decPlaces));
		return (double)(temp/Math.pow(10,decPlaces));
	}
}
