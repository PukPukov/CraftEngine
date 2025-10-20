package ru.mrbedrockpy.renderer;

import ru.mrbedrockpy.craftengine.core.cfg.ConfigVars;
import ru.mrbedrockpy.craftengine.core.registry.Registry;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.renderer.api.IResourceManager;
import ru.mrbedrockpy.renderer.graphics.tex.AtlasManager;

public class RenderInit {

    public static IResourceManager RESOURCE_MANAGER;
    public static Registry<Block> BLOCKS;
    public static ConfigVars CONFIG;
    public static AtlasManager ATLAS_MANAGER = new AtlasManager();
    
}