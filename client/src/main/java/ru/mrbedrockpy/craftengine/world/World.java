package ru.mrbedrockpy.craftengine.world;

import lombok.Getter;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.phys.AABB;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.entity.LivingEntity;
import ru.mrbedrockpy.craftengine.world.generator.ChunkGenerator;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;
import ru.mrbedrockpy.renderer.api.IWorld;

import java.util.ArrayList;
import java.util.List;

public abstract class World implements IWorld {
    
    @Getter
    private final Chunk[][] chunks;
    
    private final ChunkGenerator chunkGenerator;
    
    private final List<LivingEntity> entities;
    
    public World(int size, ChunkGenerator chunkGenerator) {
        this.chunks = new Chunk[size][size];
        this.chunkGenerator = chunkGenerator;
        this.entities = new ArrayList<>();
        generateWorld();
    }
    
    private void generateWorld() {
        for (int chunkX = 0; chunkX < chunks.length; chunkX++) {
            for (int chunkY = 0; chunkY < chunks.length; chunkY++) {
                Chunk chunk = new Chunk(new Vector2i(chunkX, chunkY));
                chunkGenerator.generate(chunk.getPosition(), chunk);
                this.chunks[chunkX][chunkY] = chunk;
            }
        }
    }
    
    public void tick() {
        for (int chunkX = 0; chunkX < chunks.length; chunkX++) {
            for (int chunkY = 0; chunkY < chunks.length; chunkY++) {
                Chunk chunk = getChunkByChunkPos(chunkX, chunkY);
                List<LivingEntity> entitiesInChunk = new ArrayList<>();
                for (LivingEntity entity: entities) {
                    if(chunk == getChunkByBlockPos(Math.round(entity.getPosition().x), Math.round(entity.getPosition().y))){
                        entitiesInChunk.add(entity);
                    }
                }
                chunk.setEntities(entitiesInChunk);
                chunk.tick();
            }
        }
    }

    public BlockRaycastResult raycast(Vector3f originF, Vector3f directionF, float maxDistanceF) {

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
        double maxDistance = maxDistanceF;

        IBlock.Direction lastFace = Block.Direction.NONE;

        IBlock block = getBlock(blockPos.x, blockPos.y, blockPos.z);
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
                    lastFace = stepZ > 0 ? Block.Direction.DOWN : Block.Direction.UP;
                }
            } else {
                if (sideDistY < sideDistZ) {
                    blockPos.y += stepY;
                    distance = sideDistY;
                    sideDistY += deltaDistY;
                    lastFace = stepY > 0 ? Block.Direction.NORTH : Block.Direction.SOUTH;
                } else {
                    blockPos.z += stepZ;
                    distance = sideDistZ;
                    sideDistZ += deltaDistZ;
                    lastFace = stepZ > 0 ? Block.Direction.DOWN : Block.Direction.UP;
                }
            }
            if (distance > maxDistance) {
                break;
            }

            block = getBlock(blockPos.x, blockPos.y, blockPos.z);
            if (block != null && block.isSolid()) {
                return new BlockRaycastResult(blockPos.x, blockPos.y, blockPos.z, block, lastFace);
            }
        }

        return null;
    }

    public Chunk getChunkByChunkPos(int x, int y) {
        try {
            if(this.chunks[x][y] == null) {
                this.chunks[x][y] = new Chunk(new Vector2i(x, y));
            }
            return chunks[x][y];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
    
    public Chunk getChunkByBlockPos(int x, int y) {
        return getChunkByChunkPos(x / Chunk.WIDTH, y / Chunk.WIDTH);
    }
    
    public IBlock getBlock(Vector3i position) {
        Chunk chunk = getChunkByBlockPos(position.x, position.y);
        if (chunk == null) return null;
        return chunk.getBlock(
            position.x % Chunk.WIDTH,
            position.y % Chunk.WIDTH,
            position.z % Chunk.HEIGHT
        );
    }
    
    public IBlock getBlock(int x, int y, int z) {
        Chunk chunk = getChunkByBlockPos(x, y);
        if (chunk == null) return Blocks.AIR;
        return chunk.getBlock(
            x % Chunk.WIDTH,
            y % Chunk.WIDTH,
            z % Chunk.HEIGHT
        );
    }
    
    public boolean setBlock(Vector3i position, IBlock block) {
        Chunk chunk = getChunkByBlockPos(position.x, position.y);
        if (chunk == null) return false;
        return chunk.setBlock(
            position.x % Chunk.WIDTH,
            position.y % Chunk.WIDTH,
            position.z % Chunk.HEIGHT,
            block
        );
    }
    
    public boolean setBlock(int x, int y, int z, Block block) {
        Chunk chunk = getChunkByBlockPos(x, y);
        if (chunk == null) return false;
        return chunk.setBlock(
            x % Chunk.WIDTH,
            y % Chunk.WIDTH,
            z % Chunk.HEIGHT,
            block
        );
    }
    
    public ArrayList<AABB> getCubes(AABB boundingBox) {
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
        
        maxX = Math.min(Chunk.WIDTH * getWorldSize(), maxX);
        maxY = Math.min(Chunk.WIDTH * getWorldSize(), maxY);
        maxZ = Math.min(Chunk.HEIGHT, maxZ);
        
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    
                    IBlock block = getBlock(x, y, z);
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
    
    public List<AABB> getAllEntityAABBs() {
        List<AABB> result = new ArrayList<>();
        for (LivingEntity entity : this.entities) {
            result.add(entity.boundingBox);
        }
        return result;
    }
    
    public boolean canPlaceBlockAt(Vector3i position) {
        return canPlaceBlockAt(position.x, position.y, position.z);
    }
    
    public boolean canPlaceBlockAt(int x, int y, int z) {
        AABB blockAABB = new AABB(x, y, z, x + 1, y + 1, z + 1);
        
        for (AABB entityAABB : getAllEntityAABBs()) {
            if (blockAABB.intersects(entityAABB)) {
                return false;
            }
        }
        
        return true;
    }
    
    public int getWorldSize() {
        return this.chunks.length * Chunk.WIDTH;
    }
    
    public void addEntity(LivingEntity entity) {
        entities.add(entity);
    }
    
    public void removeEntity(LivingEntity entity) {
        entities.remove(entity);
    }
}