package sk.accerek.hamlet.events;

import lombok.Data;

@Data
public class LoadingUpdateEvent {
    private final int updateID;
    private final String updateText;
    private final int percentage;
}
