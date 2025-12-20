package ru.mrbedrockpy.concept.api.renderer;

import ru.mrbedrockpy.concept.api.world.*;

public interface IWorldRenderer {

    void setupCamera(ICamera camera);

    void renderChunk(IChunk chunk);

    void renderEntity(IEntity entity);

    void setSkyboxImage(String path);

    void renderSkybox(ICamera camera);

    void renderParticle(IParticleInstance particle);

}
