package ru.mrbedrockpy.craftengine.render;


import ru.mrbedrockpy.craftengine.registry.Registry;
import ru.mrbedrockpy.craftengine.render.api.IResourceManager;
import ru.mrbedrockpy.craftengine.render.graphics.Texture;
import ru.mrbedrockpy.craftengine.render.graphics.TextureAtlas;
import ru.mrbedrockpy.craftengine.render.graphics.tex.AtlasManager;
import ru.mrbedrockpy.craftengine.render.util.graphics.TextureUtil;
import ru.mrbedrockpy.craftengine.util.id.RL;
import ru.mrbedrockpy.craftengine.world.block.Block;

import java.util.Map;

public class RenderInit {

    public static IResourceManager RESOURCE_MANAGER;
    public static Registry<Block> BLOCKS;

    public static final AtlasManager ATLAS_MANAGER = new AtlasManager();

    private static TextureAtlas blocksAtlasBuilder;
    private static Texture blocksAtlasTexture;
    private static int BLOCKS_ATLAS_INDEX = -1;

    public static void init() {
        ensureAtlases();
    }

    public static void ensureAtlases() {
        if (BLOCKS_ATLAS_INDEX != -1) return;

        blocksAtlasBuilder = new TextureAtlas(16);

        for (Map.Entry<RL, Texture> e : RESOURCE_MANAGER.getTextureLoader().getAll()) {
            if (!e.getKey().path().startsWith("block/")) continue;
            blocksAtlasBuilder.addTile(e.getKey(), TextureUtil.toBufferedImage(e.getValue()));
        }

        blocksAtlasTexture = blocksAtlasBuilder.buildAtlas();
        BLOCKS_ATLAS_INDEX = ATLAS_MANAGER.register(blocksAtlasBuilder);
    }

    public static int blocksAtlasIndex() {
        ensureAtlases();
        return BLOCKS_ATLAS_INDEX;
    }

    public static TextureAtlas blocksAtlasBuilder() {
        ensureAtlases();
        return blocksAtlasBuilder;
    }

    public static void shutdown() {
        Shaders.cleanup();
        ATLAS_MANAGER.close();
        BLOCKS_ATLAS_INDEX = -1;

        blocksAtlasTexture = null;
        blocksAtlasBuilder = null;
    }
}