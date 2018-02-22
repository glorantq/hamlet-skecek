package sk.accerek.hamlet.world.block;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import lombok.Getter;
import sk.accerek.hamlet.world.base.Pair;
import sk.accerek.hamlet.world.tile.Tile;
import sk.accerek.hamlet.world.tile.TileType;

import java.util.Random;

public class TowerWallBlock extends Block {
    private @Getter MossState mossState = MossState.REGULAR;

    @Override
    public void create() {
        Pair<Integer, Integer> positionInChunk = getPositionInChunk();
        Block blockBelow = getChunk().getBlock(positionInChunk.getX(), positionInChunk.getY() - 1);
        Block blockBelow0 = getChunk().getBlock(positionInChunk.getX(), positionInChunk.getY() - 3);
        Tile tileBelow = getChunk().getTile(positionInChunk.getX(), positionInChunk.getY() - 1);

        if (blockBelow == null && tileBelow != null && tileBelow.getTileType() == TileType.GRASS) {
            useStaticTexture("tower_wall/mossy_tower_wall_1.png", "tower_wall/mossy_tower_wall_2.png");
            mossState = MossState.MOSSY;
            return;
        }

        if (!(blockBelow instanceof TowerWallBlock)) {
            useStaticTexture("tower_wall/tower_wall_1.png", "tower_wall/tower_wall_2.png");
            mossState = MossState.REGULAR;
            return;
        }

        Random random = new Random();
        TowerWallBlock towerWallBlock = (TowerWallBlock) blockBelow;

        switch (towerWallBlock.getMossState()) {
            case MOSSY:
                int n = random.nextInt(2);
                if (n == 1) {
                    useStaticTexture("tower_wall/rarely_mossy_tower_wall_1.png", "tower_wall/rarely_mossy_tower_wall_2.png");
                    mossState = MossState.BARELY_MOSSY;
                } else {
                    useStaticTexture("tower_wall/mossy_tower_wall_1.png", "tower_wall/mossy_tower_wall_2.png");
                    mossState = MossState.MOSSY;
                }
                return;

            case BARELY_MOSSY:
                useStaticTexture("tower_wall/tower_wall_1.png", "tower_wall/tower_wall_2.png");
                mossState = MossState.REGULAR;
                return;
        }


        if(blockBelow0 != null && blockBelow0 instanceof TowerWallBlock) {
            TowerWallBlock towerWallBlock0 = (TowerWallBlock) blockBelow0;
            if(towerWallBlock0.getMossState() == MossState.BARELY_MOSSY || towerWallBlock0.getMossState() == MossState.MOSSY) {
                useStaticTexture("tower_wall/tower_wall_1.png", "tower_wall/tower_wall_2.png");
                mossState = MossState.REGULAR;
                return;
            }
        }

        useStaticTexture("tower_wall/tower_wall_1.png", "tower_wall/tower_wall_2.png");
    }

    @Override
    public boolean hasPhysics() {
        return true;
    }

    @Override
    public Shape getShape() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(32, 32, new Vector2(32, 32), 0);
        return shape;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "mossState=" + mossState.name() +
                '}';
    }

    private enum MossState {
        REGULAR, BARELY_MOSSY, MOSSY
    }
}
