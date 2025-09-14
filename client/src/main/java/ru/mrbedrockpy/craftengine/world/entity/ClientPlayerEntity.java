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

import static org.lwjgl.glfw.GLFW.*;

public class ClientPlayerEntity extends Entity {

    private static final float SPEED_GROUND      = 0.10f;
    private static final float SPEED_SNEAK       = 0.035f;  // не множитель, чтобы в воздухе не влиял
    private static final float SPEED_AIR         = 0.02f;
    private static final float SPRINT_MULT       = 1.3f;

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

    private final Vector3f tmpDir   = new Vector3f();
    private final Vector3f tmpFront = new Vector3f();
    private final Vector3f tmpRight = new Vector3f();
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

        tmpDir.set(0, 0, 0);
        tmpFront.set(camera.getFlatFront());
        tmpRight.set(tmpFront).cross(0, 0, 1).normalize();

        boolean anyBackwardish = false;
        if (KeyBindings.MOVE_FORWARD.isPressed()) tmpDir.add(tmpFront); else anyBackwardish = true;
        if (KeyBindings.MOVE_BACK.isPressed())   { tmpDir.sub(tmpFront); anyBackwardish = true; }
        if (KeyBindings.MOVE_LEFT.isPressed())   tmpDir.sub(tmpRight);
        if (KeyBindings.MOVE_RIGHT.isPressed())  tmpDir.add(tmpRight);

        if (KeyBindings.JUMP.isPressed()) jump();

        setSprinting(KeyBindings.SPRINT.isPressed() && !anyBackwardish);
        setSneaking(KeyBindings.SHIFT.isPressed());

        if (tmpDir.x != 0 || tmpDir.y != 0 || tmpDir.z != 0) tmpDir.normalize();

        tmpDir.mul(currentSpeed());

        velocity.add(tmpDir);

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

    private float currentSpeed() {
        if (!this.onGround) return SPEED_AIR;
        float currentSprintingMultiplier = this.sprinting ? SPRINT_MULT : 1.0f;
        return this.sneaking ? (SPEED_SNEAK) : (SPEED_GROUND * currentSprintingMultiplier);
    }
}