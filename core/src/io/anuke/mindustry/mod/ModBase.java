package io.anuke.mindustry.mod;

public abstract class ModBase{
    /**Called after all base content is loaded.*/
    public void init(){}
    /**Called only clientside, when all assets are loaded.*/
    public void load(){}
}
