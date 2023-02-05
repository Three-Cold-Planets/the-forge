package forge;

import arc.math.geom.Vec2;
import arc.util.Tmp;
import mindustry.Vars;

//Example of a setup class
public class ExampleHeatSetup extends TileHeatSetup{
    @Override
    void setupGrid(TileHeatControl heat) {
        for (int i = 0; i < heat.s; i++) {
            heat.setTileValues(i, heat.ambientTemperature * 1000, 1000, TileHeatControl.defaultPreset);
        }
    }

    @Override
    void update(TileHeatControl heat) {
        Tmp.v1.set(Vars.player);
        int index =(int) (Math.floor(Tmp.v1.x/8) + Math.floor(Tmp.v1.y/8) * heat.w);
        heat.energyValues[index] += heat.massValues[index] * heat.tilePropertyAssociations.get(index).specificHeatCapacity;
    }
}
