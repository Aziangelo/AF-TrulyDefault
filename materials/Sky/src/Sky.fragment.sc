$input v_color0, v_skypos, v_cpos
#if defined(GEOMETRY_PREPASS)
    $input v_texcoord0, v_normal, v_worldPos, v_prevWorldPos
#endif

#include <bgfx_shader.sh>

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
#include <azify/utils/functions.glsl>

void main() {
#if defined(OPAQUE)
    //Opaque
   mediump vec3 basepos_1 = normalize(v_skypos);
   lowp vec3 tmpvar_0;
  tmpvar_0 += dynamicSky(tmpvar_0.xyz, basepos_1);
  if(FogAndDistanceControl.x <= 0.0 && FogAndDistanceControl.b > FogAndDistanceControl.r) {
    tmpvar_0 = FogColor.rgb;
  }
    gl_FragColor = vec4(tmpvar_0,1.0);
#else
    //Fallback
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);

#endif
}