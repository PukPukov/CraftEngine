package ru.mrbedrockpy.concept.api.world;

import org.joml.Vector3d;

public interface IParticleInstance {

    Vector3d getPosition();

    int getCount();



    IParticle getParticle();

}
