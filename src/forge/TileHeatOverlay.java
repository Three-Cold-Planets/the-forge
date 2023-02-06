package forge;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Tmp;

import static forge.ForgeMain.heat;

public class TileHeatOverlay {
    public void draw(){
        if(heat.tilePropertyAssociations.size <= 0) return;
        for (int i = 0; i < heat.s; i++) {
            float temp = heat.kelvins(i)/2 + heat.kelvins(i + heat.s)/2;


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
            Fill.square((i % heat.w) * 8, ((int) (i / heat.w)) * 8, 4);
        }
    }
}
