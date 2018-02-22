package sk.accerek.hamlet.world.storage;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.flowpowered.nbt.*;
import com.flowpowered.nbt.stream.NBTInputStream;
import com.flowpowered.nbt.stream.NBTOutputStream;
import lombok.Cleanup;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.accerek.hamlet.world.base.Chunk;
import sk.accerek.hamlet.world.base.ChunkProvider;
import sk.accerek.hamlet.world.base.Pair;
import sk.accerek.hamlet.world.block.Block;
import sk.accerek.hamlet.world.block.BlockType;
import sk.accerek.hamlet.world.tile.Tile;
import sk.accerek.hamlet.world.tile.TileType;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NbtChunkProvider implements ChunkProvider {
    private final String worldName;
    private final FileHandle rootDirectory;
    private final FileHandle chunksDirectory;

    private Logger logger;

    public NbtChunkProvider(String worldName, Files.FileType fileType) {
        this.worldName = worldName;

        rootDirectory = Gdx.files.getFileHandle("worlds/" + worldName + "/", fileType);
        chunksDirectory = rootDirectory.child("chunks");

        logger = LoggerFactory.getLogger("NbtChunkProvider{" + worldName + "}");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Chunk> provide(int x, int y) {
        FileHandle chunkFileHandle = chunksDirectory.child("chunk." + x + "." + y + ".skacc");
        if(!chunkFileHandle.exists()) {
            return Optional.of(new Chunk(new Pair<>(x, y)));
        }

        InputStream chunkFileStream = chunkFileHandle.read();
        Tag rootTag;
        try {
            @Cleanup NBTInputStream nbtInputStream = new NBTInputStream(chunkFileStream);
            rootTag = nbtInputStream.readTag();
        } catch (Exception e) {
            logger.error("Failed to read chunk {" + x + ", " + y + "}", e);
            return Optional.empty();
        }

        if(rootTag.getType() != TagType.TAG_COMPOUND) {
            chunkLoadingError("root tag isn't a compound tag", x, y);
            return Optional.empty();
        }

        CompoundTag compoundRootTag = (CompoundTag) rootTag;

        CompoundMap data = compoundRootTag.getValue();
        if(!data.containsKey("blocks")) {
            chunkLoadingError("missing blocks tag", x, y);
            return Optional.empty();
        }

        if(!data.containsKey("tiles")) {
            chunkLoadingError("missing tiles tag", x, y);
            return Optional.empty();
        }

        List<BlockData> loadedBlockData = new ArrayList<>();
        List<TileData> loadedTileData = new ArrayList<>();

        Tag blocksTag0 = data.get("blocks");
        if(blocksTag0.getType() != TagType.TAG_LIST) {
            chunkLoadingError("blocks tag isn't a list", x, y);
            return Optional.empty();
        }

        if(((ListTag) blocksTag0).getElementType() != CompoundTag.class) {
            chunkLoadingError("blocks list doesn't store compound tags", x, y);
            return Optional.empty();
        }

        ListTag<CompoundTag> blocksTag = (ListTag<CompoundTag>) blocksTag0;
        for(CompoundTag blockTag : blocksTag.getValue()) {
            CompoundMap blockMap = blockTag.getValue();

            if(!blockMap.containsKey("type")) {
                chunkLoadingError("block tag doesn't have a type", x, y);
                continue;
            }

            if(!blockMap.containsKey("x") || !blockMap.containsKey("y")) {
                chunkLoadingError("block tag doesn't have coordinates", x, y);
                continue;
            }

            Tag typeTag = blockMap.get("type");
            Tag xTag = blockMap.get("x");
            Tag yTag = blockMap.get("y");

            if(typeTag.getType() != TagType.TAG_STRING || xTag.getType() != TagType.TAG_BYTE || yTag.getType() != TagType.TAG_BYTE) {
                chunkLoadingError("failed to assert tag types in block", x, y);
                continue;
            }

            String typeName = ((StringTag) typeTag).getValue();
            byte bX = ((ByteTag) xTag).getValue();
            byte bY = ((ByteTag) yTag).getValue();

            BlockType blockType;

            try {
                blockType = BlockType.valueOf(BlockType.class, typeName);
            } catch (IllegalArgumentException e) {
                blockType = null;
            }

            if(blockType == null) {
                chunkLoadingError("invalid block type: " + typeName, x, y);
                continue;
            }

            loadedBlockData.add(new BlockData(blockType, (int) bX, (int) bY));
        }

        logger.debug("Loaded {} blocks for chunk {};{}", loadedBlockData.size(), x, y);

        Tag tilesTag0 = data.get("tiles");
        if(tilesTag0.getType() != TagType.TAG_LIST) {
            chunkLoadingError("tiles tag isn't a list", x, y);
            return Optional.empty();
        }

        if(((ListTag) tilesTag0).getElementType() != CompoundTag.class) {
            chunkLoadingError("tiles list doesn't store compound tags", x, y);
            return Optional.empty();
        }

        ListTag<CompoundTag> tilesTag = (ListTag<CompoundTag>) tilesTag0;

        for(CompoundTag tileTag : tilesTag.getValue()) {
            CompoundMap tileMap = tileTag.getValue();

            if(!tileMap.containsKey("type")) {
                chunkLoadingError("tile tag doesn't have a type", x, y);
                continue;
            }

            if(!tileMap.containsKey("x") || !tileMap.containsKey("y")) {
                chunkLoadingError("tile tag doesn't have coordinates", x, y);
                continue;
            }

            Tag typeTag = tileMap.get("type");
            Tag xTag = tileMap.get("x");
            Tag yTag = tileMap.get("y");

            if(typeTag.getType() != TagType.TAG_STRING || xTag.getType() != TagType.TAG_BYTE || yTag.getType() != TagType.TAG_BYTE) {
                chunkLoadingError("failed to assert tag types in tile", x, y);
                continue;
            }

            String typeName = ((StringTag) typeTag).getValue();
            byte bX = ((ByteTag) xTag).getValue();
            byte bY = ((ByteTag) yTag).getValue();

            TileType tileType;

            try {
                tileType = TileType.valueOf(TileType.class, typeName);
            } catch (IllegalArgumentException e) {
                tileType = null;
            }

            if(tileType == null) {
                chunkLoadingError("invalid tile type: " + typeName, x, y);
                continue;
            }

            loadedTileData.add(new TileData(tileType, (int) bX, (int) bY));
        }

        Chunk chunk = new Chunk(new Pair<>(x, y));
        for(TileData tileData : loadedTileData) {
            chunk.setTile(tileData.getTileType(), tileData.getX(), tileData.getY());
        }

        for(BlockData blockData : loadedBlockData) {
            chunk.setBlock(blockData.getBlockType(), blockData.getX(), blockData.getY());
        }

        return Optional.of(chunk);
    }

    private void chunkLoadingError(String message, int x, int y) {
        logger.error("Failed to load chunk {};{}: {}", x, y, message);
    }

    @Override
    public void unload(Chunk chunk) {
        FileHandle chunkFileHandle = chunksDirectory.child("chunk." + chunk.getChunkPosition().getX() + "." + chunk.getChunkPosition().getY() + ".skacc");

        if(chunkFileHandle.type() == Files.FileType.Internal) {
            logger.warn("Can't save to internal storage, chunk save for {} aborted!", chunk.getChunkPosition());
            return;
        }

        CompoundTag rootCompoundTag = new CompoundTag("root", new CompoundMap());
        CompoundMap rootData = rootCompoundTag.getValue();

        ArrayList<CompoundTag> blocksList = new ArrayList<>();

        for(Block[] column : chunk.getBlocks()) {
            for(Block block : column) {
                if(block == null) {
                    continue;
                }

                CompoundTag blockTag = new CompoundTag("", new CompoundMap());
                CompoundMap blockMap = blockTag.getValue();

                blockMap.put(new StringTag("type", block.getBlockType().name()));
                blockMap.put(new ByteTag("x", block.getPositionInChunk().getX().byteValue()));
                blockMap.put(new ByteTag("y", block.getPositionInChunk().getY().byteValue()));

                blocksList.add(blockTag);
            }
        }

        ListTag<CompoundTag> blockListTag = new ListTag<>("blocks", CompoundTag.class, blocksList);

        ArrayList<CompoundTag> tilesList = new ArrayList<>();

        for(Tile[] column : chunk.getTiles()) {
            for(Tile tile : column) {
                if(tile == null) {
                    continue;
                }

                CompoundTag tileTag = new CompoundTag("", new CompoundMap());
                CompoundMap tileMap = tileTag.getValue();

                tileMap.put(new StringTag("type", tile.getTileType().name()));
                tileMap.put(new ByteTag("x", tile.getPositionInChunk().getX().byteValue()));
                tileMap.put(new ByteTag("y", tile.getPositionInChunk().getY().byteValue()));

                tilesList.add(tileTag);
            }
        }

        ListTag<CompoundTag> tileListTag = new ListTag<>("tiles", CompoundTag.class, tilesList);

        rootData.put(blockListTag);
        rootData.put(tileListTag);

        OutputStream outputStream = chunkFileHandle.write(false);

        try {
            @Cleanup NBTOutputStream nbtOutputStream = new NBTOutputStream(outputStream);
            nbtOutputStream.writeTag(rootCompoundTag);
        } catch (Exception e) {
            logger.error("Failed to save chunk " + chunk.getChunkPosition(), e);
        }
    }

    @Data
    private class BlockData {
        private final BlockType blockType;
        private final int x;
        private final int y;
    }

    @Data
    private class TileData {
        private final TileType tileType;
        private final int x;
        private final int y;
    }

    /*
    root (TAG_COMPOUND) {
        blocks(TAG_LIST) {
            _(TAG_COMPOUND) {
                type (TAG_STRING)
                x (TAG_BYTE)
                y (TAG_BYTE)
            }
        }

        tiles(TAG_LIST) {
            _(TAG_COMPOUND) {
                type (TAG_STRING)
                x (TAG_BYTE)
                y (TAG_BYTE)
            }
        }
    }
     */
}
