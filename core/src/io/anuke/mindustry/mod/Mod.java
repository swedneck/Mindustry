package io.anuke.mindustry.mod;

import io.anuke.arc.files.FileHandle;

public class Mod{
    public final ModMeta meta;
    public final ModBase listener;
    public final Class<?> mainClass;
    public final FileHandle file;

    protected boolean enabled = true;
    protected boolean requiresRestart = true;

    public Mod(FileHandle file, ModMeta meta, ModBase listener){
        this.meta = meta;
        this.file = file;
        this.listener = listener;
        this.mainClass = listener.getClass();
    }

    public boolean requiresRestart(){
        return requiresRestart;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void setEnabled(boolean enabled){
        if(this.enabled != enabled){
            requiresRestart = true;
        }
        this.enabled = enabled;
    }

    public static class ModDependency{
        public final String name;
        public final int version;

        public ModDependency(String name, int version){
            this.name = name;
            this.version = version;
        }
    }

    public static class ModMeta{
        public String name, author, main, description;
        public int version;
        public ModDependency[] dependencies;
    }
}
