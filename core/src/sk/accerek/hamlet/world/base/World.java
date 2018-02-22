package sk.accerek.hamlet.world.base;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.world.block.Block;
import sk.accerek.hamlet.world.block.BlockType;
import sk.accerek.hamlet.world.tile.Tile;
import sk.accerek.hamlet.world.tile.TileType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class World {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ChunkProvider chunkProvider;

    private final List<Chunk> loadedChunks;

    private final com.badlogic.gdx.physics.box2d.World box2dWorld;
    private final Box2DDebugRenderer dDebugRenderer;
    private final @Getter RayHandler rayHandler;

    private final Hamlet hamlet = Hamlet.get();

    public World(ChunkProvider chunkProvider) {
        RayHandler.useDiffuseLight(true);
        RayHandler.setGammaCorrection(true);

        this.chunkProvider = chunkProvider;

        this.loadedChunks = new ArrayList<>();

        this.box2dWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, 0), true);

        this.rayHandler = new RayHandler(box2dWorld);

        float intensity = .35f;
        setAmbientLight(intensity, intensity, intensity, intensity);
        this.rayHandler.setBlur(true);
        this.rayHandler.setBlurNum(5);
        this.rayHandler.setShadows(true);
        this.rayHandler.setCulling(true);

        this.dDebugRenderer = new Box2DDebugRenderer();
    }

    public boolean loadChunk(Pair<Integer, Integer> chunkPosition) {
        Optional<Chunk> optionalChunk = chunkProvider.provide(chunkPosition.getX(), chunkPosition.getY());
        if(!optionalChunk.isPresent()) {
            return false;
        }

        synchronized (loadedChunks) {
            loadedChunks.add(optionalChunk.get());
            loadedChunks.sort(new ChunkComparator());
        }

        logger.debug("Loaded chunk at {}!", chunkPosition);

        return true;
    }

    public boolean unloadChunk(Pair<Integer, Integer> chunkPosition) {
        if(!isChunkLoaded(chunkPosition)) {
            logger.warn("Chunk at {} is already unloaded!", chunkPosition);
            return false;
        }

        synchronized (loadedChunks) {
            Chunk chunk = null;
            for(Chunk chunk0 : loadedChunks) {
                if(chunk0.getChunkPosition().equals(chunkPosition)) {
                    chunk = chunk0;
                    break;
                }
            }
            if(chunk == null) {
                logger.error("Failed to unload chunk at {}: no chunk found at given position", chunkPosition);
                return false;
            }

            chunkProvider.unload(chunk);
            loadedChunks.remove(chunk);
            loadedChunks.sort(new ChunkComparator());

            return true;
        }
    }

    public boolean unloadAllChunks() {
        synchronized (loadedChunks) {
            for(Chunk chunk : loadedChunks) {
                chunkProvider.unload(chunk);
            }

            loadedChunks.clear();
        }

        return true;
    }

    private boolean isChunkLoaded(Pair<Integer, Integer> chunkPosition) {
        synchronized (loadedChunks) {
            for (Chunk chunk : loadedChunks) {
                if (chunk.getChunkPosition().equals(chunkPosition)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Optional<Chunk> getChunk(Pair<Integer, Integer> chunkPosition) {
        synchronized (loadedChunks) {
            for (Chunk chunk : loadedChunks) {
                if (chunk.getChunkPosition().equals(chunkPosition)) {
                    return Optional.of(chunk);
                }
            }
        }

        return Optional.empty();
    }

    public void render(SpriteBatch spriteBatch) {
        synchronized (loadedChunks) {
            for (Chunk chunk : loadedChunks) {
                Pair<Integer, Integer> chunkPosition = chunk.getChunkPosition();
                Pair<Integer, Integer> pixelPosition = new Pair<>(chunkPosition.getX() * Chunk.CHUNK_SIZE * Chunk.GRID_SIZE, chunkPosition.getY() * Chunk.CHUNK_SIZE * Chunk.GRID_SIZE);
                Vector3 screenPosition = Hamlet.get().getCamera().project(new Vector3(pixelPosition.getX(), pixelPosition.getY(), 0));
                if(screenPosition.x + Chunk.CHUNK_PIXEL_SIZE * 1.5 < 0 || screenPosition.x > Hamlet.VIEW_SIZE.x || screenPosition.y + Chunk.CHUNK_PIXEL_SIZE * 1.5 < 0 || screenPosition.y > Hamlet.VIEW_SIZE.y) {
                    continue;
                }
                chunk.render(spriteBatch);
            }
        }

        box2dWorld.step(1/60f, 6, 2);

        spriteBatch.end();

        rayHandler.setCombinedMatrix(hamlet.getCamera());
        rayHandler.updateAndRender();

        //dDebugRenderer.render(box2dWorld, hamlet.getCamera().combined);

        spriteBatch.begin();
    }

    public Pair<Integer, Integer> getChunkForGridPosition(Pair<Integer, Integer> tilePosition) {
        return new Pair<>(tilePosition.getX() >> Chunk.BIT_SIZE, tilePosition.getY() >> Chunk.BIT_SIZE);
    }

    public Pair<Integer, Integer> getPositionInChunk(Pair<Integer, Integer> tilePosition) {
        Pair<Integer, Integer> chunkPosition = getChunkForGridPosition(tilePosition);
        int x = tilePosition.getX() - (chunkPosition.getX() * Chunk.CHUNK_SIZE);
        int y = tilePosition.getY() - (chunkPosition.getY() * Chunk.CHUNK_SIZE);

        return new Pair<>(x, y);
    }

    public void setTile(TileType type, int x, int y) {
        Pair<Integer, Integer> chunkPosition = getChunkForGridPosition(new Pair<>(x, y));
        Pair<Integer, Integer> positionInChunk = getPositionInChunk(new Pair<>(x, y));

        if(!isChunkLoaded(chunkPosition)) {
            boolean chunkLoaded = loadChunk(chunkPosition);
            if(!chunkLoaded) {
                logger.error("Failed to set tile at {}: no chunk was loaded");
                return;
            }
        }

        Optional<Chunk> optionalChunk = getChunk(chunkPosition);
        if(!optionalChunk.isPresent()) {
            logger.error("Failed to set tile at {}: no chunk found at given position", chunkPosition);
            return;
        }

        Chunk chunk = optionalChunk.get();

        chunk.setTile(type, positionInChunk.getX(), positionInChunk.getY());
    }

    public Tile getTile(int x, int y) {
        Pair<Integer, Integer> chunkPosition = getChunkForGridPosition(new Pair<>(x, y));
        Pair<Integer, Integer> positionInChunk = getPositionInChunk(new Pair<>(x, y));

        if(!isChunkLoaded(chunkPosition)) {
            boolean chunkLoaded = loadChunk(chunkPosition);
            if(!chunkLoaded) {
                logger.error("Failed to get tile at {}: no chunk was loaded");
                return null;
            }
        }

        Optional<Chunk> optionalChunk = getChunk(chunkPosition);
        if(!optionalChunk.isPresent()) {
            logger.error("Failed to get tile at {}: no chunk found at given position", chunkPosition);
            return null;
        }

        Chunk chunk = optionalChunk.get();

        return chunk.getTile(positionInChunk.getX(), positionInChunk.getY());
    }

    public void removeTile(int x, int y) {
        Pair<Integer, Integer> chunkPosition = getChunkForGridPosition(new Pair<>(x, y));
        Pair<Integer, Integer> positionInChunk = getPositionInChunk(new Pair<>(x, y));

        if(!isChunkLoaded(chunkPosition)) {
            boolean chunkLoaded = loadChunk(chunkPosition);
            if(!chunkLoaded) {
                logger.error("Failed to remove tile from {}: no chunk was loaded");
                return;
            }
        }

        Optional<Chunk> optionalChunk = getChunk(chunkPosition);
        if(!optionalChunk.isPresent()) {
            logger.error("Failed to remove tile from {}: no chunk found at given position", chunkPosition);
            return;
        }

        Chunk chunk = optionalChunk.get();

        chunk.removeTile(positionInChunk.getX(), positionInChunk.getY());
    }

    public void setBlock(BlockType blockType, int x, int y) {
        Pair<Integer, Integer> chunkPosition = getChunkForGridPosition(new Pair<>(x, y));
        Pair<Integer, Integer> positionInChunk = getPositionInChunk(new Pair<>(x, y));

        if(!isChunkLoaded(chunkPosition)) {
            boolean chunkLoaded = loadChunk(chunkPosition);
            if(!chunkLoaded) {
                logger.error("Failed to set block at {}: no chunk was loaded");
                return;
            }
        }

        Optional<Chunk> optionalChunk = getChunk(chunkPosition);
        if(!optionalChunk.isPresent()) {
            logger.error("Failed to block tile at {}: no chunk found at given position", chunkPosition);
            return;
        }

        Chunk chunk = optionalChunk.get();

        Block block = chunk.setBlock(blockType, positionInChunk.getX(), positionInChunk.getY());

        if(block != null && block.hasPhysics()) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            Pair<Integer, Integer> pixelPosition = gridToPixelPosition(new Pair<>(x, y));
            bodyDef.position.set(new Vector2(pixelPosition.getX(), pixelPosition.getY()));
            bodyDef.fixedRotation = true;

            Body body = box2dWorld.createBody(bodyDef);
            block.setCreatedBox2DBody(body);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.density = 1;
            fixtureDef.friction = 1;
            fixtureDef.restitution = 0;
            fixtureDef.shape = block.getShape();
            fixtureDef.isSensor = false;

            body.createFixture(fixtureDef);

            block.getShape().dispose();
        }
    }

    public Block getBlock(int x, int y) {
        Pair<Integer, Integer> chunkPosition = getChunkForGridPosition(new Pair<>(x, y));
        Pair<Integer, Integer> positionInChunk = getPositionInChunk(new Pair<>(x, y));

        if(!isChunkLoaded(chunkPosition)) {
            boolean chunkLoaded = loadChunk(chunkPosition);
            if(!chunkLoaded) {
                logger.error("Failed to get block no chunk was loaded");
                return null;
            }
        }

        Optional<Chunk> optionalChunk = getChunk(chunkPosition);
        if(!optionalChunk.isPresent()) {
            logger.error("Failed to get block at {}: no chunk found at given position", chunkPosition);
            return null;
        }

        Chunk chunk = optionalChunk.get();

        return chunk.getBlock(positionInChunk.getX(), positionInChunk.getY());
    }

    public void removeBlock(int x, int y) {
        Pair<Integer, Integer> chunkPosition = getChunkForGridPosition(new Pair<>(x, y));
        Pair<Integer, Integer> positionInChunk = getPositionInChunk(new Pair<>(x, y));

        if(!isChunkLoaded(chunkPosition)) {
            boolean chunkLoaded = loadChunk(chunkPosition);
            if(!chunkLoaded) {
                logger.error("Failed to remove block from {}: no chunk was loaded");
                return;
            }
        }

        Optional<Chunk> optionalChunk = getChunk(chunkPosition);
        if(!optionalChunk.isPresent()) {
            logger.error("Failed to remove block from {}: no chunk found at given position", chunkPosition);
            return;
        }

        Chunk chunk = optionalChunk.get();

        Block block = chunk.getBlock(positionInChunk.getX(), positionInChunk.getY());
        if(block == null) {
            logger.error("Failed to remove block from {}: no block found at the given position", chunkPosition);
            return;
        }
        if(block.hasPhysics()) {
            box2dWorld.destroyBody(block.getCreatedBox2DBody());
        }
        chunk.removeBlock(positionInChunk.getX(), positionInChunk.getY());
    }

    public void setAmbientLight(float r, float g, float b, float a) {
        rayHandler.setAmbientLight(r, g, b, a);
    }

    public Pair<Integer, Integer> pixelToGridPosition(float x, float y) {
        return new Pair<>((int) Math.floor(x / Chunk.GRID_SIZE), (int) Math.floor(y / Chunk.GRID_SIZE));
    }

    public Pair<Integer, Integer> gridToPixelPosition(Pair<Integer, Integer> tilePosition) {
        return new Pair<>(tilePosition.getX() * Chunk.GRID_SIZE, tilePosition.getY() * Chunk.GRID_SIZE);
    }

    private class ChunkComparator implements Comparator<Chunk> {
        @Override
        public int compare(Chunk o1, Chunk o2) {
            if(o1.getChunkPosition().getX() < o2.getChunkPosition().getX() || o1.getChunkPosition().getY() < o2.getChunkPosition().getY()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
