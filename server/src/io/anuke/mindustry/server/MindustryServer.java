package io.anuke.mindustry.server;

import io.anuke.arc.*;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.core.*;
import io.anuke.mindustry.game.Content;
import io.anuke.mindustry.game.EventType.GameLoadEvent;
import io.anuke.mindustry.io.BundleLoader;
import io.anuke.mindustry.mod.Mod;

import static io.anuke.mindustry.Vars.*;

public class MindustryServer extends ApplicationCore{
    private String[] args;

    public MindustryServer(String[] args){
        this.args = args;
    }

    @Override
    public void setup(){
        Core.settings.setDataDirectory(Core.files.local("config"));
        loadLocales = false;
        Vars.init();

        headless = true;

        BundleLoader.load();
        content.verbose(false);
        content.load();

        add(logic = new Logic());
        add(world = new World());
        add(netServer = new NetServer());
        add(new ServerControl(args));

        content.initialize(Content::init);
    }

    @Override
    public void init(){
        super.init();

        for(Mod mod : mods.all()){
            mod.listener.postInit();
        }

        Events.fire(new GameLoadEvent());
    }
}
