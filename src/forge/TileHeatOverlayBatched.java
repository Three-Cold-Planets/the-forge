package forge;

import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.util.Tmp;
import mindustry.Vars;

import static forge.ForgeMain.*;

public class TileHeatOverlayBatched {
    public static Pixmap heatmap;
    public static TextureRegion heattex;
    public void draw(){


        if(heattex == null){
            heatmap = new Pixmap(heat.w, heat.h);
            heattex = new TextureRegion(new Texture(heatmap));
            heattex.texture.draw(heatmap);
        }

        Tmp.v1.set(Vars.world.width() * Vars.tilesize * 0.5f, Vars.world.height() * Vars.tilesize * 0.5f);

        for (int y = 0; y < heat.h; y++) {
            for (int x = 0; x < heat.w; x++) {
                //heatmap.set(x, y, Tmp.c1.set(Mathf.clamp(heat.kelvins(x + y * heat.w)/6375, 0, 1), 0, 0, 1).rgba());
                heatmap.set(x, y, Tmp.c1.set(1, 1, 1, 1).rgba8888());
            }
        }

        Tmp.tr1.set(heattex);
        //heatShader.center = Tmp.v1;
        //Draw.shader(heatShader);
        //It's kinda funny that this doesn't work. It's also more efficient than sending 1 draw call per tile by a long shot. (Although maybe not as good as batched rendering) :)
        Draw.rect(Tmp.tr1, Tmp.v1.x, Tmp.v1.y);
        Draw.reset();
        //Draw.shader();
    }
}
