package forge;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Log;
import arc.util.Tmp;
import mindustry.Vars;

import static forge.ForgeMain.heat;
import static forge.TileHeatControl.kelvins;

public class TileHeatOverlay {
    public void draw(){
        for (int i = 0; i < heat.s; i++) {
            Tmp.v1.set((i % heat.w) * Vars.tilesize, (i / heat.w) * Vars.tilesize);
            Core.camera.bounds(Tmp.r1);
            if(!Tmp.r2.setCentered(Tmp.v1.x, Tmp.v1.y, Vars.tilesize).overlaps(Tmp.r1)) continue;

            float temp = kelvins(TileHeatControl.gridTiles[i].top());

            /*
            Ranges for colors
            Blue: 0 - 303.15
            Green: 0 - 573.15
            Red: 313.15 - 1273.15
             */

            float b = Math.max(1 - temp/303.15f, 0);
            float g = Interp.Pow.slope.apply(Math.max(1 - temp/573.15f, 0));
            float r = Math.max((temp-313.15f)/1273.15f, 0);
            Draw.color(Tmp.c1.set(r, g, b, Math.min(0.15f + Interp.Pow.pow2.apply(Math.max(temp - 1273.15f, 0)/100000), 0.65f)));
            Fill.square(Tmp.v1.x, Tmp.v1.y, 4);
        }
    }
}