package forge;

import arc.*;
import arc.graphics.gl.FrameBuffer;
import arc.struct.Seq;
import arc.util.*;
import forge.TileHeatControl.HeatState;
import mindustry.*;
import mindustry.game.EventType;
import mindustry.game.EventType.*;
import mindustry.io.SaveVersion;
import mindustry.mod.*;
import rhino.ImporterTopLevel;
import rhino.NativeJavaPackage;

import static forge.TileHeatControl.kelvins;

public class ForgeMain extends Mod{

    public static TileHeatControl heat;
    public static TileHeatOverlay heatOverlay;
    public static NativeJavaPackage p;

    public static FrameBuffer effectBuffer;
    public static HeatmapShader heatShader;
    public ForgeMain(){

        heat = new TileHeatControl();
        heatOverlay = new TileHeatOverlay();

        heat.setup = new ExampleHeatSetup();

        SaveVersion.addCustomChunk("forge-THC", heat);

        heat.setup.initialize(heat);
        Events.run(EventType.WorldLoadEvent.class, () -> {
            heat.start(Vars.world.width(), Vars.world.height());
        });

        Events.run(FileTreeInitEvent.class, () -> {
            Core.app.post(() -> {
                effectBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
                try {
                    heatShader = new HeatmapShader("heat");
                }
                catch (IllegalArgumentException error){
                    Log.err("Failed to load Heat shader: " + error);
                }
            });
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
