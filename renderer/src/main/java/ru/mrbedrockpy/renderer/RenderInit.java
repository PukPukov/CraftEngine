package ru.mrbedrockpy.renderer;

import ru.mrbedrockpy.craftengine.core.registry.Registry;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.renderer.api.IResourceManager;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.graphics.TextureAtlas;
import ru.mrbedrockpy.renderer.graphics.tex.Atlas;
import ru.mrbedrockpy.renderer.graphics.tex.AtlasManager;
import ru.mrbedrockpy.renderer.util.graphics.TextureUtil;

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
        ATLAS_MANAGER.close();
        BLOCKS_ATLAS_INDEX = -1;

        blocksAtlasTexture = null;
        blocksAtlasBuilder = null;
    }
}