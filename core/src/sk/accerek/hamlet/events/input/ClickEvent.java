package sk.accerek.hamlet.events.input;

import lombok.Data;

@Data
public class ClickEvent {
    private final float x, y;
    private final int button;
}
