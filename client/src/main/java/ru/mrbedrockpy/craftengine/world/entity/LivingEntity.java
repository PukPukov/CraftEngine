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
    protected Vector3f nextTickPosition = new Vector3f();
    protected Vector3f velocity = new Vector3f();
    public Vector3f previousTickPosition = new Vector3f();
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
    
    public LivingEntity(Vector3f nextTickPosition, Vector3f size, World world) {
        this.size.set(size);
        this.world = world;
        setNextTickPosition(nextTickPosition);
    }
    
    public void update(double deltaTime, double partialTick, ClientWorld world){
    }
    public abstract void render(Camera camera);
    
    @Override
    public void tick() {
        previousTickPosition = new Vector3f(nextTickPosition);
    }
    
    public void move(Vector3d movement) {
        Vector3d prevDir = new Vector3d(movement);
        
        List<AABB> aabbs = this.world.cubes(this.boundingBox.expand(movement));
        
        for (AABB aABB : aabbs) {
            movement.x = aABB.clipXCollide(this.boundingBox, movement.x);
        }
        this.boundingBox.move(movement.x, 0.0F, 0.0F);
        
        for (AABB aABB : aabbs) {
            movement.y = aABB.clipYCollide(this.boundingBox, movement.y);
        }
        this.boundingBox.move(0.0F, movement.y, 0.0F);
        
        for (AABB abb : aabbs) {
            movement.z = abb.clipZCollide(this.boundingBox, movement.z);
        }
        this.boundingBox.move(0.0F, 0.0F, movement.z);
        
        this.onGround = prevDir.z != movement.z && prevDir.z < 0.0F;
        
        if (prevDir.x != movement.x) this.velocity.x = 0.0F;
        if (prevDir.y != movement.y) this.velocity.y  = 0.0F;
        if (prevDir.z != movement.z) this.velocity.z = 0.0F;
        
        nextTickPosition.set(this.boundingBox.root());
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
    
    public void setNextTickPosition(Vector3f nextTickPosition) {
        this.nextTickPosition.set(nextTickPosition);
        this.boundingBox = new AABB(
            nextTickPosition.x - size.x / 2, nextTickPosition.y - size.y / 2, nextTickPosition.z,
            nextTickPosition.x + size.x / 2, nextTickPosition.y + size.y / 2, nextTickPosition.z + size.z
        );
    }
    
    
    public void jump() {
        if (onGround) {
            velocity.z = jumpStrength;
            onGround = false;
        }
    }
    
    public int x() {
        return (int) nextTickPosition.x;
    }
    
    public int y() {
        return (int) nextTickPosition.y;
    }
    
    public int z() {
        return (int) nextTickPosition.z;
    }
    
    @Override
    public Vector2i chunkPosition() {
        return new Vector2i((int) (nextTickPosition.x / Chunk.WIDTH), (int) (nextTickPosition.y / Chunk.WIDTH));
    }

    @Override
    public AABB boundingBox() {
        return boundingBox;
    }
}