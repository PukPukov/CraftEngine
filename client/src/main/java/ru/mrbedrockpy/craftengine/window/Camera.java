package ru.mrbedrockpy.craftengine.window;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.mrbedrockpy.renderer.api.ICamera;

public class Camera implements ICamera {
    
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Vector3f position = new Vector3f(0,0,0);
    private final Vector2f angle = new Vector2f(0,0); // pitch (x), yaw (y)
    
    private final float fov = (float) Math.toRadians(80.0);
    private final float aspectRatio = 16f / 9f;
    private final float zNear = 0.1f;
    private final float zFar = 1000f;
    
    public Camera() {
        updateProjectionMatrix();
        updateViewMatrix();
    }
    
    public void position(Vector3f position) {
        this.position.set(position);
        updateViewMatrix();
    }
    
    @Override
    public Vector3f position() {
        return new Vector3f(position);
    }
    
    public void move(Vector3f delta) {
        position.add(delta);
        updateViewMatrix();
    }
    
    public void angle(Vector2f angle) {
        this.angle.set(angle);
        updateViewMatrix();
    }
    
    public Vector2f angle() {
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
    public Matrix4f viewMatrix() {
        return new Matrix4f(viewMatrix);
    }
    
    @Override
    public Matrix4f projectionMatrix() {
        return new Matrix4f(projectionMatrix);
    }
    
    public void updateProjectionMatrix() {
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
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
    public Vector3f front() {
        float yaw = (float) Math.toRadians(angle.y);
        float pitch = (float) Math.toRadians(angle.x);
        
        Vector3f front = new Vector3f();
        front.x = (float) (Math.cos(pitch) * Math.cos(yaw));
        front.y = (float) (Math.cos(pitch) * Math.sin(yaw));
        front.z = (float) Math.sin(pitch);
        
        return front.normalize();
    }
    
    @Override
    public Vector3f flatFront() {
        float yaw = (float) Math.toRadians(angle.y);
        
        Vector3f front = new Vector3f();
        front.x = (float) Math.cos(yaw);
        front.y = (float) Math.sin(yaw);
        front.z = 0;
        
        return front.normalize();
    }
    
}