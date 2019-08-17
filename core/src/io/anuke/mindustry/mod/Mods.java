package io.anuke.mindustry.mod;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.files.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.Pixmap.*;
import io.anuke.arc.graphics.Texture.*;
import io.anuke.arc.graphics.g2d.*;
import io.anuke.arc.util.*;
import io.anuke.arc.util.io.*;
import io.anuke.mindustry.*;
import io.anuke.mindustry.core.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import static io.anuke.mindustry.Vars.*;

public class Mods{
    private Array<Mod> allMods = new Array<>();
    private ModLoader loader = Platform.instance.getModLoader();
    private ObjectSet<String> localesUsed = new ObjectSet<>();

    public Array<Mod> all(){
        return allMods;
    }

    /** Loads all mods in the mod directory. Skips mods that don't work.*/
    public void load(){

        for(FileHandle file : Vars.modDirectory.list()){
            try{
                Mod mod = loader.loadMod(file);
                loadFiles(mod);
                allMods.add(mod);
                mod.requiresRestart = false;
            }catch(IOException e){
                Log.info("Failed to load mod {0}!", file.name());
                if(!headless) ui.showError(file.nameWithoutExtension() + ":\n" + Strings.parseException(e, true));
                Log.err(e);
            }
        }

        Vars.modLocaleDirectory.deleteDirectory();

        //load up the bundles.
        for(String used : localesUsed){
            FileHandle intern = Core.files.internal("bundles/" + used + ".properties");
            FileHandle dest = Vars.modLocaleDirectory.child(intern.name());
            if(intern.exists()){
                intern.copyTo(dest);

                for(Mod mod : allMods){
                    if(mod.bundles.containsKey(used)){
                        dest.writeString("\n" + mod.bundles.get(used).readString() + "\n", true);
                    }
                }
            }
        }
    }

    private void loadFiles(Mod mod) throws IOException{
        ZipFile zip = new ZipFile(mod.file.file());
        mod.zipFiles = Array.with(Collections.list(zip.entries()).toArray(new ZipEntry[0])).map(entry -> new ZipFileHandle(entry, zip));
        mod.zipFiles.add(mod.zipRoot = new ZipFileHandle());
        mod.zipFiles.each(f -> f.init(mod.zipFiles));

        int bundles = 0;

        //load bundles.
        for(FileHandle bundle : mod.zipRoot.child("bundles").list()){
            if(bundle.extension().equals("properties") && bundle.name().startsWith("bundle")){
                bundles ++;
                localesUsed.add(bundle.nameWithoutExtension());
                mod.bundles.put(bundle.nameWithoutExtension(), bundle);
            }
        }

        Log.info("Found {0} bundles for mod '{1}'.", bundles, mod.meta.name);
    }

    public void packSprites(){
        PixmapPacker packer = new PixmapPacker(2048, 2048, Format.RGBA8888, 2, true);
        for(Mod mod : allMods){
            try{
                int packed = 0;
                for(FileHandle file : mod.zipRoot.child("sprites").list()){
                    if(file.extension().equals("png")){
                        try(InputStream stream = file.read()){
                            byte[] bytes = Streams.copyStreamToByteArray(stream, Math.max((int)file.length(), 512));
                            Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
                            packer.pack(mod.meta.name + ":" + file.nameWithoutExtension(), pixmap);
                            pixmap.dispose();
                            packed ++;
                        }
                    }
                }
                Log.info("Packed {0} images for mod '{1}'.", packed, mod.meta.name);
            }catch(IOException e){
                Log.err("Error packing images for mod: {0}", mod.meta.name);
                e.printStackTrace();
                if(!headless) ui.showError(mod.meta.name + ":\n" + Strings.parseException(e, true));
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

    public class ZipFileHandle extends FileHandleStream{
        private ZipFileHandle[] children = {};
        private ZipFileHandle parent;

        private final ZipEntry entry;
        private final ZipFile zip;

        public ZipFileHandle(){
            super("");
            zip = null;
            entry = null;
        }

        public ZipFileHandle(ZipEntry entry, ZipFile file){
            super(entry.getName());
            this.entry = entry;
            this.zip = file;
        }

        private void init(Array<ZipFileHandle> files){
            parent = files.find(other -> other != this && path().startsWith(other.path()) && !path().substring(1 + other.path().length()).contains("/"));

            if(isDirectory()){
                children = files.select(z -> z != this && z.path().startsWith(path()) && !z.path().substring(1 + path().length()).contains("/")).toArray(ZipFileHandle.class);
            }
        }

        @Override
        public FileHandle child(String name){
            for(ZipFileHandle child : children){
                if(child.name().equals(name)){
                    return child;
                }
            }
            return new FileHandle(new File(file, name)){
                @Override
                public boolean exists(){
                    return false;
                }
            };
        }

        @Override
        public String name(){
            return file.getName();
        }


        @Override
        public FileHandle parent(){
            return parent;
        }

        @Override
        public FileHandle[] list(){
            return children;
        }

        @Override
        public boolean isDirectory(){
            return entry == null || entry.isDirectory();
        }

        @Override
        public InputStream read(){
            if(entry == null) throw new RuntimeException("Not permitted.");
            try{
                return zip.getInputStream(entry);
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }

        @Override
        public long length(){
            return isDirectory() ? 0 : entry.getSize();
        }
    }
}
