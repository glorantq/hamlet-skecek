package sk.accerek.hamlet.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.Hamlet;

import java.util.concurrent.TimeUnit;

public class TextureProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AssetManager assetManager;
    private final Cache<String, Texture> textureCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(150)
            .build();

    private final @Getter Texture backupTexture;

    public TextureProvider() {
        this.assetManager = Hamlet.get().getAssetManager();

        Pixmap missingPixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        missingPixmap.setColor(0, 0, 0, 1);
        missingPixmap.fillRectangle(0, 0, 32, 32);
        missingPixmap.fillRectangle(32, 32, 32, 32);
        missingPixmap.setColor(1, 0, 1, 1);
        missingPixmap.fillRectangle(32, 0, 32, 32);
        missingPixmap.fillRectangle(0, 32, 32, 32);
        backupTexture = new Texture(missingPixmap);
    }

    public @NonNull Texture getTexture(String key) {
        if(assetManager.isLoaded(key)) {
            return assetManager.get(key);
        }

        Texture cached = textureCache.getIfPresent(key);
        if(cached != null) {
            return cached;
        }

        FileHandle handle = Gdx.files.internal(key);
        if(handle.exists()) {
            Texture texture = new Texture(handle);
            textureCache.put(key, texture);
            logger.info("Loading \"{}\" into the cache!", key);
            return texture;
        }

        logger.error("Invalid texture: {}", key);
        return backupTexture;
    }
}
