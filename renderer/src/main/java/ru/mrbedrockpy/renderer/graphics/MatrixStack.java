package ru.mrbedrockpy.renderer.graphics;

import org.joml.Matrix4f;

import java.util.Stack;

public class MatrixStack {

    private final Stack<Matrix4f> stack = new Stack<>();
    private Matrix4f current;

    public MatrixStack() {
        current = new Matrix4f().identity();
    }

    public void push() {
        stack.push(new Matrix4f(current));
    }

    public void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Matrix stack underflow: too many pops.");
        }
        current = stack.pop();
    }

    public void translate(float x, float y, float z) {
        current.translate(x, y, z);
    }

    public void scale(float x, float y, float z) {
        current.scale(x, y, z);
    }

    public void rotateX(float radians) {
        current.rotateX(radians);
    }

    public void rotateY(float radians) {
        current.rotateY(radians);
    }

    public void rotateZ(float radians) {
        current.rotateZ(radians);
    }

    public Matrix4f getMatrix() {
        return current;
    }

    public void set(Matrix4f matrix) {
        this.current = new Matrix4f(matrix);
    }

    public Matrix4f peekCopy() {
        return new Matrix4f(current);
    }
}