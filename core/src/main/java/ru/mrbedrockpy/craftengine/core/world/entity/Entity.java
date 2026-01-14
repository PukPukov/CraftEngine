package ru.mrbedrockpy.craftengine.core.world.entity;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.core.phys.AABB;
import ru.mrbedrockpy.craftengine.core.phys.PhysConstants;
import ru.mrbedrockpy.craftengine.core.world.World;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;

import java.util.List;
import java.util.function.Consumer;

public abstract class Entity {
    
    @Getter
    protected Vector3f position = new Vector3f();
    protected Vector3f velocity = new Vector3f();
    public Vector3f previousTickPosition = new Vector3f();
    @Getter
    protected Vector3f size = new Vector3f(1, 1, 1);
    @Getter
    @Setter
    protected World world;
    
    protected boolean onGround = false;
    
    private final float jumpStrength = 0.510f;
    @Getter
    protected AABB boundingBox;
    
    public Entity(Vector3f tickPosition, Vector3f size, World world) {
        this.size.set(size);
        this.world = world;
        setPosition(tickPosition);
    }
    
    public void update(double deltaTime, double partialTick) {
    }

    public void tick() {
        previousTickPosition = new Vector3f(position);
        velocity.y -= PhysConstants.GRAVITY;
    }
    
    // MOVE LIMITED
    public void moveLimited(Vector3d movement, boolean protectFromFalling) {
        Vector3d originalMovement = new Vector3d(movement);
        
        List<AABB> aabbs = this.world.cubes(this.boundingBox.expand(movement));
        
        if (protectFromFalling && this.onGround) {
            var xProbeBox = this.boundingBox.clone();
            xProbeBox.move(movement.x, 0f, 0f);
            var zProbeBox = this.boundingBox.clone();
            zProbeBox.move(0f, 0f, movement.z);
            
            double initialProbe = -0.25;
            double xProbe = initialProbe, zProbe = initialProbe;
            for (AABB aabb : aabbs) {
                xProbe = aabb.clipYCollide(xProbeBox, xProbe);
                zProbe = aabb.clipYCollide(zProbeBox, zProbe);
            }
            if (xProbe == initialProbe) this.moveCloseToBounds(
                movement.x, (x) -> movement.x = x,
                this.boundingBox.minX, this.boundingBox.maxX
            );
            if (zProbe == initialProbe) this.moveCloseToBounds(
                movement.z, (z) -> movement.z = z,
                this.boundingBox.minZ, this.boundingBox.maxZ
            );
        }

        for (AABB aabb : aabbs) {
            movement.x = aabb.clipXCollide(this.boundingBox, movement.x);
            movement.y = aabb.clipYCollide(this.boundingBox, movement.y);
            movement.z = aabb.clipZCollide(this.boundingBox, movement.z);
        }
        this.boundingBox.move(movement); // TODO: сделать три цикла и исправить баг с залезаниями в блоки через грани

        this.onGround = originalMovement.y != movement.y && originalMovement.y < 0.0F;
        
        if (originalMovement.x != movement.x) this.velocity.x = 0.0F;
        if (originalMovement.y != movement.y) this.velocity.y  = 0.0F;
        if (originalMovement.z != movement.z) this.velocity.z = 0.0F;
        
        position.set(this.boundingBox.root());
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
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
        this.boundingBox = new AABB(
            position.x - size.x / 2, position.y ,position.z - size.z / 2,
            position.x + size.x / 2, position.y + size.y, position.z + size.z / 2
        );
    }
    
    
    public void jump() {
        if (onGround) {
            velocity.y = jumpStrength;
            onGround = false;
        }
    }

    public Vector2i getChunkPosition() {
        return new Vector2i((int) (position.x / Chunk.SIZE), (int) (position.z / Chunk.SIZE));
    }
}