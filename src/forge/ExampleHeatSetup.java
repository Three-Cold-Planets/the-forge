package forge;

import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.util.Time;
import arc.util.Tmp;
import forge.TileHeatControl.HeatState;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.graphics.Layer;

import static forge.ForgeMain.heatOverlay;
import static forge.TileHeatControl.*;
import static mindustry.Vars.state;

//Example of a setup class
public class ExampleHeatSetup extends TileHeatSetup{
    TileHeatControl heat;
    @Override
    void setupGrid(TileHeatControl heat) {
        for (int i = 0; i < heat.s; i++) {
            boolean solid = Vars.world.tile(i % heat.w, (int) Math.floor(i/heat.w)).solid();
            heat.setTileValues(i, heat.ambientTemperature * (solid ? 10 * defaultBlock.specificHeatCapacity : 1 * defaultFloor.specificHeatCapacity), solid ? 10 : 1, solid ? defaultBlock : defaultFloor);
        }
    }

    @Override
    void update(TileHeatControl heat) {
        Tmp.v1.set(Vars.player);
        Fx.smoke.at(Tmp.v1.x, Tmp.v1.y);
        int index =(int) (Math.floor(Tmp.v1.x/8) + Math.floor(Tmp.v1.y/8) * heat.w);
        Tmp.v2.set((index/8 % heat.w) * 8, Mathf.floor(index/8) * heat.w * 8);
        Fx.smoke.at(Tmp.v2.x, Tmp.v2.y);
        if(index >= heat.s || index < 0) return;
        gridStates.get(index).flow += 750;
    }

    @Override
    void initialize(TileHeatControl heat) {

        this.heat = heat;

        Events.run(EventType.Trigger.update, () -> {
            if (state.isGame() && !state.isPaused()) {
                    if (heat.heatThread == null) {
                        heat.setupThread();
                    }
                    heat.heatThread.updateTime(Time.delta * 0.25f);
                }
        });

        Events.on(EventType.TileChangeEvent.class, event -> {
            if(heat.gridLoaded){
                if(event.tile.build != null){
                    updateBuildTerrain(event.tile.build);
                }else{
                    updateTerrain(event.tile.x, event.tile.y);
                }
            }
        });
        //on tile removed
        Events.on(EventType.TilePreChangeEvent.class, event -> {
            if(heat.gridLoaded){
                if(event.tile.build != null){
                    updateBuildTerrain(event.tile.build);
                }else{
                    updateTerrain(event.tile.x, event.tile.y);
                }
            }
        });

        Events.run(EventType.Trigger.draw, () -> {
            if(state.isGame() && heat.gridLoaded) Draw.draw(Layer.power + 1, heatOverlay::draw);
        });
    }

    public void updateTerrain(int x, int y){
        boolean solid = Vars.world.tile(x, y).solid();
        int index = x + y * heat.w;
        HeatState state = gridStates.get(index);
        state.mass = solid ? 10 : 1;
        state.material = solid ? defaultBlock : defaultFloor;
    };

    public void updateBuildTerrain(Building b){
        if(b.block.size == 1){
            updateTerrain(b.tile.x, b.tile.y);
        }else{
            int offset = (b.block.size - 1) / 2;
            for(int y = b.tile.y - offset; y < b.tile.y - offset + b.block.size; y++){
                for(int x = b.tile.x - offset; x < b.tile.x - offset + b.block.size; x++){
                    updateTerrain(x, y);
                }
            }
        }
    };
}
