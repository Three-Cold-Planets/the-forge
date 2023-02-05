package forge;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Interp;
import arc.util.Tmp;

import static forge.ForgeMain.heat;

public class TileHeatOverlay {
    public void draw(){
        for (int i = 0; i < heat.s; i++) {
            float temp = heat.kelvins(i);


            /*
            Ranges for colors
            Blue: 0 - 303.15
            Green: 0 - 573.15
            Red: 313.15 - 1273.15
             */


            float b = Math.max(1 - temp/303.15f, 0);
            float g = Interp.Pow.slope.apply(Math.max(1 - temp/573.15f, 0));
            float r = Math.max((temp-313.15f)/1273.15f, 0);
            Draw.color(Tmp.c1.set(r, g, b, 0.15f));
            Fill.square((i % heat.w) * 8, ((int) (i / heat.w)) * 8, 8);
        }
    }
}
