uniform sampler2D u_texture;

uniform vec2 u_resolution;
uniform vec2 u_center;
uniform vec2 u_worldBounds;

varying vec2 v_texCoords;

//All the colours for the heatmap to interp between, how lovely!
const vec4[] c_colors = vec4[8](
    vec4(83, 212, 200, 1),
    vec4(68, 150, 199, 1),
    vec4(46, 153, 194, 1),
    vec4(64, 194, 46, 1),
    vec4(186, 203, 67, 1),
    vec4(220, 172, 110, 1),
    vec4(207, 102, 91, 1),
    vec4(194, 12, 12, 1)
);

mat2 worldScale(vec2 worldBounds){
    return mat2(worldBounds.x, 0,
                0, worldBounds.y);
}

void main(){

    vec4 color = texture(u_texture, v_texCoords.xy);

    vec4 returnCol = vec4(0);
    returnCol.a = 0.5;
    returnCol.b = clamp(1.0 - color.r * 6375/273.15, 0.0, 1.0);

    returnCol = normalize(returnCol);

    gl_FragColor = returnCol;
}