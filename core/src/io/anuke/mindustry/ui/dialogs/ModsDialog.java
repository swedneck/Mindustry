package io.anuke.mindustry.ui.dialogs;

import io.anuke.arc.Core;
import io.anuke.mindustry.core.Platform;
import io.anuke.mindustry.mod.Mod;

import java.io.IOException;

import static io.anuke.mindustry.Vars.*;

public class ModsDialog extends FloatingDialog{

    public ModsDialog(){
        super("$mods");
        addCloseButton();
        shown(this::setup);
    }

    void setup(){
        cont.clear();
        cont.defaults().width(520f).pad(4);
        if(!mods.all().isEmpty()){
            cont.pane(table -> {
                table.margin(10f).top();
                for(Mod mod : mods.all()){
                    table.table("pane", t -> {
                        t.defaults().pad(2).left().top();
                        t.margin(14f).left();
                        t.add("[accent]" + mod.meta.name + "[lightgray] v" + mod.meta.version);
                        t.row();
                        t.label(() -> mod.isEnabled() ? "$mod.enabled" : "$mod.disabled");
                        t.row();
                        if(mod.meta.author != null){
                            t.add(Core.bundle.format("mod.author", mod.meta.author));
                            t.row();
                        }
                        if(mod.meta.description != null){
                            t.labelWrap("[lightgray]" + mod.meta.description).growX();
                            t.row();
                        }
                        if(mod.requiresRestart()){
                            t.add("$mod.requiresrestart");
                        }
                        t.row();
                        t.table(buttons -> {
                            buttons.defaults().size(iconsize);

                            buttons.addImageButton("icon-trash", "clear", iconsize, () -> ui.showConfirm("$confirm", "$mod.remove.confirm", () -> {
                                mods.removeMod(mod);
                                mod.setEnabled(false);
                                mod.setRequiresRestart(true);
                                setup();
                            })).disabled(b -> mod.requiresRestart());
                            //buttons.addImageButton("clear", "icon-trash-small", iconsizesmall, () -> ui.showConfirm("$confirm", "$mod.delete.confirm", () -> {

                            //}));
                        }).pad(-10f).padTop(4);

                    }).width(500f);
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
                    e.printStackTrace();
                }
            }, true, s -> s.equals("jar"));
        }).margin(10f).width(500f);
    }
}
