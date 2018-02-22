package sk.accerek.hamlet;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import lombok.Getter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.events.LoadingUpdateEvent;
import sk.accerek.hamlet.events.ScreenChangeEvent;
import sk.accerek.hamlet.input.GestureHandler;
import sk.accerek.hamlet.input.InputHandler;
import sk.accerek.hamlet.modules.HamletModule;
import sk.accerek.hamlet.modules.IModule;
import sk.accerek.hamlet.render.Anchor;
import sk.accerek.hamlet.render.TextureProvider;
import sk.accerek.hamlet.render.text.TextRenderer;
import sk.accerek.hamlet.render.text.font.FontStyle;
import sk.accerek.hamlet.screens.LoadingScreen;
import sk.accerek.hamlet.screens.TestScreen;
import sk.accerek.hamlet.world.storage.WorldLoaderException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Hamlet extends Game {
    private static Hamlet INSTANCE;
    private static Logger logger = LoggerFactory.getLogger(Hamlet.class);
    public static Hamlet get() {
        return INSTANCE;
    }
    public static Hamlet initialise(Platform platform) {
        if(INSTANCE != null) {
            logger.error("Tried to call Hamlet#initialise() multiple times!");
            return get();
        }

        INSTANCE = new Hamlet(platform);
        return get();
    }

	public static final Vector2 WINDOW_SIZE = new Vector2(1280, 720);
    public static Vector2 VIEW_SIZE = WINDOW_SIZE;
    private final Platform platform;

    private @Getter FitViewport viewport;
    private @Getter SpriteBatch spriteBatch;
    private @Getter OrthographicCamera camera;
    private @Getter OrthographicCamera uiCamera;
    private @Getter AssetManager assetManager;
    private @Getter TextureProvider textureProvider;

    private @Getter TextRenderer textRenderer;

    private @Getter EventBus eventBus;

    private ImmutableList<IModule> modules;

    private Hamlet(Platform platform) {
        this.platform = platform;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down...");

            for(IModule module : modules) {
                module.destroy();
            }
        }, "Shutdown Handler Thread"));
    }

	@Override
	public void create() {
        eventBus = new EventBus("main-event-bus");

        logger.info("Starting game...");

        Reflections reflections = new Reflections("sk.accerek.hamlet.modules");
        Set<Class<? extends IModule>> classes = reflections.getSubTypesOf(IModule.class);
        List<IModule> loadedModules = new ArrayList<>();
        for(Class<? extends IModule> moduleClass : classes) {
            if(!moduleClass.isAnnotationPresent(HamletModule.class)) {
                logger.warn("Module {} is not annotated with @HamletModule, skipping...");
                continue;
            }

            HamletModule annotation = moduleClass.getAnnotation(HamletModule.class);

            try {
                loadedModules.add(moduleClass.getConstructor().newInstance());
                logger.debug("Loaded module {}", annotation.name());
            } catch (Exception e) {
                logger.error("Failed to instantiate " + annotation.name(), e);
            }
        }
        modules = ImmutableList.copyOf(loadedModules);

        for(IModule module : modules) {
            module.create();
        }

        logger.debug("Loaded {} modules!", modules.size());

        camera = new OrthographicCamera(WINDOW_SIZE.x, WINDOW_SIZE.y);
        uiCamera = new OrthographicCamera(WINDOW_SIZE.x, WINDOW_SIZE.y);
        uiCamera.setToOrtho(false);
        uiCamera.update();
        viewport = new FitViewport(WINDOW_SIZE.x, WINDOW_SIZE.y, camera);

        spriteBatch = new SpriteBatch();
        assetManager = new AssetManager();

        textRenderer = new TextRenderer(spriteBatch);

        textureProvider = new TextureProvider();

        camera.position.set(WINDOW_SIZE.x / 2, WINDOW_SIZE.y / 2, 0);
        uiCamera.position.set(WINDOW_SIZE.x / 2, WINDOW_SIZE.y / 2, 0);

        Gdx.input.setInputProcessor(new InputMultiplexer(InputHandler.get(), new GestureDetector(GestureHandler.get())));

        setScreen(new TestScreen());
    }

	public void renderUi(Runnable uiTask) {
        spriteBatch.flush();
        viewport.setCamera(uiCamera);
        spriteBatch.setProjectionMatrix(uiCamera.combined);
        viewport.apply();

        uiTask.run();

        spriteBatch.flush();
        viewport.setCamera(camera);
        spriteBatch.setProjectionMatrix(camera.combined);
        viewport.apply();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.setProjectionMatrix(camera.combined);
        spriteBatch.begin();
        super.render();
        spriteBatch.end();

        platform.nativeUpdate();

        if(Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            if(Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode((int) WINDOW_SIZE.x, (int) WINDOW_SIZE.y);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        Gdx.graphics.setTitle("Hamlet - " + Gdx.graphics.getFramesPerSecond() + " FPS" + (Gdx.graphics.getFramesPerSecond() >= 60 ? " (Kollay-ready)" : ""));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);

        uiCamera.viewportWidth = width;
        uiCamera.viewportHeight = height;
        uiCamera.position.set(WINDOW_SIZE.x / 2, WINDOW_SIZE.y / 2, 0);
        uiCamera.update();

        logger.debug("Resizing viewport: " + width + "x" + height);
        super.resize(width, height);

        VIEW_SIZE = new Vector2(width, height);
    }

    @Override
    public void setScreen(Screen screen) {
        logger.debug("Changing screen to an instance of " + screen.getClass().getCanonicalName());
        eventBus.post(new ScreenChangeEvent(screen.getClass()));
        super.setScreen(screen);
    }

    public boolean isFpsCapped() {
        return platform.isFpsCapped();
    }

    public boolean isDebugEnabled() {
        return platform.isDebugEnabled();
    }
}
