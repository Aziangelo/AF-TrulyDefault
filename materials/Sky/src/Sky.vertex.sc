$input a_color0, a_position
#ifdef GEOMETRY_PREPASS
    $input a_texcoord0
    #ifdef INSTANCING
        $input i_data0, i_data1, i_data2
    #endif
#endif

$output v_color0, v_skypos, v_cpos
#ifdef GEOMETRY_PREPASS
    $output v_texcoord0, v_normal, v_worldPos, v_prevWorldPos
#endif

#include <bgfx_shader.sh>

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
#include <azify/utils/functions.glsl>

void main() {
#if defined(OPAQUE)
#include <azify/utils/components.glsl> // Components Files
    //Opaque
    vec4 tmpvar = vec4(a_position, 1.0);
    /*
    tmpvar.y -= sqrt(dot(a_position.xz, a_position.xz) * 17.5);*/
    v_skypos = tmpvar.xyz + vec3(0.0, 0.128, 0.0);
    v_cpos = a_position;
    v_color0 = mix(SkyColor, FogColor, a_color0.x);
    gl_Position = mul(u_modelViewProj, tmpvar);
#else
    //Fallback
    v_color0 = vec4(0.0, 0.0, 0.0, 0.0);
    gl_Position = vec4(0.0, 0.0, 0.0, 0.0);

#endif
}