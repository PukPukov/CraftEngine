package ru.mrbedrockpy.craftengine.world.entity;

import lombok.Getter;
import org.joml.*;
import ru.mrbedrockpy.craftengine.Util;
import ru.mrbedrockpy.craftengine.keybind.KeyBindings;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.World;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.inventory.PlayerInventory;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;
import ru.mrbedrockpy.renderer.phys.PhysConstants;
import ru.mrbedrockpy.renderer.window.Input;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;

public class ClientPlayerEntity extends Entity {

    private static final double EYE_STD          = 1.8;
    private static final double EYE_SNEAK        = 1.6;
    private static final double EYE_STEP         = Util.genericLerpStep(EYE_STD, EYE_SNEAK, 100);

    private static final float MOUSE_SENS        = 0.03f;

    @Getter private final Camera camera = new Camera();
    @Getter private double currentEyeOffset = EYE_STD;

    private boolean eyeLerpPhase = false;
    private boolean sneaking = false;
    private boolean sprinting = false;

    private long prevFrameNanos;

    @Getter private final PlayerInventory inventory = new PlayerInventory();
    private final Vector3f tmpPos   = new Vector3f();

    public ClientPlayerEntity(Vector3f position, ClientWorld world) {
        super(position, new Vector3f(0.6f, 0.6f, 1.8f), world);
        this.camera.setPosition(new Vector3f(position).add(0, 0, (float) currentEyeOffset));
    }

    @Override
    public void update(double deltaTime, double partialTick) {
        super.update(deltaTime, partialTick);

        handleMouseLook();
        handleMouseActions(world);

        long now = System.nanoTime();
        Util.genericLerp(
                this.prevFrameNanos, now,
                this.currentEyeOffset,
                sneaking ? EYE_SNEAK : EYE_STD,
                (off) -> this.currentEyeOffset = off,
                EYE_STEP,
                this.eyeLerpPhase, (ph) -> this.eyeLerpPhase = ph
        );
        this.camera.frameUpdate(this.prevFrameNanos, now);
        this.prevFrameNanos = now;

        if (partialTick < 0.03) {
            tmpPos.set(this.previousTickPosition);
        } else {
            tmpPos.set(this.previousTickPosition).lerp(this.tickPosition, (float) partialTick);
        }
        camera.setPosition(tmpPos.add(0, 0, (float) currentEyeOffset));
    }

    private void handleMouseLook() {
        if (Input.currentLayer() != Input.Layer.GAME) return;
        camera.rotate(new Vector2f(
                (float) -Input.getDeltaY() * MOUSE_SENS,
                (float) -Input.getDeltaX() * MOUSE_SENS
        ));
    }

    private void handleMouseActions(World world) {
        if (KeyBindings.ATTACK.wasPressed()) {
            BlockRaycastResult hit = world.raycast(camera.getPosition(), camera.getFront(), 4.5f);
            if (hit != null) world.setBlock(hit.x, hit.y, hit.z, Blocks.AIR);
        } else if (KeyBindings.BUILD.wasPressed()) {
            ItemStack selected = inventory.getSelectedStack();
            if (selected != null) selected.item().use(this);
        }
    }

    @Override
    public void tick() {
        super.tick();

        float strafe = 0f, forward = 0f;
        if (KeyBindings.MOVE_LEFT.isPressed())   strafe -= 1f;
        if (KeyBindings.MOVE_RIGHT.isPressed())  strafe += 1f;
        if (KeyBindings.MOVE_FORWARD.isPressed()) forward += 1f;
        if (KeyBindings.MOVE_BACK.isPressed())    forward -= 1f;

        if (KeyBindings.JUMP.isPressed()) jump();
        setSneaking(KeyBindings.SHIFT.isPressed());

        boolean backwardish = forward <= 0f;
        setSprinting(KeyBindings.SPRINT.isPressed() && !backwardish);

        float slipperiness = onGround ? 0.6f : 1.0f;
        float friction = onGround ? slipperiness * 0.91f : 0.91f;

        float accel = onGround ? (0.1f * (0.16277136f / (friction * friction * friction))) : 0.02f;

        if (sprinting) accel *= 1.3f;
        if (sneaking)  accel *= 0.3f;

        Vector3f front = camera.getFlatFront();
        Vector3f right = new Vector3f(front).cross(0,0,1).normalize();

        float len = (float) Math.sqrt(strafe*strafe + forward*forward);
        if (len > 1e-6f) { strafe /= len; forward /= len; }

        velocity.x += (front.x * forward + right.x * strafe) * accel;
        velocity.y += (front.y * forward + right.y * strafe) * accel;

        velocity.x *= friction;
        velocity.y *= friction;

        this.moveLimited(new Vector3d(velocity.x, velocity.y, velocity.z), this.sneaking);
    }

    @Override
    public void render(Camera camera) {}


    private void setSneaking(boolean value) {
        if (this.sneaking == value) return;
        this.sneaking = value;
        this.eyeLerpPhase = true;
    }

    private void setSprinting(boolean value) {
        if (this.sprinting == value) return;
        this.sprinting = value;
        if (value) this.camera.sprint();
        else       this.camera.walk();
    }
}