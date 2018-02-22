package sk.accerek.hamlet.world;

import sk.accerek.hamlet.world.base.Chunk;
import sk.accerek.hamlet.world.base.ChunkProvider;
import sk.accerek.hamlet.world.base.Pair;

import java.util.Optional;

public class TestChunkProvider implements ChunkProvider {
    @Override
    public Optional<Chunk> provide(int x, int y) {
        return Optional.of(new Chunk(new Pair<>(x, y)));
    }

    @Override
    public void unload(Chunk chunk) {

    }
}
