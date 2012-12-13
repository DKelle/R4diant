4DGame
======

A 4D game made in Java by a group of high school students. Yes, we suck. Yes, we would appreciate it if you helped us.

Note: to make changes to files, add a copy of the project to your IDE (Eclipse, maybe JCreator if you're weird). Make sure to add build path references to lwjgl.jar and lwjgl_util.jar, and to link those to the native libraries in natives/windows (or whatever your OS is). This is the hardest part. (All of these files are in the LWJGL folder.)
When you're done editing files, and are sure the project still works with all the ones you didn't edit, add only the changed files to a new folder in the dropbox with a name of <your name here><version>, and include a text file saying what the changes were.
Oh, and feel free to add your own screenshots, with the format (version)(number as letter), because why not. (ex. 0.1a, 0.1b, 0.1c...)

Updates:


Version 0.1732:

-Chunks and rendering for transparency are now correct.

-Content:
--Block - Redesigned for the new chunk layout. CheckSides function complete.
--Chunk - Now a 4D array. Period. Transparency sort now correct.
--ChunkLoader - A small random terrain function.
--Solid - CheckSides is now just part of block.
--World - render preparations are now fairly complete.
--Runner - Movement dampening corrected. Rendering for transparency.


Version 0.1:


-Rendering is better, including a start to w. Chunks are now loaded into rendering queue.

-Content
--Block - getPosition function added.
--Chunk - ArrayList of blocks. This will be a problem. It's a massive fight between memory and computation power. Choose one or the other... Maybe later there will be an option for both. Added render preparation functions.
--ChunkLoader - Now has the temporary blocks hardcoded.
--Player - movement features updated. Checking to see if a chunk is in view (minimal).
--Point4D - Distance and angle functions changed a bit for yaw. Be careful...
--Solid - checkSides added. This function will probably just be part of Block in the future.
--World - Added prepareForRender, which just calls it for all loaded chunks.
--Runner - Main runner. Movement functions. Rendering. Currently, w distance appears as change in alpha.

-Problems
--Alpha and chunkInView functions are minimal, and will not work correctly later.
--Should chunks have an ArrayList or array? (memory vs. computations)
--Two transparent blocks will block out each other entirely sometimes.. huh?


Version 0.0:


-Bare files. Tentative.

-Content:
--BiomeMap - Maps the biomes to voronoi
--Block - Block. What more to say
--Chunk - 8x8x8x8 chunk of blocks. Might be redesigned.
--ChunkLoader - Loads chunks based on player position
--Entity - Entities.
--Flowing - Gases and liquids.
--FullscreenExample - The runner.
--Generation - Land generation.
--Inventory - Inventory.
--Item - Item.
--ItemOut - Item on the ground.
--Living - A living entity
--OffGridBlock - A block off the grid.
--Ore - Ore, in the blocks
--Particle - Particles, for effects
--Player - The player.
--Point4D - A point, with xyzw.
--Solid - A solid block.
--World - The world, as in the whole game. May be redesigned.


Dropbox created 14:20 Nov 4, 2012
