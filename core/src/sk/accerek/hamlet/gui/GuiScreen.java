package sk.accerek.hamlet.gui;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.NonNull;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.events.input.ClickEvent;
import sk.accerek.hamlet.events.input.KeyTypedEvent;
import sk.accerek.hamlet.events.input.ScrolledEvent;
import sk.accerek.hamlet.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiScreen implements Screen {
    private final List<GuiElement> elements = new ArrayList<>();
    protected final @NonNull EventBus eventBus;

    public GuiScreen() {
        eventBus = Hamlet.get().getEventBus();
        eventBus.register(this);
    }

    @Override
    public void render(float delta) {
        synchronized (elements) {
            elements.forEach(GuiElement::render);
        }
    }

    @Subscribe public void onClick(ClickEvent event) {
        synchronized (elements) {
           elements.forEach(element -> element.onClick(event.getX(), event.getY(), event.getButton()));
        }
    }

    @Subscribe public void onKeyTyped(KeyTypedEvent event) {
        synchronized (elements) {
            for(GuiElement element : elements) {
                element.onKeyTyped(event.getCharacter());
            }
        }
    }

    @Subscribe public void onScrolled(ScrolledEvent event) {
        synchronized (elements) {
            elements.forEach(element -> element.onScroll(event.getAmount()));
        }
    }

    @Override
    public void dispose() {
        eventBus.unregister(this);

        synchronized (elements) {
            elements.forEach(GuiElement::dispose);
        }
    }

    protected void addElement(GuiElement element) {
        synchronized (elements) {
            elements.add(element);
        }

        element.show();
    }

    protected void removeElement(GuiElement element) {
        synchronized (elements) {
            elements.remove(element);
        }

        element.dispose();
    }
}
