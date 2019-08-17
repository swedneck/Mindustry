package io.anuke.mindustry.mod;

import io.anuke.arc.Core;
import io.anuke.arc.collection.Array;
import io.anuke.arc.files.FileHandle;
import io.anuke.arc.graphics.Pixmap;
import io.anuke.arc.graphics.Pixmap.Format;
import io.anuke.arc.graphics.Texture.TextureFilter;
import io.anuke.arc.graphics.g2d.PixmapPacker;
import io.anuke.arc.util.Log;
import io.anuke.arc.util.io.Streams;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.core.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Mods{
    private Array<Mod> allMods = new Array<>();
    private ModLoader loader = Platform.instance.getModLoader();

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

    public void packSprites(){
        PixmapPacker packer = new PixmapPacker(1024, 1024, Format.RGBA8888, 2, true);
        for(Mod mod : allMods){
            try{
                int packed = 0;
                try(ZipFile zip = new ZipFile(mod.file.file())){
                    for(ZipEntry entry : Collections.list(zip.entries())){
                        if(entry.getName().startsWith("sprites/") && entry.getName().toLowerCase().endsWith(".png")){
                            String fileName = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                            fileName = fileName.substring(0, fileName.length() - 4);
                            try(InputStream stream = zip.getInputStream(entry)){
                                byte[] bytes = Streams.copyStreamToByteArray(stream, Math.max((int)entry.getSize(), 512));
                                Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
                                packer.pack(mod.meta.name + ":" + fileName, pixmap);
                                pixmap.dispose();
                                packed ++;
                            }
                        }
                    }
                }
                Log.info("Packed {0} images for mod '{1}'.", packed, mod.meta.name);
            }catch(IOException e){
                Log.err("Error packing images for mod: {0}", mod.meta.name);
                e.printStackTrace();
            }
        }

        packer.getPages().each(page -> page.updateTexture(TextureFilter.Nearest, TextureFilter.Nearest, false));
        packer.getPages().each(page -> page.getRects().each((name, rect) -> Core.atlas.addRegion(name, page.getTexture(), (int)rect.x, (int)rect.y, (int)rect.width, (int)rect.height)));
    }

    public void removeMod(Mod mod){
        mod.file.delete();

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
