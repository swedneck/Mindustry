package io.anuke.mindustry.mod;

import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.serialization.Json;

import java.io.IOException;

/**Handles I/O and loading of mod files.
 * Put into a different class to hide the nasty reflection.*/
public abstract class ModLoader{
    protected Json json = new Json();

    public abstract Mod loadMod(FileHandle file) throws IOException;
}
