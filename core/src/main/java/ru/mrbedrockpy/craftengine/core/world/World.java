package ru.mrbedrockpy.craftengine.core.world;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.phys.AABB;
import ru.mrbedrockpy.craftengine.core.util.config.CraftEngineConfig;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.core.world.block.Blocks;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;
import ru.mrbedrockpy.craftengine.core.world.chunk.ChunkLoadManager;
import ru.mrbedrockpy.craftengine.core.world.entity.Entity;
import ru.mrbedrockpy.craftengine.core.world.entity.PlayerEntity;
import ru.mrbedrockpy.craftengine.core.world.generator.ChunkGenerator;
import ru.mrbedrockpy.craftengine.core.world.raycast.BlockRaycastResult;


import java.util.ArrayList;
import java.util.List;

public class World {

    protected final ChunkLoadManager chunkLoadManager = new ChunkLoadManager(Chunk.SIZE, CraftEngineConfig.RENDER_DISTANCE, 0);
    
    @Getter
    private final Chunk[][] chunks;
    
    private final ChunkGenerator chunkGenerator;
    
    private final List<Entity> entities = new ArrayList<>();
    private final List<PlayerEntity> players = new ArrayList<>();
    
    public World(int size, ChunkGenerator chunkGenerator) {
        this.chunks = new Chunk[size][size];
        this.chunkGenerator = chunkGenerator;
    }

    public void tick() {
        chunkLoadManager.tick(players.stream().map(Entity::getChunkPosition).toList());
        for(Entity entity : entities){
            entity.tick();
        }
    }

    public Chunk getChunk(Vector2i pos) {
        return this.getChunk(pos.x, pos.y);
    }

    public @Nullable Chunk getChunk(int x, int z) {
        try {
            if(this.chunks[x][z] == null) {
                this.chunks[x][z] = new Chunk(new Vector2i(x, z));
                chunkGenerator.generate(this.chunks[x][z]);
            }
            return this.chunks[x][z];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void setChunk(Chunk chunk) {
        if (chunk == null) return;
        int x = chunk.getPosition().x;
        int z = chunk.getPosition().y;
        if (x < 0 || z < 0 || x >= chunks.length || z >= chunks[0].length) return;
        this.chunks[x][z] = chunk;
    }
    
    public @Nullable Chunk getChunkByBlockPosition(int x, int y) {
        return getChunk(Math.floorDiv(x, Chunk.SIZE), Math.floorDiv(y, Chunk.SIZE));
    }
    
    public Block getBlock(Vector3i position) {
        return getBlock(position.x, position.y, position.z);
    }
    
    public Block getBlock(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE) {
            return Blocks.AIR;
        }
        Chunk chunk = getChunkByBlockPosition(x, z);
        if (chunk == null) {
            return Blocks.AIR;
        }
        return chunk.getBlock(
            Math.floorMod(x, Chunk.SIZE),
            y,
            Math.floorMod(z, Chunk.SIZE)
        );
    }
    
    public boolean setBlock(Vector3i position, Block block) {
        return setBlock(position.x, position.y, position.z, block);
    }
    
    public boolean setBlock(int x, int y, int z, Block block) {
        if (y < 0 || y >= Chunk.SIZE) return false;
        Chunk chunk = getChunkByBlockPosition(x, z);
        if (chunk == null) return false;
        return chunk.setBlock(
            Math.floorMod(x, Chunk.SIZE),
            y,
            Math.floorMod(z, Chunk.SIZE),
            block
        );
//        if (success) {
//            int localX = Math.floorMod(x, Chunk.SIZE);
//            int localZ = Math.floorMod(z, Chunk.SIZE);
//
//            if (localX == 0) {
//                Chunk neighbor = getChunkByBlockPosition(x - 1, z);
//                if (neighbor != null) neighbor.markDirty();
//            } else if (localX == Chunk.SIZE - 1) {
//                Chunk neighbor = getChunkByBlockPosition(x + 1, z);
//                if (neighbor != null) neighbor.markDirty();
//            }
//
//            if (localZ == 0) {
//                Chunk neighbor = getChunkByBlockPosition(x, z - 1);
//                if (neighbor != null) neighbor.markDirty();
//            } else if (localZ == Chunk.SIZE - 1) {
//                Chunk neighbor = getChunkByBlockPosition(x, z + 1);
//                if (neighbor != null) neighbor.markDirty();
//            }
//        }
//
//        return success;
    }
    
    public ArrayList<AABB> cubes(AABB boundingBox) {
        ArrayList<AABB> boundingBoxList = new ArrayList<>();
        
        int minX = (int) (Math.floor(boundingBox.minX) - 1);
        int maxX = (int) (Math.ceil(boundingBox.maxX) + 1);
        int minY = (int) (Math.floor(boundingBox.minY) - 1);
        int maxY = (int) (Math.ceil(boundingBox.maxY) + 1);
        int minZ = (int) (Math.floor(boundingBox.minZ) - 1);
        int maxZ = (int) (Math.ceil(boundingBox.maxZ) + 1);
        
        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        minZ = Math.max(0, minZ);
        
        maxX = Math.min(Chunk.SIZE * getSize(), maxX);
        maxY = Math.min(Chunk.SIZE, maxY);
        maxZ = Math.min(Chunk.SIZE * getSize(), maxZ);

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    
                    Block block = getBlock(x, y, z);
                    if (block != Blocks.AIR) {
                        
                        AABB aabb = block.getAABB(x, y, z);
                        if (aabb != null) {
                            boundingBoxList.add(aabb);
                        }
                    }
                }
            }
        }
        return boundingBoxList;
    }
    
    public List<AABB> allEntityAABBs() {
        List<AABB> result = new ArrayList<>();
        for (Entity entity : this.entities) {
            result.add(entity.getBoundingBox());
        }
        return result;
    }
    
    public boolean canPlaceBlockAt(Vector3i position) {
        return canPlaceBlockAt(position.x, position.y, position.z);
    }

    // TODO: использовать сущностей из текущего чанка
    public boolean canPlaceBlockAt(int x, int y, int z) {
        AABB blockAABB = new AABB(x, y, z, x + 1, y + 1, z + 1);
        
        for (AABB entityAABB : allEntityAABBs()) {
            if (blockAABB.intersects(entityAABB)) {
                return false;
            }
        }
        
        return true;
    }
    
    public int getSize() {
        return this.chunks.length;
    }
    
    public void addEntity(Entity entity) {
        entities.add(entity);
        if(entity instanceof PlayerEntity player){
            players.add(player);
        }
    }
    
    public void removeEntity(Entity entity) {
        entities.remove(entity);
        if(entity instanceof PlayerEntity player) {
            players.remove(player);
        }
    }

    public int getTopY(int x, int z) {
        for(int y = Chunk.SIZE; y >= 0; y--){
            if(!this.getBlock(x, y, z).isSolid()) continue;
            return y;
        }
        return 0;
    }

    public BlockRaycastResult rayCast(Vector3f originF, Vector3f directionF, float maxDistance) {

        Vector3d origin = new Vector3d(originF.x, originF.y, originF.z);
        Vector3d direction = new Vector3d(directionF.x, directionF.y, directionF.z);

        Vector3d pos = new Vector3d(origin);
        Vector3i blockPos = new Vector3i(
                (int) Math.floor(pos.x),
                (int) Math.floor(pos.y),
                (int) Math.floor(pos.z)
        );

        double deltaDistX = direction.x == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / direction.x);
        double deltaDistY = direction.y == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / direction.y);
        double deltaDistZ = direction.z == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / direction.z);

        int stepX = (int) Math.signum(direction.x);
        int stepY = (int) Math.signum(direction.y);
        int stepZ = (int) Math.signum(direction.z);

        double sideDistX = stepX > 0
                ? (Math.floor(pos.x) + 1.0 - pos.x) * deltaDistX
                : (pos.x - Math.floor(pos.x)) * deltaDistX;
        if (stepX == 0) sideDistX = Double.POSITIVE_INFINITY;

        double sideDistY = stepY > 0
                ? (Math.floor(pos.y) + 1.0 - pos.y) * deltaDistY
                : (pos.y - Math.floor(pos.y)) * deltaDistY;
        if (stepY == 0) sideDistY = Double.POSITIVE_INFINITY;

        double sideDistZ = stepZ > 0
                ? (Math.floor(pos.z) + 1.0 - pos.z) * deltaDistZ
                : (pos.z - Math.floor(pos.z)) * deltaDistZ;
        if (stepZ == 0) sideDistZ = Double.POSITIVE_INFINITY;

        double distance = 0.0;

        Block.Direction lastFace = Block.Direction.NONE;

        Block block = getBlock(blockPos.x, blockPos.y, blockPos.z);
        if (block != null && block.isSolid()) {
            return new BlockRaycastResult(blockPos.x, blockPos.y, blockPos.z, block, lastFace);
        }

        while (distance <= maxDistance) {
            if (sideDistX < sideDistY) {
                if (sideDistX < sideDistZ) {
                    blockPos.x += stepX;
                    distance = sideDistX;
                    sideDistX += deltaDistX;
                    lastFace = stepX > 0 ? Block.Direction.WEST : Block.Direction.EAST;
                } else {
                    blockPos.z += stepZ;
                    distance = sideDistZ;
                    sideDistZ += deltaDistZ;
                    lastFace = stepZ > 0 ? Block.Direction.NORTH : Block.Direction.SOUTH;
                }
            } else {
                if (sideDistY < sideDistZ) {
                    blockPos.y += stepY;
                    distance = sideDistY;
                    sideDistY += deltaDistY;
                    lastFace = stepY > 0 ? Block.Direction.DOWN : Block.Direction.UP;
                } else {
                    blockPos.z += stepZ;
                    distance = sideDistZ;
                    sideDistZ += deltaDistZ;
                    lastFace = stepZ > 0 ? Block.Direction.NORTH : Block.Direction.SOUTH;
                }
            }
            if (distance > maxDistance) break;
            block = getBlock(blockPos.x, blockPos.y, blockPos.z);
            if (block != null && block.isSolid()) return new BlockRaycastResult(blockPos.x, blockPos.y, blockPos.z, block, lastFace);
        }

        return null;
    }

}