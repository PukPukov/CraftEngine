package ru.mrbedrockpy.craftengine.world.entity;

import lombok.Getter;
import org.joml.*;
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

    private static final double standardEyeOffset = 1.8;
    private static final double sneakingEyeOffset = 1.6;
    private static final double eyeOffsetChangePerNanosecond = (standardEyeOffset - sneakingEyeOffset) / 100_000_000;

    private static final float sensitivity = 0.03f;

    // STATE

    private @Getter
    final Camera camera = new Camera();
    private @Getter double currentEyeOffset = standardEyeOffset;
    private boolean sneakingStateChangePhase = false;
    private boolean sneaking = false;
    private long previousFrameTimeNanos;
    @Getter
    private final PlayerInventory inventory = new PlayerInventory();

    public ClientPlayerEntity(Vector3f position, ClientWorld world) {
        super(position, new Vector3f(0.6f, 0.6f, 1.8f), world);
        this.camera.position(position.add(0, 0, (float) currentEyeOffset));
        Input.onPress.put(GLFW_KEY_LEFT_SHIFT, () -> toggleSneak(true));
        Input.onRelease.put(GLFW_KEY_LEFT_SHIFT, () -> toggleSneak(false));
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
        long timeBetweenFrames = currentTime - this.previousFrameTimeNanos;
        this.previousFrameTimeNanos = currentTime;
        if (this.sneakingStateChangePhase) {
            double eyeDistanceBetweenFrames = timeBetweenFrames * eyeOffsetChangePerNanosecond;
            if (!this.sneaking) this.changeSneakingEyeOffset(
                    this.currentEyeOffset + eyeDistanceBetweenFrames,
                    (next) -> next < standardEyeOffset,
                    standardEyeOffset
            );
            else this.changeSneakingEyeOffset(
                    this.currentEyeOffset - eyeDistanceBetweenFrames,
                    (next) -> next > sneakingEyeOffset,
                    sneakingEyeOffset
            );
        }
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

        if (Input.pressed(GLFW_KEY_W)) direction.add(new Vector3f(front));
        if (Input.pressed(GLFW_KEY_S)) direction.sub(new Vector3f(front));
        if (Input.pressed(GLFW_KEY_A)) direction.sub(new Vector3f(right));
        if (Input.pressed(GLFW_KEY_D)) direction.add(new Vector3f(right));
        if (Input.pressed(GLFW_KEY_SPACE)) this.jump();

        changeVelocityByForce(direction.x, direction.y, this.speed()); // изменение велосити игроком
        this.moveLimited(new Vector3d(velocity.x, velocity.y, velocity.z), this.sneaking);

        // изменение велосити окружающей средой
        velocity.z -= 0.08f; // гравитация
        velocity.mul(0.97f, 0.97f, 0.97f); // сопротивление воздуха
        if (onGround) { // трение об землю
            velocity.x *= 0.7f;
            velocity.y *= 0.7f;
        }
    }

    @Override
    public void render(Camera camera) {

    }

    private void toggleSneak(boolean isSneaking) {
        this.sneaking = isSneaking;
        this.sneakingStateChangePhase = true;
    }

    private void changeSneakingEyeOffset(double next, DoublePredicate nextInBounds, double limit) {
        if (!nextInBounds.test(next)) {
            this.currentEyeOffset = limit;
            this.sneakingStateChangePhase = false;
        } else this.currentEyeOffset = next;
    }

    private float speed() {
        if (!this.onGround) return speedInAir;
        return this.sneaking ? speedSneaking : speedStandard;
    }

}