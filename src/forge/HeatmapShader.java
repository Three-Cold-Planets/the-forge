package forge;

import arc.Core;
import arc.graphics.Texture;
import arc.graphics.gl.Shader;
import arc.math.geom.Vec2;
import mindustry.Vars;

public class HeatmapShader extends Shader {
    public Texture heatmap;
    public Vec2 center;

    public HeatmapShader(String name){
        super(Core.files.internal("shaders/screenspace.vert"),
                Vars.tree.get("shaders/" + name + ".frag"));
    }

    @Override
    public void apply() {
        super.apply();

        setUniformf("u_center", center);

        setUniformf("u_resolution", Core.graphics.getWidth(),
                Core.graphics.getHeight());

        setUniformf("u_worldBounds", Vars.world.width(), Vars.world.height());
    }
}
