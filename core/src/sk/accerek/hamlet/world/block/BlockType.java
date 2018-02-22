package sk.accerek.hamlet.world.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public enum BlockType {
    TEST(TestBlock.class), TOWER_WALL(TowerWallBlock.class);

    private final Class<? extends Block> clazz;

    public Class<? extends Block> getClazz() {
        return clazz;
    }

    public static BlockType getTypeFor(Class<? extends Block> clazz) {
        BlockType[] types = values();

        Optional<BlockType> optionalBlockType = Arrays.stream(types).filter(type -> type.clazz == clazz).findFirst();
        return optionalBlockType.orElse(null);
    }
}
