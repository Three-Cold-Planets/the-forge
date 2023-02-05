package forge;

import arc.*;
import arc.graphics.g2d.Draw;
import arc.struct.Seq;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import mindustry.graphics.Layer;
import mindustry.io.SaveVersion;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import rhino.ImporterTopLevel;
import rhino.NativeJavaPackage;

public class ForgeMain extends Mod{

    public static TileHeatControl heat;
    public static TileHeatOverlay heatOverlay;
    public static NativeJavaPackage p;
    public ForgeMain(){
        Log.info("Loaded ExampleJavaMod constructor.");

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("behold").row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(Core.atlas.find("example-java-mod-frog")).pad(20f).row();
                dialog.cont.button("I see", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });

        heat = new TileHeatControl();
        heatOverlay = new TileHeatOverlay();

        heat.setup = new ExampleHeatSetup();

        SaveVersion.addCustomChunk("forge-THC", heat);
        Events.run(EventType.SaveLoadEvent.class, () -> {
            heat.start(Vars.world.width(), Vars.world.height());
        });
        
        Events.run(Trigger.draw, () -> {
            Draw.draw(Layer.overlayUI, heatOverlay::draw);
        });

        Events.run(Trigger.update, () -> {
            if(!Vars.state.isPlaying()) return;
            heat.tick(Time.delta);
        });
    }

    @Override
    public void init() {
        ImporterTopLevel scope = (ImporterTopLevel) Vars.mods.getScripts().scope;

        Seq<String> packages = Seq.with(
                "forge"
        );

        packages.each(name -> {
            p = new NativeJavaPackage(name, Vars.mods.mainLoader());

            p.setParentScope(scope);

            scope.importPackage(p);
        });
    }

    @Override
    public void loadContent(){
        Log.info("Loading some example content.");
    }

}
