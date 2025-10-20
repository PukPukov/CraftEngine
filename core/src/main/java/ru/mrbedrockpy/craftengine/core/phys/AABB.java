package ru.mrbedrockpy.craftengine.core.phys;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class AABB {
    
    private final double epsilon = 0.0F;
    
    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;
    
    public AABB(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    @Override
    public AABB clone() {
        return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    public AABB expand(double x, double y, double z) {
        double minX = this.minX;
        double minY = this.minY;
        double minZ = this.minZ;
        double maxX = this.maxX;
        double maxY = this.maxY;
        double maxZ = this.maxZ;
        
        if (x < 0.0F) {
            minX += x;
        } else {
            maxX += x;
        }
        
        if (y < 0.0F) {
            minY += y;
        } else {
            maxY += y;
        }
        
        if (z < 0.0F) {
            minZ += z;
        } else {
            maxZ += z;
        }
        
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public AABB expand(Vector3d value) {
        return this.expand(value.x, value.y, value.z);
    }
    
    public AABB grow(double x, double y, double z) {
        return new AABB(this.minX - x, this.minY - y,
            this.minZ - z, this.maxX + x,
            this.maxY + y, this.maxZ + z);
    }
    
    public double clipXCollide(AABB otherBoundingBox, double x) {
        if (otherBoundingBox.maxY <= this.minY || otherBoundingBox.minY >= this.maxY) {
            return x;
        }
        
        if (otherBoundingBox.maxZ <= this.minZ || otherBoundingBox.minZ >= this.maxZ) {
            return x;
        }
        
        if (x > 0.0F && otherBoundingBox.maxX <= this.minX) {
            double max = this.minX - otherBoundingBox.maxX - this.epsilon;
            if (max < x) {
                x = max;
            }
        }
        
        if (x < 0.0F && otherBoundingBox.minX >= this.maxX) {
            double max = this.maxX - otherBoundingBox.minX + this.epsilon;
            if (max > x) {
                x = max;
            }
        }
        
        return x;
    }
    
    public double clipYCollide(AABB otherBoundingBox, double y) {
        if (otherBoundingBox.maxX <= this.minX || otherBoundingBox.minX >= this.maxX) {
            return y;
        }
        
        if (otherBoundingBox.maxZ <= this.minZ || otherBoundingBox.minZ >= this.maxZ) {
            return y;
        }
        
        if (y > 0.0F && otherBoundingBox.maxY <= this.minY) {
            double max = this.minY - otherBoundingBox.maxY - this.epsilon;
            if (max < y) {
                y = max;
            }
        }
        
        if (y < 0.0F && otherBoundingBox.minY >= this.maxY) {
            double max = this.maxY - otherBoundingBox.minY + this.epsilon;
            if (max > y) {
                y = max;
            }
        }
        
        return y;
    }

    public double clipZCollide(AABB otherBoundingBox, double z) {
        if (otherBoundingBox.maxX <= this.minX || otherBoundingBox.minX >= this.maxX) {
            return z;
        }
        
        if (otherBoundingBox.maxY <= this.minY || otherBoundingBox.minY >= this.maxY) {
            return z;
        }
        
        if (z > 0.0F && otherBoundingBox.maxZ <= this.minZ) {
            double max = this.minZ - otherBoundingBox.maxZ - this.epsilon;
            if (max < z) {
                z = max;
            }
        }
        
        if (z < 0.0F && otherBoundingBox.minZ >= this.maxZ) {
            double max = this.maxZ - otherBoundingBox.minZ + this.epsilon;
            if (max > z) {
                z = max;
            }
        }
        
        return z;
    }
    
    public boolean intersects(AABB otherBoundingBox) {
        // Check on X axis
        if (otherBoundingBox.maxX <= this.minX || otherBoundingBox.minX >= this.maxX) {
            return false;
        }

        // Check on Y axis
        if (otherBoundingBox.maxY <= this.minY || otherBoundingBox.minY >= this.maxY) {
            return false;
        }

        // Check on Z axis
        return !(otherBoundingBox.maxZ <= this.minZ || otherBoundingBox.minZ >= this.maxZ);
    }

    public void move(double x, double y, double z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
    }

    public AABB offset(double x, double y, double z) {
        return new AABB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }
    
    public Vector3f root() {
        return new Vector3f(
            (float) ((this.minX + this.maxX) / 2.0D),
            (float) ((this.minY + this.maxY) / 2.0D),
            (float) this.minZ
        );
    }
    
}