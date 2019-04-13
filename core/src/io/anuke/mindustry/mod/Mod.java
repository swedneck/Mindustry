package io.anuke.mindustry.mod;

public class Mod{
    public final ModMeta meta;
    public final ModBase listener;
    public final Class<?> mainClass;

    protected boolean enabled = true;
    protected boolean requiresRestart = true;

    public Mod(ModMeta meta, ModBase listener){
        this.meta = meta;
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
