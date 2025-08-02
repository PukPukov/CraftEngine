package ru.mrbedrockpy.craftengine.world.entity;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import ru.mrbedrockpy.renderer.api.IEntity;
import ru.mrbedrockpy.renderer.phys.AABB;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.Chunk;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.World;

import java.util.List;

// TODO: разделить LivingEntity на Entity и LivingEntity, чтобы не было лишних методов в LivingEntity
public abstract class LivingEntity implements IEntity {
    
    @Getter
    protected Vector3f position = new Vector3f();
    protected Vector3f velocity = new Vector3f();
    public Vector3f prevPosition = new Vector3f();
    @Getter
    protected Vector3f size = new Vector3f(1, 1, 1);
    protected int pitch;
    protected int yaw;
    @Setter
    @Getter
    protected World world;
    
    protected boolean onGround = false;
    
    private final float jumpStrength = 0.7f;
    protected AABB boundingBox;
    
    public LivingEntity(Vector3f position, Vector3f size, World world) {
        this.size.set(size);
        this.world = world;
        setPosition(position);
    }
    
    public void update(double deltaTime, double partialTick, ClientWorld world){
    }
    public abstract void render(Camera camera);
    
    @Override
    public void tick() {
        prevPosition = new Vector3f(position);
    }
    
    public void move(Vector3d direction) {
        Vector3d prevDir = new Vector3d(direction);
        
        List<AABB> aABBs = this.world.cubes(this.boundingBox.expand(direction));
        
        for (AABB abb : aABBs) {
            direction.z = abb.clipZCollide(this.boundingBox, direction.z);
        }
        this.boundingBox.move(0.0F, 0.0F, direction.z);
        
        for (AABB aABB : aABBs) {
            direction.x = aABB.clipXCollide(this.boundingBox, direction.x);
        }
        this.boundingBox.move(direction.x, 0.0F, 0.0F);
        
        for (AABB aABB : aABBs) {
            direction.y = aABB.clipYCollide(this.boundingBox, direction.y);
        }
        this.boundingBox.move(0.0F, direction.y, 0.0F);
        
        this.onGround = prevDir.z != direction.z && prevDir.z < 0.0F;
        
        if (prevDir.x != direction.x) this.velocity.x = 0.0F;
        if (prevDir.y != direction.y) this.velocity.y  = 0.0F;
        if (prevDir.z != direction.z) this.velocity.z = 0.0F;
        
        position.set(
            (float) ((this.boundingBox.minX + this.boundingBox.maxX) / 2.0D),
            (float) ((this.boundingBox.minY + this.boundingBox.maxY) / 2.0D),
            (float) this.boundingBox.minZ
        );
    }
    
    protected void moveRelative(float x, float y, float speed) {
        float distance = x * x + y * y;
        
        if (distance < 0.01F)
            return;
        
        distance = speed / (float) Math.sqrt(distance);
        x *= distance;
        y *= distance;
        
        double sin = Math.sin(Math.toRadians(this.yaw));
        double cos = Math.cos(Math.toRadians(this.yaw));
        
        velocity.x += (float) (x * cos - y * sin);
        velocity.y += (float) (y * cos + x * sin);
    }
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
        this.boundingBox = new AABB(
            position.x - size.x / 2, position.y - size.y / 2, position.z,
            position.x + size.x / 2, position.y + size.y / 2, position.z + size.z
        );
    }
    
    
    public void jump() {
        if (onGround) {
            velocity.z = jumpStrength;
            onGround = false;
        }
    }
    
    public int x() {
        return (int) position.x;
    }
    
    public int y() {
        return (int) position.y;
    }
    
    public int z() {
        return (int) position.z;
    }
    
    @Override
    public Vector2i chunkPosition() {
        return new Vector2i((int) (position.x / Chunk.WIDTH), (int) (position.y / Chunk.WIDTH));
    }

    @Override
    public AABB boundingBox() {
        return boundingBox;
    }
}