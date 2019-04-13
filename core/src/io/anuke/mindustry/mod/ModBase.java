package io.anuke.mindustry.mod;

public abstract class ModBase{
    /**Called after all base content is loaded.*/
    public void init(){}
    /**Called after all mods and modules are loaded. Initialize everything related to dependencies here.*/
    public void postInit(){}
}
