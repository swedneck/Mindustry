package io.anuke.mindustry.ui.dialogs;

import io.anuke.arc.Core;
import io.anuke.mindustry.core.Platform;
import io.anuke.mindustry.graphics.Pal;
import io.anuke.mindustry.mod.Mod;

import java.io.IOException;

import static io.anuke.mindustry.Vars.mods;
import static io.anuke.mindustry.Vars.ui;

public class ModsDialog extends FloatingDialog{

    public ModsDialog(){
        super("$mods");
        addCloseButton();
        shown(this::setup);
    }

    void setup(){
        cont.clear();
        cont.defaults().width(300f).pad(4);
        if(!mods.all().isEmpty()){
            cont.pane(table -> {
                table.margin(10f);
                for(Mod mod : mods.all()){
                    table.table("flat", t -> {
                        t.defaults().pad(2);
                        t.margin(10f).left();
                        t.add(mod.meta.name).color(Pal.accent);
                        t.row();
                        t.add("v[LIGHT_GRAY]" + mod.meta.version);
                        t.row();
                        t.label(() -> mod.isEnabled() ? "$mod.enabled" : "$mod.disabled");
                        t.row();
                        if(mod.requiresRestart()){
                            t.add("$mod.requiresrestart");
                        }
                    }).height(160f).width(280f);
                    table.row();
                }
            });

        }else{
            cont.table("flat", t -> t.add("$mods.none")).height(50f);
        }

        cont.row();

        cont.addImageTextButton("$mod.import", "icon-add", 14 * 3, () -> {
            Platform.instance.showFileChooser(Core.bundle.get("mod.import"), ".jar File", file -> {
                try{
                    mods.importMod(file);
                    setup();
                }catch(IOException e){
                    ui.showError(e.getMessage());
                }
            }, true, "jar");
        }).margin(10f);
    }
}
