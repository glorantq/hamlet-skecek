package sk.accerek.hamlet.input;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.common.eventbus.EventBus;
import lombok.NonNull;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.events.input.ClickEvent;

public class GestureHandler implements GestureDetector.GestureListener {
    private static GestureHandler INSTANCE;

    public static GestureHandler get() {
        if(INSTANCE == null) {
            INSTANCE = new GestureHandler();
        }

        return INSTANCE;
    }

    private final @NonNull EventBus eventBus;

    private GestureHandler() {
        this.eventBus = Hamlet.get().getEventBus();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        Viewport viewport = Hamlet.get().getViewport();
        Vector2 unprojected = viewport.unproject(new Vector2(x, y));
        eventBus.post(new ClickEvent(unprojected.x, unprojected.y, button));
        return true;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}
