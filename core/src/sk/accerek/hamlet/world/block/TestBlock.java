package sk.accerek.hamlet.world.block;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

public class TestBlock extends Block {

    public TestBlock() {
        useStaticTexture("j.png");
    }

    @Override
    public void create() {

    }

    @Override
    public boolean hasPhysics() {
        return true;
    }

    @Override
    public Shape getShape() {
        PolygonShape polygonShape = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(64, 0);
        vertices[1] = new Vector2(110, 32);
        vertices[2] = new Vector2(64, 64);
        vertices[3] = new Vector2(18, 32);
        polygonShape.set(vertices);
        return polygonShape;
    }
}
