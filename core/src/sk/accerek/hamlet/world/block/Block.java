package sk.accerek.hamlet.world.block;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Shape;
import lombok.Getter;
import lombok.Setter;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.render.TextureProvider;
import sk.accerek.hamlet.world.base.Chunk;
import sk.accerek.hamlet.world.base.Pair;
import sk.accerek.hamlet.world.tile.TileType;

import java.util.Random;

public abstract class Block {
    private final Hamlet hamlet = Hamlet.get();
    private final TextureProvider textureProvider = hamlet.getTextureProvider();

    private Texture texture = textureProvider.getBackupTexture();
    private Animation<TextureRegion> animation = null;

    private @Getter @Setter Pair<Integer, Integer> positionInChunk;
    private @Getter @Setter Chunk chunk;
    private final @Getter BlockType blockType = BlockType.getTypeFor(getClass());

    private @Getter @Setter Body createdBox2DBody;

    private float stateTime = 0f;

    public void render(SpriteBatch spriteBatch) {
        Pair<Integer, Integer> pixelPosition = getPixelPosition();

        float width, height;

        if (animation != null) {
            stateTime += Gdx.graphics.getDeltaTime();
            TextureRegion region = animation.getKeyFrame(stateTime, true);

            width = 2 * Math.round(region.getRegionWidth() / 2);
            height = 2 * Math.round(region.getRegionHeight() / 2);

            spriteBatch.draw(region, pixelPosition.getX(), pixelPosition.getY(), width, height);

            return;
        }

        width = 2 * Math.round(texture.getWidth() / 2);
        height = 2 * Math.round(texture.getHeight() / 2);

        spriteBatch.draw(texture, pixelPosition.getX(), pixelPosition.getY(), width, height);
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

    void useStaticTexture(String... textureNames) {
        Texture texture;
        if (textureNames.length == 1) {
            texture = textureProvider.getTexture("blocks/" + textureNames[0]);
        } else {
            Random random = new Random();
            texture = textureProvider.getTexture("blocks/" + textureNames[random.nextInt(textureNames.length)]);
        }

        if (texture == null) {
            texture = textureProvider.getBackupTexture();
        }

        this.texture = texture;
    }

    void useAnimation(String tileSet, int tileSize, float frameTime) {
        Texture tileSetTexture = textureProvider.getTexture("blocks/" + tileSet);

        TextureRegion[][] frames = TextureRegion.split(tileSetTexture, tileSize, tileSize);
        TextureRegion[] frames0 = new TextureRegion[frames.length * frames[0].length];
        int index = 0;
        for (TextureRegion[] frame : frames) {
            for (TextureRegion aFrame : frame) {
                frames0[index++] = aFrame;
            }
        }

        this.animation = new Animation<>(frameTime, frames0);
    }

    public abstract boolean hasPhysics();

    public abstract Shape getShape();

    public abstract void create();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "positionInChunk=" + positionInChunk +
                ", position=" + getWorldPosition() +
                ", blockType=" + blockType +
                '}';
    }
}
