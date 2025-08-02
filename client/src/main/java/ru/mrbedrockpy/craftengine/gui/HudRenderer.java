package ru.mrbedrockpy.craftengine.gui;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import java.text.DecimalFormat;

public class HudRenderer {
    
    private final DecimalFormat decimalFormat = new DecimalFormat();
    
    public int width;
    public int height;

    public HudRenderer(int width, int height) {
        this.decimalFormat.setMaximumFractionDigits(3);
        this.decimalFormat.setMinimumFractionDigits(3);
        this.width = width;
        this.height = height;
    }

    public Texture texture = Texture.load("cursor.png"), hudTexture = Texture.load("hotbar.png");

    public void render(DrawContext context){
        context.drawTexture(width / 2 - 25,height / 2 - 25, 50, 50, texture);
        context.drawTexture(width / 2 - hudTexture.width() / 2 * 5, height - hudTexture.height() * 5, hudTexture.width() * 5, hudTexture.height() * 5, hudTexture);
        context.drawText(String.valueOf(CraftEngineClient.INSTANCE.fpsCounter().fps()), 5, 5, 0.5f);
        context.drawText(positionToString(CraftEngineClient.INSTANCE.player().nextTickPosition()), 5, 20, 0.5f);
        context.drawText(CraftEngineClient.INSTANCE.player().camera().angle().toString(), 5, 35, 0.5f);
    }
    
    public String positionToString(Vector3f position) {
        return this.decimalFormat.format(position.x) + ", " + this.decimalFormat.format(position.y) + ", " + this.decimalFormat.format(position.z);
    }
}