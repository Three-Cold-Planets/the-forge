package forge;

import arc.Core;
import arc.math.geom.Vec2;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.Vars;

//Example of a setup class
public class ExampleHeatSetup extends TileHeatSetup{
    @Override
    void setupGrid(TileHeatControl heat) {
        for (int i = 0; i < heat.s; i++) {
            //Log.info("X: " + i % heat.w + ", Y: " + Math.floor(i/heat.w));
            boolean solid = Vars.world.tile(i % heat.w, (int) Math.floor(i/heat.w)).solid();
            heat.setTileValues(i, heat.ambientTemperature * 1000, 1000, TileHeatControl.defaultFloor);
            heat.setTileValues(i + heat.s, heat.ambientTemperature * 1000, solid ? 10000 : 1000, solid ? TileHeatControl.defaultBlock : TileHeatControl.defaultAir);
        }
    }

    @Override
    void update(TileHeatControl heat) {
        Tmp.v1.set(Vars.player);
        int index =(int) (Math.floor(Tmp.v1.x/8) + Math.floor(Tmp.v1.y/8) * heat.w);
        heat.energyValues[index] += heat.massValues[index] * heat.tilePropertyAssociations.get(index).specificHeatCapacity * 2;

        Tmp.v1.set(Core.input.mouseWorld());
        index =(int) (Math.floor(Tmp.v1.x/8) + Math.floor(Tmp.v1.y/8) * heat.w);
        if(index < 0) return;
        Log.info("Flow stats for tile cursor is on:\nTemp:" + heat.celsius(index) + heat.s + "\nFlow total:" + heat.totalFlow(index + heat.s));
    }
}
