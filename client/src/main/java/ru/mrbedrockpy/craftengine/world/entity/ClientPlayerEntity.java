package ru.mrbedrockpy.craftengine.world.entity;

import lombok.Getter;
import org.joml.*;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.renderer.api.IPlayerEntity;
import ru.mrbedrockpy.renderer.window.Input;

import static org.lwjgl.glfw.GLFW.*;

public class ClientPlayerEntity extends LivingEntity implements IPlayerEntity {
    @Getter
    private final Camera camera = new Camera();
    private final float speed = 1f;
    private final float sensitivity = 0.03f;
    @Getter
    private final float eyeOffset = 1.8f;
    
    public ClientPlayerEntity(Vector3f position, ClientWorld world) {
        super(position, new Vector3f(0.6f, 0.6f, 1.8f), world);
        this.camera.setPosition(position.add(0, 0, eyeOffset));
    }
    
    @Override
    public void update(double deltaTime, double partialTick, ClientWorld world) {
        super.update(deltaTime, partialTick, world);
        if(!Input.isGUIOpen()) {
            camera.rotate(new Vector2f(
                (float) ((float) -Input.getDeltaY() * sensitivity),
                (float) ((float) -Input.getDeltaX() * sensitivity)
            ));
        }
        Vector3f cameraMove = interpolatePosition(prevPosition, position, partialTick);
        camera.setPosition(cameraMove.add(0, 0, eyeOffset));
    }
    
    public Vector3f interpolatePosition(Vector3f prevPosition, Vector3f currentPosition, double deltaTime) {
        return new Vector3f(
            (float) (prevPosition.x + (currentPosition.x - prevPosition.x) * deltaTime),
            (float) (prevPosition.y + (currentPosition.y - prevPosition.y) * deltaTime),
            (float) (prevPosition.z + (currentPosition.z - prevPosition.z) * deltaTime)
        );
    }
    
    @Override
    public void tick(){
        super.tick();
        Vector3f direction = new Vector3f();
        Vector3f front = camera.getFlatFront();
        Vector3f right = new Vector3f();
        front.cross(new Vector3f(0, 0, 1), right).normalize();
        
        if(!Input.isGUIOpen()) {
            if (Input.pressed(GLFW_KEY_W)) direction.add(new Vector3f(front).mul(speed));
            if (Input.pressed(GLFW_KEY_S)) direction.sub(new Vector3f(front).mul(speed));
            if (Input.pressed(GLFW_KEY_A)) direction.sub(new Vector3f(right).mul(speed));
            if (Input.pressed(GLFW_KEY_D)) direction.add(new Vector3f(right).mul(speed));
            
            if (Input.pressed(GLFW_KEY_SPACE)) jump();
        }
        moveRelative(direction.x, direction.y, this.onGround ? 0.1F : 0.02F);
        velocity.z -= 0.08f;
        this.move(new Vector3d(velocity.x, velocity.y, velocity.z));
        velocity.mul(0.98f, 0.98f, 0.91f);
        if(onGround){
            velocity.x *= 0.7f;
            velocity.y *= 0.7f;
        }
    }
    
    @Override
    public void render(Camera camera) {
    }

    @Override
    public Vector2i getChunkPosition() {
        return super.getChunkPosition();
    }
}