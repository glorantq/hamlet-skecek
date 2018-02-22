package sk.accerek.hamlet.world.tile;

import com.badlogic.gdx.graphics.Texture;
import lombok.Getter;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.render.TextureProvider;

public enum TileType {
    ROCKY_FLOOR("Rocky Floor", "rocky_floor.png"),
    GRASS("Grass", "grass/grass_1.png", "grass/grass_2.png", "grass/grass_3.png", "grass/grass_4.png", "grass/grass_5.png");

    private final @Getter String name;
    private final @Getter Texture[] gdxTextures;

    TileType(String name, String... textureNames) {
        this.name = name;

        TextureProvider textureProvider = Hamlet.get().getTextureProvider();
        this.gdxTextures = new Texture[textureNames.length];
        for(int i = 0; i < textureNames.length; i++) {
            this.gdxTextures[i] = textureProvider.getTexture("tiles/" + textureNames[i]);
        }
    }

    public int getId() {
        return this.ordinal();
    }

    public static TileType getById(int id) {
        if(id > TileType.values().length || id < 0) {
            return null;
        }

        return TileType.values()[id];
    }
}
