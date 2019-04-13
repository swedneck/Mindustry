package io.anuke.mindustry.mod;

import io.anuke.arc.files.FileHandle;
import io.anuke.arc.util.serialization.Json;
import io.anuke.mindustry.mod.Mod.ModMeta;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**Handles I/O and loading of mod files.
 * Put into a different class to hide the nasty reflection.*/
public class ModLoader{
    private Json json = new Json();

    public Mod loadMod(FileHandle file) throws IOException{
        try(ZipFile zip = new ZipFile(file.file())){
            ZipEntry info = zip.getEntry("mod-info.json");
            if(info == null){
                throw new IOException("No mod-info.json found in jar. Is this a mod?");
            }

            ModMeta meta = json.fromJson(ModMeta.class, new BufferedInputStream(zip.getInputStream(info), 1024));

            URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, file.file().toURI().toURL());

            Class<?> main = Class.forName(meta.mainClass);
            return new Mod(meta, (ModBase)main.newInstance());
        }catch(IOException e){
            throw e;
        }catch(Exception e){
            throw new IOException(e);
        }
    }
}
