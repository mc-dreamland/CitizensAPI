package net.citizensnpcs.api.astar.pathfinder;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import net.citizensnpcs.api.util.BoundingBox;

public class ChunkBlockSource extends CachingChunkBlockSource<CompletableFuture<Chunk>> {

    public ChunkBlockSource(Location location, float radius) {
        super(location, radius);
    }

    public ChunkBlockSource(World world, int x, int z, float radius) {
        super(world, x, z, radius);
    }

    @Override
    protected BlockData getBlockData(CompletableFuture<Chunk> chunk, int x, int y, int z) {
        Chunk load = chunk.getNow(null);
        if (load == null)
            return null;
        return load.getBlock(x & 15, y, z & 15).getBlockData();
    }

    @Override
    protected CompletableFuture<Chunk> getChunkObject(int x, int z) {
        return world.getChunkAtAsync(x, z);
    }

    @Override
    protected BoundingBox getCollisionBox(CompletableFuture<Chunk> chunk, int x, int y, int z) {
        if (!SUPPORT_BOUNDING_BOX)
            return null;
        Chunk load = chunk.getNow(null);
        if (load == null)
            return BoundingBox.ONE.add(x, y, z);
        return BoundingBox.convert(load.getBlock(x & 15, y, z & 15).getBoundingBox());
    }

    @Override
    protected int getLightLevel(CompletableFuture<Chunk> chunk, int x, int y, int z) {
        Chunk load = chunk.getNow(null);
        if (load == null)
            return 0;
        return load.getBlock(x, y, z).getLightLevel();
    }

    @Override
    protected Material getType(CompletableFuture<Chunk> chunk, int x, int y, int z) {
        Chunk load = chunk.getNow(null);
        if (load == null)
            return Material.BARRIER;
        return SUPPORT_GET_TYPE ? world.getType(x, y, z)
                : load.getBlock(x & 15, y, z & 15).getType();
    }

    private static boolean SUPPORT_BOUNDING_BOX = true;
    private static boolean SUPPORT_GET_TYPE = true;
    static {
        try {
            Class.forName("org.bukkit.RegionAccessor").getMethod("getType", int.class, int.class, int.class);
        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            SUPPORT_GET_TYPE = false;
        }
        try {
            Block.class.getMethod("getBoundingBox");
        } catch (NoSuchMethodException | SecurityException e) {
            SUPPORT_BOUNDING_BOX = false;
        }
    }
}
