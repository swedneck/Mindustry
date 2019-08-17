package io.anuke.mindustry.mod;

import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;
import io.anuke.mindustry.mod.Mods.*;

import java.util.zip.*;

public class Mod{
    /** Metadata parsed from JSON.*/
    public final ModMeta meta;
    /** Base class instance.*/
    public final ModBase listener;
    /** Class to load mod from, extends BaseMod.*/
    public final Class<?> mainClass;
    /** Local mod .jar file.*/
    public final FileHandle file;

    /** All stream files inside the jar files. These only support read() and name().*/
    public Array<ZipFileHandle> zipFiles = new Array<>();
    /** Root zip file handle.*/
    public ZipFileHandle zipRoot;
    /** The zip file object itself.*/
    public ZipFile zip;
    /** Locales in the mod.*/
    public ObjectMap<String, FileHandle> bundles = new ObjectMap<>();

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

    public void setRequiresRestart(boolean requiresRestart){
        this.requiresRestart = requiresRestart;
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
