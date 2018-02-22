package sk.accerek.hamlet.screens;

import box2dLight.PointLight;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.google.common.eventbus.EventBus;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.gui.GuiScreen;
import sk.accerek.hamlet.modules.base.DiscordModule;
import sk.accerek.hamlet.render.Anchor;
import sk.accerek.hamlet.render.text.TextRenderer;
import sk.accerek.hamlet.render.text.font.FontStyle;
import sk.accerek.hamlet.world.TestChunkProvider;
import sk.accerek.hamlet.world.base.Chunk;
import sk.accerek.hamlet.world.base.Pair;
import sk.accerek.hamlet.world.base.World;
import sk.accerek.hamlet.world.block.Block;
import sk.accerek.hamlet.world.block.BlockType;
import sk.accerek.hamlet.world.storage.NbtChunkProvider;
import sk.accerek.hamlet.world.storage.WorldLoaderException;
import sk.accerek.hamlet.world.tile.Tile;
import sk.accerek.hamlet.world.tile.TileType;

import java.util.Properties;
import java.util.Random;

public class TestScreen extends GuiScreen {
    private World world = new World(new NbtChunkProvider("test", Files.FileType.Internal));
    private SpriteBatch spriteBatch = Hamlet.get().getSpriteBatch();
    private Camera camera = Hamlet.get().getCamera();
    private TextRenderer textRenderer = Hamlet.get().getTextRenderer();
    private EventBus eventBus = Hamlet.get().getEventBus();

    private float x = 0;
    private float y = 0;

    private PointLight pointLight;

    private NinePatchDrawable npd;

    public TestScreen() {
        world.loadChunk(new Pair<>(0, 0));

        String version;
        Properties properties = new Properties();

        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("version.properties"));
            version = properties.getProperty("version");
        } catch (Exception ignored) {
            version = "?";
        }

        DiscordModule.DiscordPresenceUpdate presenceUpdate = DiscordModule.DiscordPresenceUpdate.builder()
                .largeImageKey("small_crown")
                .largeImageText("Hamlet: The Game")
                .details("Beta-testing" + (version != null ? " (build " + version + ")" : ""))
                .withTimestamp(false).build();

        eventBus.post(presenceUpdate);

        NinePatch ninePatch = new NinePatch(new Texture("test.png"), 3, 3, 3, 3);
        npd = new NinePatchDrawable(ninePatch);

        new PointLight(world.getRayHandler(), 500, Color.WHITE, 30 * Chunk.GRID_SIZE, 0, 0);

        world.setAmbientLight(.4f, .4f, .6f, .4f);

        x = Hamlet.WINDOW_SIZE.x / 2;
        y = Hamlet.WINDOW_SIZE.y / 2;

        pointLight = new PointLight(world.getRayHandler(), 400, Color.WHITE, 10 * Chunk.GRID_SIZE, x, y);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        world.render(spriteBatch);

        float speed = 500;

        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            speed *= 5;
        }

        if(Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
            speed *= 20;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            y += speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            y -= speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            x -= speed * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            x += speed * delta;
        }

        camera.position.set(x, y, 0);
        camera.update();

        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Vector3 pixelPos = camera.unproject(mousePos);

        Pair<Integer, Integer> tilePos = world.pixelToGridPosition(pixelPos.x, pixelPos.y);
        Tile tile = world.getTile(tilePos.getX(), tilePos.getY());
        Block block = world.getBlock(tilePos.getX(), tilePos.getY());

        pointLight.setPosition(pixelPos.x, pixelPos.y);

        String text = null;

        if(block != null) {
            text = block.toString();
        } else if(tile != null) {
            text = tile.toString();
        }

        if (text != null) {
            String finalText = text;
            Hamlet.get().renderUi(() -> {
                Vector3 uP = Hamlet.get().getUiCamera().unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                Vector2 drawPos = new Vector2(uP.x, uP.y);
                drawPos.add(15, 15);
                Vector2 size = textRenderer.getPixelSize("pixel", 35, false, 0, FontStyle.REGULAR, finalText);
                size = size.add(20, 20);
                if(Gdx.input.getX() + size.x > Hamlet.VIEW_SIZE.x) {
                    drawPos = drawPos.sub(size.x, 0);
                }
                if(Gdx.input.getY() - size.y < 0) {
                    drawPos.sub(0, size.y + 15);
                }
                npd.draw(spriteBatch, drawPos.x - 10, drawPos.y - 10, size.x, size.y);
                textRenderer.drawText("pixel", 35, FontStyle.REGULAR, Anchor.LEFT, Anchor.BOTTOM, Color.WHITE, drawPos.x, drawPos.y, finalText);
            });
        }

        if (Gdx.input.justTouched()) {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                world.removeTile(tilePos.getX(), tilePos.getY());
            } else {
                world.setTile(TileType.ROCKY_FLOOR, tilePos.getX(), tilePos.getY());
            }
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            Random random = new Random();
            int x = random.nextInt(100000);
            x *= (random.nextBoolean() ? -1 : 1);

            int y = random.nextInt(100000);
            y *= (random.nextBoolean() ? -1 : 1);

            Pair<Integer, Integer> pixelPos0 = world.gridToPixelPosition(new Pair<>(x, y));
            this.x = pixelPos0.getX();
            this.y = pixelPos0.getY();

            world.setTile(TileType.ROCKY_FLOOR, pixelPos0.getX(), pixelPos0.getY());
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            int x = tilePos.getX();
            int y = tilePos.getY();

            for(int i = -5; i < 5; i++) {
                for(int j = -5; j < 5; j++) {
                    int x1 = x + i;
                    int y1 = y + j;

                    double d = Math.sqrt(Math.pow((x - x1), 2) + Math.pow((y - y1), 2));

                    if(d <= 4) {
                        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                            world.removeTile(x1, y1);
                        } else {
                            world.setTile(TileType.ROCKY_FLOOR, x1, y1);
                        }
                    }
                }
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.G) && Gdx.input.justTouched()) {
            world.setBlock(BlockType.TOWER_WALL, tilePos.getX(), tilePos.getY());
        }

        if(Hamlet.get().isDebugEnabled()) {
            Hamlet.get().renderUi(() -> {
                Vector3 screenCoords = new Vector3(Hamlet.WINDOW_SIZE.x / 2, Hamlet.WINDOW_SIZE.y - 15, 0);
                String debugText = "Hamlet: The Game\n<" + Gdx.graphics.getFramesPerSecond() + " fps, " + Gdx.graphics.getGLVersion().getRendererString() + " OpenGL " + Gdx.graphics.getGLVersion().getMajorVersion() + "." + Gdx.graphics.getGLVersion().getMinorVersion() + " with " + Gdx.graphics.getType().name() + (Hamlet.get().isFpsCapped() ? " ! vSync !" : "") + ">";
                Vector2 size = textRenderer.getPixelSize("pixel", 30, true, (int) Hamlet.WINDOW_SIZE.x, FontStyle.REGULAR, debugText);
                npd.draw(spriteBatch, screenCoords.x - size.x / 2 - 10, screenCoords.y - size.y - 10, size.x + 20, size.y + 20);
                textRenderer.drawWrappedText("pixel", 30, (int) size.x, FontStyle.REGULAR, Anchor.CENTRE, Anchor.TOP, Align.center, Color.WHITE, screenCoords.x, screenCoords.y, debugText);
            });
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            Pair<Integer, Integer> chunkPos = world.getChunkForGridPosition(tilePos);
            world.unloadChunk(chunkPos);
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            world.unloadAllChunks();
        }
    }
}
