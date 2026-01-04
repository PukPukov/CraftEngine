package ru.mrbedrockpy.craftengine.core.world.entity;

import lombok.Getter;
import lombok.Setter;
import org.joml.*;

import ru.mrbedrockpy.craftengine.core.world.World;
import ru.mrbedrockpy.craftengine.core.world.inventory.PlayerInventory;

public class PlayerEntity extends Entity {

    @Setter
    protected boolean sneaking = false;
    @Setter
    protected boolean sprinting = false;

    @Getter protected final PlayerInventory inventory = new PlayerInventory();
    protected final Vector3f tmpPos   = new Vector3f();

    public PlayerEntity(Vector3f position, World world) {
        super(position, new Vector3f(0.6f, 1.8f, 0.6f), world);
    }

    @Override
    public void tick() {
        super.tick();

        float slipperiness = onGround ? 0.6f : 1.0f;
        float friction = onGround ? slipperiness * 0.91f : 0.91f;

        velocity.x *= friction;
        velocity.z *= friction;

        this.moveLimited(new Vector3d(velocity.x, velocity.y, velocity.z), this.sneaking);
    }

    public Vector3f getFront() {
        return new Vector3f();
    }

    public float getEyeOffset(){
        return 1.8f;
    }
}