package io.anuke.mindustry.mod;

import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.Log;
import io.anuke.mindustry.Vars;

import java.io.IOException;

public class Mods{
    private Array<Mod> allMods = new Array<>();
    private ModLoader loader = new ModLoader();

    public Array<Mod> all(){
        return allMods;
    }

    /** Loads all mods in the mod directory. Skips mods that don't work.*/
    public void load(){
        for(FileHandle file : Vars.modDirectory.list()){
            try{
                Mod mod = loader.loadMod(file);
                allMods.add(mod);
                mod.requiresRestart = false;
            }catch(IOException e){
                Log.info("Failed to load mod {0}!", file.name());
                Log.err(e);
            }
        }
    }

    public void importMod(FileHandle file) throws IOException{
        Vars.modDirectory.mkdirs();
        FileHandle dest = Vars.modDirectory.child(file.name());
        if(dest.exists()){
            throw new IOException("A mod with the same filename already exists!");
        }

        file.copyTo(dest);
        try{
            allMods.add(loader.loadMod(dest));
        }catch(IOException e){
            dest.delete();
            throw e;
        }catch(Throwable t){
            dest.delete();
            throw new IOException(t);
        }
    }
}
