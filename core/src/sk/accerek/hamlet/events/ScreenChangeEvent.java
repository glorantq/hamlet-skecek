package sk.accerek.hamlet.events;

import com.badlogic.gdx.Screen;
import lombok.Data;

@Data
public class ScreenChangeEvent {
    private final Class<? extends Screen> screen;
}
