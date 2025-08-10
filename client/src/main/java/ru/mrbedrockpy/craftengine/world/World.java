package ru.mrbedrockpy.craftengine.world;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.entity.LivingEntity;
import ru.mrbedrockpy.craftengine.world.generator.ChunkGenerator;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.api.IChunk;
import ru.mrbedrockpy.renderer.api.IEntity;
import ru.mrbedrockpy.renderer.api.IWorld;
import ru.mrbedrockpy.renderer.phys.AABB;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

import java.util.ArrayList;
import java.util.List;

public abstract class World implements IWorld {
    
    @Getter
    private final IChunk[][] chunks;
    
    private final ChunkGenerator chunkGenerator;
    
    private final List<IEntity> entities;
    
    public World(int size, ChunkGenerator chunkGenerator) {
        this.chunks = new Chunk[size][size];
        this.chunkGenerator = chunkGenerator;
        this.entities = new ArrayList<>();
    }

    public void tick() {
        for (int chunkX = 0; chunkX < 8; chunkX++) {
            for (int chunkY = 0; chunkY < 8; chunkY++) {
                IChunk chunk = chunk(chunkX, chunkY);
                if (chunk == null) throw new IllegalStateException();
                List<IEntity> entitiesInChunk = new ArrayList<>();
                for (IEntity entity: entities) {
                    if(chunk == chunkByBlockPosition(Math.round(entity.getTickPosition().x), Math.round(entity.getTickPosition().y))){
                        entitiesInChunk.add(entity);
                    }
                }
                chunk.setEntities(entitiesInChunk);
                chunk.tick();
            }
        }
        for (var entity : this.entities) {
            entity.tick();
        }
    }
    
    @Override
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
        
        IBlock block = block(blockPos.x, blockPos.y, blockPos.z);
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
            
            block = block(blockPos.x, blockPos.y, blockPos.z);
            if (block != null && block.isSolid()) {
                return new BlockRaycastResult(blockPos.x, blockPos.y, blockPos.z, block, lastFace);
            }
        }
        
        return null;
    }

    @Nullable
    @Override
    public IChunk chunk(Vector2i pos) {
        return this.chunk(pos.x, pos.y);
    }

    @Override
    public @Nullable IChunk chunk(int x, int y) {
        try {
            IChunk chunk = this.chunks[x][y];
            if(chunk == null) {
                this.chunks[x][y] = new Chunk(new Vector2i(x, y));
                chunkGenerator.generate(new Vector2i(x, y), this.chunks[x][y]);
            }
            return chunk;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
    
    @Override
    public @Nullable IChunk chunkByBlockPosition(int x, int y) {
        return chunk(Math.floorDiv(x, Chunk.WIDTH), Math.floorDiv(y, Chunk.WIDTH));
    }
    
    @Override
    public IBlock block(Vector3i position) {
        return block(position.x, position.y, position.z);
    }
    
    @Override
    public IBlock block(int x, int y, int z) {
        if (z < 0 || z >= Chunk.HEIGHT) {
            return Blocks.AIR;
        }
        IChunk chunk = chunkByBlockPosition(x, y);
        if (chunk == null) {
            return Blocks.AIR; // Outside of the world's horizontal bounds
        }
        return chunk.getBlock(
            Math.floorMod(x, Chunk.WIDTH),
            Math.floorMod(y, Chunk.WIDTH),
            z
        );
    }
    
    public boolean setBlock(Vector3i position, IBlock block) {
        return setBlock(position.x, position.y, position.z, block);
    }
    
    public boolean setBlock(int x, int y, int z, IBlock block) {
        if (z < 0 || z >= Chunk.HEIGHT) {
            return false;
        }
        IChunk chunk = chunkByBlockPosition(x, y);
        if (chunk == null) return false;
        
        boolean success = chunk.setBlock(
            Math.floorMod(x, Chunk.WIDTH),
            Math.floorMod(y, Chunk.WIDTH),
            z,
            block
        );
        
        if (success) {
            // Mark neighboring chunks as dirty if the block is on a boundary
            int localX = Math.floorMod(x, Chunk.WIDTH);
            int localY = Math.floorMod(y, Chunk.WIDTH);
            
            if (localX == 0) {
                IChunk neighbor = chunkByBlockPosition(x - 1, y);
                if (neighbor != null) neighbor.markDirty();
            } else if (localX == Chunk.WIDTH - 1) {
                IChunk neighbor = chunkByBlockPosition(x + 1, y);
                if (neighbor != null) neighbor.markDirty();
            }
            
            if (localY == 0) {
                IChunk neighbor = chunkByBlockPosition(x, y - 1);
                if (neighbor != null) neighbor.markDirty();
            } else if (localY == Chunk.WIDTH - 1) {
                IChunk neighbor = chunkByBlockPosition(x, y + 1);
                if (neighbor != null) neighbor.markDirty();
            }
        }
        
        return success;
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
        
        maxX = Math.min(Chunk.WIDTH * size(), maxX);
        maxY = Math.min(Chunk.WIDTH * size(), maxY);
        maxZ = Math.min(Chunk.HEIGHT, maxZ);
        
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    
                    IBlock block = block(x, y, z);
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
        for (IEntity entity : this.entities) {
            result.add(entity.getBoundingBox());
        }
        return result;
    }
    
    public boolean canPlaceBlockAt(Vector3i position) {
        return canPlaceBlockAt(position.x, position.y, position.z);
    }
    
    public boolean canPlaceBlockAt(int x, int y, int z) {
        AABB blockAABB = new AABB(x, y, z, x + 1, y + 1, z + 1);
        
        for (AABB entityAABB : allEntityAABBs()) {
            if (blockAABB.intersects(entityAABB)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public int size() {
        return this.chunks.length * Chunk.WIDTH;
    }
    
    public void addEntity(IEntity entity) {
        entities.add(entity);
    }
    
    public void removeEntity(LivingEntity entity) {
        entities.remove(entity);
    }

    public int getTopZ(int x, int y){
        for(int z = IChunk.HEIGHT; z >= 0; z--){
            if(!this.block(x, y, z).isSolid()) continue;
            return z;
        }
        return IChunk.HEIGHT;
    }
}