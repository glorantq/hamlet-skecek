package sk.accerek.hamlet.input;

import com.badlogic.gdx.InputProcessor;
import com.google.common.eventbus.EventBus;
import lombok.NonNull;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.events.input.KeyTypedEvent;
import sk.accerek.hamlet.events.input.ScrolledEvent;

public class InputHandler implements InputProcessor {
    private static InputHandler INSTANCE;

    public static InputHandler get() {
        if(INSTANCE == null) {
            INSTANCE = new InputHandler();
        }

        return INSTANCE;
    }

    private final @NonNull EventBus eventBus;

    private InputHandler() {
        eventBus = Hamlet.get().getEventBus();
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        eventBus.post(new KeyTypedEvent(character));
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        eventBus.post(new ScrolledEvent(amount));
        return true;
    }
}
