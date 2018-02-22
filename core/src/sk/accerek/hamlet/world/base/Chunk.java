package sk.accerek.hamlet.world.base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.world.block.Block;
import sk.accerek.hamlet.world.block.BlockType;
import sk.accerek.hamlet.world.tile.Tile;
import sk.accerek.hamlet.world.tile.TileType;

import java.lang.reflect.Array;

public class Chunk {
    public static final int BIT_SIZE = 4;
    public static final int CHUNK_SIZE = (int) Math.pow(BIT_SIZE, 2);
    public static final int GRID_SIZE = 64;
    public static final int CHUNK_PIXEL_SIZE = CHUNK_SIZE * GRID_SIZE;

    private final Logger logger;
    private final @Getter Pair<Integer, Integer> chunkPosition;

    private @Getter Tile[][] tiles;
    private @Getter Block[][] blocks;

    public Chunk(Pair<Integer, Integer> chunkPosition) {
        this.logger = LoggerFactory.getLogger("Chunk" + chunkPosition);
        this.chunkPosition = chunkPosition;
        this.tiles = new Tile[CHUNK_SIZE][CHUNK_SIZE];
        this.blocks = new Block[CHUNK_SIZE][CHUNK_SIZE];
    }

    public void render(SpriteBatch spriteBatch) {
        for(Tile[] row : tiles) {
            if(row == null) {
                continue;
            }

            for(Tile tile : row) {
                if(tile == null) {
                    continue;
                }

                if(!isPositionInView(tile.getPixelPosition())) {
                    continue;
                }

                tile.render(spriteBatch);
            }
        }

        for(int i = blocks.length - 1; i >= 0; i--) {
            Block[] row = getRow(blocks, i, Block[].class);
            if(row == null) {
                continue;
            }

            for(Block block : row) {
                if(block == null) {
                    continue;
                }

                if(!isPositionInView(block.getPixelPosition())) {
                    continue;
                }

                block.render(spriteBatch);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T[] getRow(T[][] array, int num, Class<T[]> clazz) {
        if(array.length == 0) {
            return (T[]) new Object[0];
        }
        T[] row = (T[]) Array.newInstance(clazz.getComponentType(), array[0].length);

        for(int i = 0; i < array.length; i++) {
            for(int j = 0; j < array[i].length; j++) {
                if(j == num) {
                    row[i] = array[i][j];
                    break;
                }
            }
        }

        return row;
    }

    private Vector3 projectCoordinates(Pair<Integer, Integer> pixelPosition) {
        return Hamlet.get().getCamera().project(new Vector3(pixelPosition.getX(), pixelPosition.getY(), 0));
    }

    private boolean isInView(Vector3 screenCoordinates) {
        return !(screenCoordinates.x + GRID_SIZE * 2 < 0 || screenCoordinates.x > Hamlet.VIEW_SIZE.x || screenCoordinates.y + GRID_SIZE * 2 < 0 || screenCoordinates.y > Hamlet.VIEW_SIZE.y);
    }

    private boolean isPositionInView(Pair<Integer, Integer> position) {
        return isInView(projectCoordinates(position));
    }

    public void setTile(TileType type, int xInChunk, int yInChunk) {
        if(xInChunk >= CHUNK_SIZE || yInChunk >= CHUNK_SIZE || xInChunk < 0 || yInChunk < 0) {
            return;
        }

        Tile tile = new Tile(type, this, new Pair<>(xInChunk, yInChunk));

        tiles[xInChunk][yInChunk] = tile;
    }

    public Tile getTile(int xInChunk, int yInChunk) {
        if(xInChunk >= CHUNK_SIZE || yInChunk >= CHUNK_SIZE || xInChunk < 0 || yInChunk < 0) {
            return null;
        }

        return tiles[xInChunk][yInChunk];
    }

    public void removeTile(int xInChunk, int yInChunk) {
        if(xInChunk >= CHUNK_SIZE || yInChunk >= CHUNK_SIZE || xInChunk < 0 || yInChunk < 0) {
            return;
        }

        tiles[xInChunk][yInChunk] = null;
    }

    public Block setBlock(BlockType blockType, int xInChunk, int yInChunk) {
        if(xInChunk >= CHUNK_SIZE || yInChunk >= CHUNK_SIZE || xInChunk < 0 || yInChunk < 0) {
            return null;
        }


        Block block;

        try {
            block = blockType.getClazz().getConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Failed to instantiate Block!" + e);
            return null;
        }

        block.setChunk(this);
        block.setPositionInChunk(new Pair<>(xInChunk, yInChunk));
        block.create();
        blocks[xInChunk][yInChunk] = block;

        return block;
    }

    public Block getBlock(int xInChunk, int yInChunk) {
        if(xInChunk >= CHUNK_SIZE || yInChunk >= CHUNK_SIZE || xInChunk < 0 || yInChunk < 0) {
            return null;
        }

        return blocks[xInChunk][yInChunk];
    }

    public void removeBlock(int xInChunk, int yInChunk) {
        if(xInChunk >= CHUNK_SIZE || yInChunk >= CHUNK_SIZE || xInChunk < 0 || yInChunk < 0) {
            return;
        }

        blocks[xInChunk][yInChunk] = null;
    }
}
