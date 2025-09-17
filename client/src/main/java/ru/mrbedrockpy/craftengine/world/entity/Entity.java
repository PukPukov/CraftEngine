package ru.mrbedrockpy.craftengine.world.entity;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.world.chunk.Chunk;
import ru.mrbedrockpy.renderer.phys.AABB;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.World;
import ru.mrbedrockpy.renderer.phys.PhysConstants;

import java.util.List;
import java.util.function.Consumer;

public abstract class Entity {
    
    @Getter
    protected Vector3f tickPosition = new Vector3f();
    protected Vector3f velocity = new Vector3f();
    public Vector3f previousTickPosition = new Vector3f();
    @Getter
    protected Vector3f size = new Vector3f(1, 1, 1);
    @Getter
    @Setter
    protected World world;
    
    protected boolean onGround = false;
    
    private final float jumpStrength = 0.44f;
    @Getter
    protected AABB boundingBox;
    
    public Entity(Vector3f tickPosition, Vector3f size, World world) {
        this.size.set(size);
        this.world = world;
        setTickPosition(tickPosition);
    }
    
    public void update(double deltaTime, double partialTick) {
    }

    public abstract void render(Camera camera);
    
    public void tick() {
        previousTickPosition = new Vector3f(tickPosition);
        velocity.z -= PhysConstants.GRAVITY;
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
        
        tickPosition.set(this.boundingBox.root());
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
    
    public void setTickPosition(Vector3f tickPosition) {
        this.tickPosition.set(tickPosition);
        this.boundingBox = new AABB(
            tickPosition.x - size.x / 2, tickPosition.y - size.y / 2, tickPosition.z,
            tickPosition.x + size.x / 2, tickPosition.y + size.y / 2, tickPosition.z + size.z
        );
    }
    
    
    public void jump() {
        if (onGround) {
            velocity.z = jumpStrength;
            onGround = false;
        }
    }

    public Vector2i getChunkPosition() {
        return new Vector2i((int) (tickPosition.x / Chunk.SIZE), (int) (tickPosition.y / Chunk.SIZE));
    }
}