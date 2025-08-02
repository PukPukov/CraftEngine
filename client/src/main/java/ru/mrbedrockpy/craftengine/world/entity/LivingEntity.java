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
import java.util.function.Consumer;

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
    
    private final float jumpStrength = 0.5f;
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
    
    // MOVE LIMITED
    public void moveLimited(Vector3d movement, boolean protectFromFalling) {
        Vector3d originalMovement = new Vector3d(movement);
        
        List<AABB> aabbs = this.world.cubes(this.boundingBox.expand(movement));
        
        if (protectFromFalling && this.onGround) {
            var xProbeBox = this.boundingBox.clone();
            xProbeBox.move(movement.x, 0f, 0f);
            var yProbeBox = this.boundingBox.clone();
            yProbeBox.move(0f, movement.y, 0f);
            
            double initialProbe = -0.25;
            double xProbe = initialProbe, yProbe = initialProbe;
            for (AABB aabb : aabbs) {
                xProbe = aabb.clipZCollide(xProbeBox, xProbe);
                yProbe = aabb.clipZCollide(yProbeBox, yProbe);
            }
            if (xProbe == initialProbe) this.moveCloseToBounds(
                movement.x, (x) -> movement.x = x,
                this.boundingBox.minX, this.boundingBox.maxX
            );
            if (yProbe == initialProbe) this.moveCloseToBounds(
                movement.y, (y) -> movement.y = y,
                this.boundingBox.minY, this.boundingBox.maxY
            );
        }
        
        for (AABB aabb : aabbs) {
            movement.x = aabb.clipXCollide(this.boundingBox, movement.x);
        }
        this.boundingBox.move(movement.x, 0.0F, 0.0F);
        
        for (AABB aabb : aabbs) {
            movement.y = aabb.clipYCollide(this.boundingBox, movement.y);
        }
        this.boundingBox.move(0.0F, movement.y, 0.0F);
        
        for (AABB aabb : aabbs) {
            movement.z = aabb.clipZCollide(this.boundingBox, movement.z);
        }
        this.boundingBox.move(0.0F, 0.0F, movement.z);
        
        this.onGround = originalMovement.z != movement.z && originalMovement.z < 0.0F;
        
        if (originalMovement.x != movement.x) this.velocity.x = 0.0F;
        if (originalMovement.y != movement.y) this.velocity.y  = 0.0F;
        if (originalMovement.z != movement.z) this.velocity.z = 0.0F;
        
        nextTickPosition.set(this.boundingBox.root());
    }
    
    private void moveCloseToBounds(double coordinate, Consumer<Double> coordinateSetter, double min, double max) {
        boolean forward = coordinate > 0;
        double fBoxFace = (forward ? min : max);
        double next = fBoxFace + coordinate;
        int target = forward ? (int) next : (int) Math.ceil(next);
        double resultMove = target-fBoxFace;
        if (forward) resultMove -= 0.0001;
        else resultMove += 0.0001;
        coordinateSetter.accept(resultMove);
    }
    
    // MOVE LIMITED.END
    
    protected void changeVelocityByForce(float x, float y, float force) {
        float distance = x * x + y * y;
        
        if (distance < 0.01F)
            return;
        
        distance = force / (float) Math.sqrt(distance);
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
    
    public int blockX() {
        return (int) nextTickPosition.x;
    }
    
    public int blockY() {
        return (int) nextTickPosition.y;
    }
    
    public int blockZ() {
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