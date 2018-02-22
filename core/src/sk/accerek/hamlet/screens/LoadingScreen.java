package sk.accerek.hamlet.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.events.LoadingUpdateEvent;
import sk.accerek.hamlet.gui.GuiScreen;
import sk.accerek.hamlet.gui.elements.GuiTexture;
import sk.accerek.hamlet.render.Anchor;
import sk.accerek.hamlet.render.TextureProvider;
import sk.accerek.hamlet.render.text.TextRenderer;
import sk.accerek.hamlet.render.text.font.FontStyle;

import java.util.Optional;

public class LoadingScreen extends GuiScreen {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final @NonNull Hamlet hamlet = Hamlet.get();
    private final @NonNull SpriteBatch spriteBatch = hamlet.getSpriteBatch();
    private final @NonNull TextRenderer textRenderer = hamlet.getTextRenderer();
    private final @NonNull EventBus eventBus = hamlet.getEventBus();
    private final int updateListenerID;

    private Optional<LoadingUpdateEvent> lastLoadingEvent;

    public LoadingScreen(int id) {
        super();

        this.updateListenerID = id;

        eventBus.register(this);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        lastLoadingEvent.ifPresent(loadingUpdateEvent ->
                textRenderer.drawText(
                        "pixel",
                        48,
                        FontStyle.REGULAR,
                        Anchor.RIGHT,
                        Anchor.BOTTOM,
                        Color.WHITE,
                        Hamlet.WINDOW_SIZE.x - 10,
                        10,
                        loadingUpdateEvent.getUpdateText() + " (" + loadingUpdateEvent.getPercentage() + "%)"));
    }

    @Override
    public void dispose() {
        eventBus.unregister(this);
    }

    @Subscribe
    private void handleLoadingUpdate(LoadingUpdateEvent event) {
        logger.debug("Updating LoadingScreen #{}: {}", updateListenerID, event);
        lastLoadingEvent = Optional.of(event);
    }
}
