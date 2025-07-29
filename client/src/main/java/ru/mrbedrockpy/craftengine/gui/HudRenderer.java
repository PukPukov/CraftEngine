package ru.mrbedrockpy.craftengine.gui;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.gui.DrawContext;

public class HudRenderer {

    public int width;

    public int height;

    public HudRenderer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Texture texture = Texture.load("cursor.png"), hudTexture = Texture.load("hotbar.png");

    public void render(DrawContext context){
        context.drawTexture(width / 2 - 25,height / 2 - 25, 50, 50, texture);
        context.drawTexture(width / 2 - hudTexture.getWidth() / 2 * 5, height - hudTexture.getHeight() * 5, hudTexture.getWidth() * 5, hudTexture.getHeight() * 5, hudTexture);
        context.drawText(String.valueOf(CraftEngineClient.INSTANCE.getFpsCounter().getFPS()), 5, 5, 0.5f);
        context.drawText(positionToString(CraftEngineClient.INSTANCE.getPlayer().getPosition()), 5, 20, 0.5f);
        context.drawText(CraftEngineClient.INSTANCE.getPlayer().getCamera().getAngle().toString(), 5, 35, 0.5f);
    }

    public String positionToString(Vector3f position) {
        return Math.round(position.x) + ", " + Math.round(position.y) + ", " + Math.round(position.z);
    }
}