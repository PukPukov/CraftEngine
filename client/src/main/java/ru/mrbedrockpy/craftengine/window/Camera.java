package ru.mrbedrockpy.craftengine.window;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.Util;
import ru.mrbedrockpy.craftengine.config.ConfigVars;
import ru.mrbedrockpy.craftengine.config.CraftEngineConfiguration;
import ru.mrbedrockpy.renderer.api.ICamera;

public class Camera implements ICamera {
    
    // CONSTANTS
    private static final float standardFovDegrees = ConfigVars.INSTANCE.getFloat("fov");
    private static final float sprintFovDegrees = standardFovDegrees + 10 * ConfigVars.INSTANCE.getFloat("fov.dynamic_multiplier");
    private static final float aspectRatio = 16f / 9f;
    private static final float zNear = 0.1f;
    private static final float zFar = 1000f;
    private static final double fovChangeStepRadians = Math.toRadians(Util.genericLerpStep(sprintFovDegrees, standardFovDegrees, 100));
    
    // STATE
    private boolean fovChangePhase = false;
    private double targetFovRadians = (float) Math.toRadians(standardFovDegrees);
    private double actualFovRadians = targetFovRadians;
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Vector3f position = new Vector3f(0, 0, 0);
    private final Vector2f angle = new Vector2f(0, 0); // pitch (x), yaw (y)
    
    public Camera() {
        updateProjectionMatrix();
        updateViewMatrix();
    }
    
    public void frameUpdate(long previousTime, long currentTime) {
        Util.genericLerp(
            previousTime, currentTime,
            this.actualFovRadians, this.targetFovRadians, this::changeFov,
            fovChangeStepRadians * ConfigVars.INSTANCE.getFloat("fov.dynamic_multiplier"),
            this.fovChangePhase, (phase) -> this.fovChangePhase = phase
        );
        updateProjectionMatrix();
    }
    
    public void walk() {
        this.lerpFovTo(standardFovDegrees);
    }
    
    public void sprint() {
        this.lerpFovTo(sprintFovDegrees);
    }
    
    public void lerpFovTo(float targetFov) {
        this.targetFovRadians = (float) Math.toRadians(targetFov);
        this.fovChangePhase = true;
    }
    
    public void changeFov(double actualFovRadians) {
        this.actualFovRadians = actualFovRadians;
        this.updateProjectionMatrix();
    }
    
    public void setPosition(Vector3f position) {
        this.position.set(position);
        updateViewMatrix();
    }
    
    @Override
    public Vector3f getPosition() {
        return new Vector3f(position);
    }
    
    public void move(Vector3f delta) {
        position.add(delta);
        updateViewMatrix();
    }
    
    public void setAngle(Vector2f angle) {
        this.angle.set(angle);
        updateViewMatrix();
    }
    
    public Vector2f getAngle() {
        return new Vector2f(angle);
    }
    
    public void rotate(Vector2f deltaAngle) {
        angle.add(deltaAngle);
        clampAngles();
        updateViewMatrix();
    }
    
    private void clampAngles() {
        if (angle.x > 89f) angle.x = 89f;
        if (angle.x < -89f) angle.x = -89f;
        
        if (angle.y > 180f) angle.y -= 360f;
        else if (angle.y < -180f) angle.y += 360f;
    }
    
    @Override
    public Matrix4f getViewMatrix() {
        return new Matrix4f(viewMatrix);
    }
    
    @Override
    public Matrix4f getProjectionMatrix() {
        return new Matrix4f(projectionMatrix);
    }
    
    public void updateProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective((float) actualFovRadians, aspectRatio, zNear, zFar);
    }
    
    public void updateViewMatrix() {
        Vector3f front = new Vector3f();
        
        float pitchRad = (float) Math.toRadians(angle.x);
        float yawRad = (float) Math.toRadians(angle.y);
        
        front.x = (float) (Math.cos(pitchRad) * Math.cos(yawRad));
        front.y = (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        front.z = (float) Math.sin(pitchRad);
        front.normalize();
        
        Vector3f eyePosition = new Vector3f(position);
        Vector3f center = new Vector3f(eyePosition).add(front);
        Vector3f up = new Vector3f(0, 0, 1);
        
        viewMatrix.identity();
        viewMatrix.lookAt(eyePosition, center, up);
    }
    
    @Override
    public Vector3f getFront() {
        float yaw = (float) Math.toRadians(angle.y);
        float pitch = (float) Math.toRadians(angle.x);
        
        Vector3f front = new Vector3f();
        front.x = (float) (Math.cos(pitch) * Math.cos(yaw));
        front.y = (float) (Math.cos(pitch) * Math.sin(yaw));
        front.z = (float) Math.sin(pitch);
        
        return front.normalize();
    }
    
    @Override
    public Vector3f getFlatFront() {
        float yaw = (float) Math.toRadians(angle.y);
        
        Vector3f front = new Vector3f();
        front.x = (float) Math.cos(yaw);
        front.y = (float) Math.sin(yaw);
        front.z = 0;
        
        return front.normalize();
    }
}
