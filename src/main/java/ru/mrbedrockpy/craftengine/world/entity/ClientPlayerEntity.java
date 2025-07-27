package ru.mrbedrockpy.craftengine.world.entity;

import lombok.Getter;
import org.joml.*;
import ru.mrbedrockpy.craftengine.event.MouseClickEvent;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.window.Input;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.raycast.BlockRaycastResult;

import static org.lwjgl.glfw.GLFW.*;

public class ClientPlayerEntity extends LivingEntity {
    @Getter
    private final Camera camera = new Camera();
    private final float speed = 1f;
    private final float sensitivity = 20.0f;
    @Getter
    private final float eyeOffset = 1.8f;

    public ClientPlayerEntity(Vector3f position, ClientWorld world) {
        super(position, new Vector3f(0.6f, 1.8f, 0.6f), world);
        this.camera.setPosition(position.add(0, 1.8f, 0));
    }

    @Override
    public void update(float deltaTime, float partialTick, ClientWorld world) {
        super.update(deltaTime, partialTick, world);
        camera.rotate(new Vector2f(
                (float) -Input.getDeltaY() * sensitivity * deltaTime,
                (float) Input.getDeltaX() * sensitivity * deltaTime
        ));
        Vector3f cameraMove = interpolatePosition(prevPosition, position, partialTick);
        camera.setPosition(cameraMove);
    }

    public Vector3f interpolatePosition(Vector3f prevPosition, Vector3f currentPosition, float deltaTime) {
        return new Vector3f(
                prevPosition.x + (currentPosition.x - prevPosition.x) * deltaTime,
                prevPosition.y + (currentPosition.y - prevPosition.y) * deltaTime,
                prevPosition.z + (currentPosition.z - prevPosition.z) * deltaTime
        );
    }

    @Override
    public void tick(){
        super.tick();
        Vector3f direction = new Vector3f();
        Vector3f front = camera.getFlatFront();
        Vector3f right = new Vector3f();
        front.cross(new Vector3f(0, 1, 0), right).normalize();

        if (Input.pressed(GLFW_KEY_A)) direction.add(new Vector3f(front).mul(speed));
        if (Input.pressed(GLFW_KEY_D)) direction.sub(new Vector3f(front).mul(speed));
        if (Input.pressed(GLFW_KEY_W)) direction.add(new Vector3f(right).mul(speed));
        if (Input.pressed(GLFW_KEY_S)) direction.sub(new Vector3f(right).mul(speed));
        if (Input.pressed(GLFW_KEY_SPACE)) jump();
        moveRelative(direction.x, direction.z, this.onGround ? 0.1F : 0.02F);
        velocity.y -= 0.08f;
        this.move(new Vector3d(velocity.x, velocity.y, velocity.z));
        velocity.mul(0.98f, 0.91f, 0.98f);
        if(onGround){
            velocity.x *= 0.7f;
            velocity.z *= 0.7f;
        }
    }

    @Override
    public void render(Camera camera) {
    }

    public void onMouseClick(MouseClickEvent event) {
        if (event.getButton() == GLFW_MOUSE_BUTTON_LEFT) {
            BlockRaycastResult blockRaycastResult = world.raycast(camera.getPosition().add(0, eyeOffset, 0), camera.getFront(), 4.5f);
            if(blockRaycastResult != null){
                world.setBlock(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z, Blocks.AIR);
            }
        } else if (event.getButton() == GLFW_MOUSE_BUTTON_RIGHT) {
            BlockRaycastResult blockRaycastResult = world.raycast(camera.getPosition().add(0, eyeOffset, 0), camera.getFront(), 4.5f);
            if(blockRaycastResult != null){
                Vector3i offset = getOffsetFromDirection(blockRaycastResult.direction);
                Vector3i blockPos = new Vector3i(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z).add(offset);
                world.setBlock(blockPos.x, blockPos.y, blockPos.z, Blocks.STONE);
            }
        }
    }
    // TODO: вынести в отдельный класс, например, BlockUtils
    public Vector3i getOffsetFromDirection(Block.Direction dir) {
        return switch (dir) {
            case UP    -> new Vector3i(0, 1, 0);
            case DOWN  -> new Vector3i(0, -1, 0);
            case NORTH -> new Vector3i(0, 0, -1);
            case SOUTH -> new Vector3i(0, 0, 1);
            case EAST  -> new Vector3i(1, 0, 0);
            case WEST  -> new Vector3i(-1, 0, 0);
            default    -> new Vector3i(0, 0, 0);
        };
    }
}
