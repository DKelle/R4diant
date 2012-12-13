import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
 
public class FullscreenExample {
 
	/** position of quad */
	float x = 0, y = 0, z = 5;
	float wane = 0, roll = 0, pitch = 0, yaw = 0;
	/** angle of quad rotation */
	float rotation = 0f;
	float speed = 0.01f;
	float rotspeed = 0.1f;
	float movedamp = 1.0f;
	float rotdamp = 1.0f;
 
	/** time at last frame */
	long lastFrame;
 
	/** frames per second */
	int fps;
	/** last fps time */
	long lastFPS;
	
	/** is VSync Enabled */
	boolean vsync;
 
	public void start() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}
 
		initGL(); // init OpenGL
		getDelta(); // call once before loop to initialise lastFrame
		lastFPS = getTime(); // call before loop to initialise fps timer
 
		while (!Display.isCloseRequested()) 
		{
			int delta = getDelta();
 
			update(delta);
			renderGL();
 
			Display.update();
			Display.sync(60); // cap fps to 60fps
		}
 
		Display.destroy();
	}
 
	public void update(int delta) 
	{
		// rotate quad
		//need to add dampening
		
		if (Keyboard.isKeyDown(Keyboard.KEY_I)) 
		{
			pitch += rotspeed * delta;
			if (pitch > 90.0f)
				pitch = 90.0f;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_K)) 
		{
			pitch -= rotspeed * delta;
			if (pitch < -90.0f)
				pitch = -90.0f;
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_J)) yaw += rotspeed * delta;
		if (Keyboard.isKeyDown(Keyboard.KEY_L)) yaw -= rotspeed * delta;
		
		if (Keyboard.isKeyDown(Keyboard.KEY_U)) roll -= rotspeed * delta;
		if (Keyboard.isKeyDown(Keyboard.KEY_O)) roll += rotspeed * delta;
 
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
			
			movedamp = 1.0f/3.0f;
		
		else if ( ( (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D)) 
		    && ( Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S) ) )
		  || ( ( Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_D) ) 
		    && ( Keyboard.isKeyDown(Keyboard.KEY_Q) || Keyboard.isKeyDown(Keyboard.KEY_E) ) )
		  || ( ( Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_S) ) 
			&& ( Keyboard.isKeyDown(Keyboard.KEY_Q) || Keyboard.isKeyDown(Keyboard.KEY_E) ) ) )
			
			movedamp = 1.0f/2.0f;
		
		else 
			movedamp = 1.0f/1.0f;
		
		//dampening: multiply by the dampening factor.
		
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) 
		{
			z -= movedamp * speed * Math.cos( Math.toRadians(yaw+90) ) * delta;
			x -= movedamp * speed * Math.sin( Math.toRadians(yaw+90) ) * delta;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_D)) 
		{
			z -= movedamp * speed * delta * Math.cos( Math.toRadians(yaw-90) );
			x -= movedamp * speed * delta * Math.sin( Math.toRadians(yaw-90) );
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_W)) 
		{
			x -= movedamp * speed * delta * Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw));
			y += movedamp * speed * delta * Math.sin(Math.toRadians(pitch));
			z -= movedamp * speed * delta * Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_S)) 
		{
			x += movedamp * speed * delta * Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw));
			y -= movedamp * speed * delta * Math.sin(Math.toRadians(pitch));
			z += movedamp * speed * delta * Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		}
		
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
		{
			y += delta*speed;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			y -= delta*speed;
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
		
 
		// draw quad
		//GL11.glPushMatrix();
			GL11.glLoadIdentity();
			GL11.glRotatef(-roll, 0, 0, 1);
			GL11.glRotatef(-pitch, 1, 0, 0);
			GL11.glRotatef(-yaw, 0, 1, 0);
			GL11.glTranslatef(-x, -y, -z);
			
 
			GL11.glBegin(GL11.GL_QUADS);
				GL11.glColor4f(1, 0.5f, 0.5f, 1);
				GL11.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Top)
				GL11.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Top)
				GL11.glVertex3f(-1.0f, 1.0f, 1.0f);          // Bottom Left Of The Quad (Top)
				GL11.glVertex3f( 1.0f, 1.0f, 1.0f);          // Bottom Right Of The Quad (Top)
				
				GL11.glColor4f(1, 1, 0.5f, 1);
				GL11.glVertex3f( 1.0f,-1.0f, 1.0f);          // Top Right Of The Quad (Bottom)
				GL11.glVertex3f(-1.0f,-1.0f, 1.0f);          // Top Left Of The Quad (Bottom)
				GL11.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Bottom)
				GL11.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Bottom)
				
				GL11.glColor4f(0.5f, 1, 0.5f, 1);
				GL11.glVertex3f( 1.0f, 1.0f, 1.0f);          // Top Right Of The Quad (Front)
				GL11.glVertex3f(-1.0f, 1.0f, 1.0f);          // Top Left Of The Quad (Front)
				GL11.glVertex3f(-1.0f,-1.0f, 1.0f);          // Bottom Left Of The Quad (Front)
				GL11.glVertex3f( 1.0f,-1.0f, 1.0f);          // Bottom Right Of The Quad (Front)
				
				GL11.glColor4f(0.5f, 1, 1, 1);
				GL11.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Back)
				GL11.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Back)
				GL11.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Back)
				GL11.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Back)
				
				GL11.glColor4f(0.5f, 0.5f, 1, 1);
				GL11.glVertex3f(-1.0f, 1.0f, 1.0f);          // Top Right Of The Quad (Left)
				GL11.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Left)
				GL11.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Left)
				GL11.glVertex3f(-1.0f,-1.0f, 1.0f);          // Bottom Right Of The Quad (Left)
				
				GL11.glColor4f(1, 0.5f, 1, 1);
				GL11.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Right)
				GL11.glVertex3f( 1.0f, 1.0f, 1.0f);          // Top Left Of The Quad (Right)
				GL11.glVertex3f( 1.0f,-1.0f, 1.0f);          // Bottom Left Of The Quad (Right)
				GL11.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Right)
			GL11.glEnd();
		//GL11.glPopMatrix();
			
		rotation += 3;
		
		
	}
 
	public static void main(String[] argv) {
		FullscreenExample fullscreenExample = new FullscreenExample();
		fullscreenExample.start();
	}
}