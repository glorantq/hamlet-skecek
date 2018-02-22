package sk.accerek.hamlet.gui.elements;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import sk.accerek.hamlet.Hamlet;
import sk.accerek.hamlet.gui.GuiElement;
import sk.accerek.hamlet.render.Anchor;

@AllArgsConstructor
@RequiredArgsConstructor
public class GuiTexture implements GuiElement {
    private final @NonNull SpriteBatch spriteBatch = Hamlet.get().getSpriteBatch();

    private final @Getter Texture texture;
    private final Anchor horizontalAnchor;
    private final Anchor verticalAnchor;
    private @Getter Vector2 position = new Vector2(0, 0);
    private final Vector2 renderPosition = position;
    private @Getter Vector2 size = new Vector2(0, 0);

    public GuiTexture(Texture texture, Anchor horizontalAnchor, Anchor verticalAnchor, Vector2 position) {
        this.texture = texture;
        this.horizontalAnchor = horizontalAnchor;
        this.verticalAnchor = verticalAnchor;
        this.position = position;
        this.size = new Vector2(texture.getWidth(), texture.getHeight());
    }

    @Override
    public void show() {
        if(this.size.x == 0 || this.size.y == 0) {
            this.size = new Vector2(texture.getWidth(), texture.getHeight());
        }

        adjustPosition();
    }

    @Override
    public void render() {
        spriteBatch.draw(texture, renderPosition.x, renderPosition.y, size.x, size.y);
    }

    private void adjustPosition() {
        float x = position.x;
        float y = position.y;

        System.out.println(x + " " + y + "  " + size.x + "  " + size.y);

        switch (horizontalAnchor) {
            case CENTRE:
                x -= size.x / 2;
                break;
            case RIGHT:
                x -= size.x;
                break;
        }

        switch (verticalAnchor) {
            case CENTRE:
                y -= size.y / 2;
                break;
            case TOP:
                y -= size.y;
                break;
        }

        System.out.println(x + " " + y + "  " + size.x + "  " + size.y);

        this.renderPosition.set(x, y);
    }

    public void setSize(Vector2 size) {
        this.size = size;
        adjustPosition();
    }

    public void setSize(float width, float height) {
        setSize(new Vector2(width, height));
    }

    public void setPosition(Vector2 position) {
        this.position = position;
        adjustPosition();
    }

    public void setPosition(float x, float y) {
        setPosition(new Vector2(x, y));
    }
}
