package sk.accerek.hamlet.gui;

public interface GuiElement {
    default void show() {}
    void render();
    default void dispose() {}

    default void onClick(float x, float y, int button) {}
    default void onScroll(int amount) {}
    default void onKeyTyped(char character) {}
}
