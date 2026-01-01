package ru.mrbedrockpy.craftengine.client.world.entity;

import lombok.Getter;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBindings;
import ru.mrbedrockpy.renderer.window.Camera;
import ru.mrbedrockpy.craftengine.core.world.World;
import ru.mrbedrockpy.craftengine.core.world.block.Blocks;
import ru.mrbedrockpy.craftengine.core.world.entity.PlayerEntity;
import ru.mrbedrockpy.craftengine.core.world.item.ItemStack;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockBreakPacketC2S;
import ru.mrbedrockpy.craftengine.core.util.Util;
import ru.mrbedrockpy.craftengine.core.world.raycast.BlockRaycastResult;
import ru.mrbedrockpy.renderer.window.Input;

public class ClientPlayerEntity extends PlayerEntity {

    private static final float MOUSE_SENS = 0.03f;
    protected static final double EYE_STD          = 1.8;
    protected static final double EYE_SNEAK        = 1.6;
    protected static final double EYE_STEP         = Util.genericLerpStep(EYE_STD, EYE_SNEAK, 100);
    @Getter protected double currentEyeOffset = EYE_STD;
    private boolean eyeLerpPhase = false;


    @Getter
    private final Camera camera = new Camera();
    private long prevFrameNanos;

    public ClientPlayerEntity(Vector3f position, World world) {
        super(position, world);
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
            tmpPos.set(this.previousTickPosition).lerp(this.position, (float) partialTick);
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
            BlockRaycastResult hit = world.raycast(new Vector3f(camera.getPosition()), camera.getFront(), 4.5f);
            if (hit != null){
                world.setBlock(hit.x, hit.y, hit.z, Blocks.AIR);
                CraftEngineClient.INSTANCE.getGameClient().send(new BlockBreakPacketC2S(new Vector3i(hit.x, hit.y, hit.z)));
            }
        } else if (KeyBindings.BUILD.wasPressed()) {
            ItemStack selected = inventory.getSelectedStack();
            if (selected != null) selected.item().use(this);
        }
    }


    @Override
    public void tick() {
        super.tick();

        float strafe = 0f, forward = 0f;
        if (KeyBindings.MOVE_LEFT.isPressed()) strafe -= 1f;
        if (KeyBindings.MOVE_RIGHT.isPressed()) strafe += 1f;
        if (KeyBindings.MOVE_FORWARD.isPressed()) forward += 1f;
        if (KeyBindings.MOVE_BACK.isPressed()) forward -= 1f;

        if (KeyBindings.JUMP.isPressed()) jump();
        setSneaking(KeyBindings.SHIFT.isPressed());

        boolean backwardish = forward <= 0f;
        setSprinting(KeyBindings.SPRINT.isPressed() && !backwardish);

        float slipperiness = onGround ? 0.6f : 1.0f;
        float friction = onGround ? slipperiness * 0.91f : 0.91f;

        float accel = onGround ? (0.1f * (0.16277136f / (friction * friction * friction))) : 0.02f;

        if (sprinting) accel *= 1.3f;
        if (sneaking) accel *= 0.3f;

        Vector3f front = camera.getFlatFront();
        Vector3f right = new Vector3f(front).cross(0, 0, 1).normalize();

        float len = (float) Math.sqrt(strafe * strafe + forward * forward);
        if (len > 1e-6f) {
            strafe /= len;
            forward /= len;
        }

        velocity.x += (front.x * forward + right.x * strafe) * accel;
        velocity.y += (front.y * forward + right.y * strafe) * accel;
    }

    @Override
    public Vector3f getFront() {
        return camera.getFront();
    }

    @Override
    public void setSneaking(boolean value) {
        super.setSneaking(value);
        this.eyeLerpPhase = true;
    }

    @Override
    public void setSprinting(boolean value) {
        if (this.sprinting == value) return;
        this.sprinting = value;
        if (value) this.camera.sprint();
        else       this.camera.walk();
    }
}
