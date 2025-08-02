package ru.mrbedrockpy.craftengine.world.entity;

import lombok.Getter;
import org.joml.*;
import ru.mrbedrockpy.craftengine.Util;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.inventory.PlayerInventory;
import ru.mrbedrockpy.renderer.window.Input;

import java.util.function.DoublePredicate;

import static org.lwjgl.glfw.GLFW.*;

public class ClientPlayerEntity extends LivingEntity {

    // CONSTANTS

    private static final float speedStandard = 0.1f;
    private static final float speedSneaking = 0.05f; // сделано не мультиплаерами т.к. приседание в воздухе не должно менять скорость
    private static final float speedInAir = 0.02f;
    private static final float sprintingMultiplier = 1.3f;

    private static final double standardEyeOffset = 1.8;
    private static final double sneakingEyeOffset = 1.6;
    private static final double eyeOffsetStep = Util.genericLerpStep(standardEyeOffset, sneakingEyeOffset, 100);

    private static final float sensitivity = 0.03f;

    // STATE

    private @Getter
    final Camera camera = new Camera();
    private @Getter double currentEyeOffset = standardEyeOffset;
    private boolean sneakingStateChangePhase = false;
    private boolean isSneaking = false;
    private boolean isSprinting = false;
    private long previousFrameTimeNanos;
    private @Getter final PlayerInventory inventory = new PlayerInventory();


    public ClientPlayerEntity(Vector3f position, ClientWorld world) {
        super(position, new Vector3f(0.6f, 0.6f, 1.8f), world);
        this.camera.position(position.add(0, 0, (float) currentEyeOffset));

        Input.onPress.put(GLFW_KEY_LEFT_SHIFT, () -> toggleSneak(true));
        Input.onRelease.put(GLFW_KEY_LEFT_SHIFT, () -> toggleSneak(false));
        Input.onRelease.put(GLFW_KEY_LEFT_CONTROL, () -> toggleSprint(false));
    }

    // TODO вытащить гуи отсюда, нахуй он тут?
    // TODO зачем вообще всё это под Entity пихать? Я думаю у игрока должны быть свои тики, игрок != энтити
    // TODO добавить спринт

    @Override
    public void update(double deltaTime, double partialTick, ClientWorld world) {
        super.update(deltaTime, partialTick, world);
        if (!Input.isGUIOpen()) {
            camera.rotate(new Vector2f(
                    (float) -Input.deltaY() * sensitivity,
                    (float) -Input.deltaX() * sensitivity
            ));
        }
        long currentTime = System.nanoTime();
        Util.genericLerp(
                this.previousFrameTimeNanos, currentTime,
                this.currentEyeOffset, !this.isSneaking ? standardEyeOffset : sneakingEyeOffset, (offset) -> this.currentEyeOffset = offset,
                eyeOffsetStep,
                this.sneakingStateChangePhase, (phase) -> this.sneakingStateChangePhase = phase
        );
        this.camera.frameUpdate(this.previousFrameTimeNanos, currentTime);
        this.previousFrameTimeNanos = currentTime;

        Vector3f position = partialTick < 0.03 ? this.previousTickPosition : new Vector3f(this.previousTickPosition).lerp(this.nextTickPosition, (float) partialTick);
        camera.position(new Vector3f(position).add(0, 0, (float) currentEyeOffset));
    }

    @Override
    public void tick() {
        super.tick();
        Vector3f direction = new Vector3f();
        Vector3f front = camera.flatFront();
        Vector3f right = new Vector3f();
        front.cross(new Vector3f(0, 0, 1), right).normalize(); // установка right

        boolean noSprint = false;
        if (Input.pressed(GLFW_KEY_W)) direction.add(new Vector3f(front));
        else noSprint = true;
        if (Input.pressed(GLFW_KEY_S)) {
            direction.sub(new Vector3f(front));
            noSprint = true;
        }
        if (Input.pressed(GLFW_KEY_A)) direction.sub(new Vector3f(right));
        if (Input.pressed(GLFW_KEY_D)) direction.add(new Vector3f(right));
        if (Input.pressed(GLFW_KEY_SPACE)) this.jump();
        control:
        if (Input.pressed(GLFW_KEY_LEFT_CONTROL)) {
            if (noSprint) {
                toggleSprint(false);
                break control;
            }
            toggleSprint(true);
        }

        // изменение велосити окружающей средой
        velocity.z -= 0.08f; // гравитация
        velocity.mul(0.97f, 0.97f, 0.97f); // сопротивление воздуха
        if (onGround) { // трение об землю
            velocity.x *= 0.7f;
            velocity.y *= 0.7f;
        }
        // изменение велосити игроком
        changeVelocityByForce(direction.x, direction.y, this.speed());

        this.moveLimited(new Vector3d(velocity.x, velocity.y, velocity.z), this.isSneaking);
    }

    @Override
    public void render(Camera camera) {

    }

    private void toggleSneak(boolean isSneaking) {
        this.isSneaking = isSneaking;
        this.sneakingStateChangePhase = true;
    }

    private void toggleSprint(boolean isSprinting) {
        this.isSprinting = isSprinting;
        if (isSprinting) this.camera.sprint();
        else this.camera.walk();
    }

    private float speed() {
        if (!this.onGround) return speedInAir;
        float currentSprintingMultiplier = this.isSprinting ? sprintingMultiplier : 1.0f;
        return this.isSneaking ? (speedSneaking * currentSprintingMultiplier) : (speedStandard * currentSprintingMultiplier);
    }

}