$input v_color0, v_skypos, v_cpos
#if defined(GEOMETRY_PREPASS)
    $input v_texcoord0, v_normal, v_worldPos, v_prevWorldPos
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
vec4 albedo;

   vec3 basepos_1 = normalize(v_skypos);
   
   if (dev_UnWater) {
      albedo.rgb = vec3(UNDERWATER_COLOR);
   } else {
      albedo.rgb = dynamicSky(albedo.rgb, nskyposP, AFnight, AFdusk, AFrain, SkyColor.rgb, FogColor.rgb);
   }
    gl_FragColor = albedo;
#else
    //Fallback
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);

#endif
}