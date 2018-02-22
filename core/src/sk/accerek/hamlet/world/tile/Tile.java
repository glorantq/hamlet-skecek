package sk.accerek.hamlet.world.tile;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lombok.Getter;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.render.TextureProvider;
import sk.accerek.hamlet.world.base.Chunk;
import sk.accerek.hamlet.world.base.Pair;

import java.util.Random;

public class Tile {
    private @Getter Pair<Integer, Integer> positionInChunk;
    private @Getter Chunk chunk;
    private final @Getter TileType tileType;

    private @Getter Texture gdxTexture;

    public Tile(TileType type, Chunk chunk, Pair<Integer, Integer> positionInChunk) {
        this.chunk = chunk;
        this.positionInChunk = positionInChunk;
        this.tileType = type;

        Texture[] textures = type.getGdxTextures();
        if(textures.length > 1) {
            Random random = new Random();
            this.gdxTexture = textures[random.nextInt(textures.length)];
        } else {
            this.gdxTexture = textures[0];
        }
    }

    public void render(SpriteBatch spriteBatch) {
        Pair<Integer, Integer> position = getPixelPosition();

        spriteBatch.draw(gdxTexture, position.getX(), position.getY(), Chunk.GRID_SIZE, Chunk.GRID_SIZE);
    }

    public Pair<Integer, Integer> getPixelPosition() {
        Pair<Integer, Integer> chunkPosition = chunk.getChunkPosition();
        return new Pair<>(
                chunkPosition.getX() * (Chunk.CHUNK_SIZE * Chunk.GRID_SIZE) + positionInChunk.getX() * Chunk.GRID_SIZE,
                chunkPosition.getY() * (Chunk.CHUNK_SIZE * Chunk.GRID_SIZE) + positionInChunk.getY() * Chunk.GRID_SIZE);
    }

    public Pair<Integer, Integer> getWorldPosition() {
        Pair<Integer, Integer> chunkPosition = chunk.getChunkPosition();
        return new Pair<>(
                chunkPosition.getX() * Chunk.CHUNK_SIZE + positionInChunk.getX(),
                chunkPosition.getY() * Chunk.CHUNK_SIZE + positionInChunk.getY());
    }

    @Override
    public String toString() {
        return "Tile{" +
                "positionInChunk=" + positionInChunk +
                ", position=" + getWorldPosition() +
                ", tileType=" + tileType +
                '}';
    }
}
