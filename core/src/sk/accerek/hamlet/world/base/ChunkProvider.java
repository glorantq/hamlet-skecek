package sk.accerek.hamlet.world.base;

import java.util.Optional;

public interface ChunkProvider {
    Optional<Chunk> provide(int x, int y);
    void unload(Chunk chunk);
}
