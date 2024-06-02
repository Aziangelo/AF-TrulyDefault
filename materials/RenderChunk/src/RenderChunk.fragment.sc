$input v_color0, v_fog, v_texcoord0, v_lightmapUV, v_cpos, v_wpos, v_color1, v_color2, v_color3, v_color4, v_color5, v_color6, v_color7, v_color8, v_color9

#include <bgfx_shader.sh>
#include <azify/utils/functions.glsl>

uniform vec4 FogColor;
uniform vec4 SkyColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

SAMPLER2D_AUTOREG(s_MatTexture);
SAMPLER2D_AUTOREG(s_SeasonsTexture);
SAMPLER2D_AUTOREG(s_LightMapTexture);

void main() {
    vec4 diffuse;

#if defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY)
    diffuse.rgb = vec3(1.0, 1.0, 1.0);
#else
    diffuse = texture2D(s_MatTexture, v_texcoord0);

#if defined(ALPHA_TEST)
    if (diffuse.a < 0.5) {
        discard;
    }
#endif

#if defined(SEASONS) && (defined(OPAQUE) || defined(ALPHA_TEST))
    diffuse.rgb *=
        mix(vec3(1.0, 1.0, 1.0),
            texture2D(s_SeasonsTexture, v_color0.xy).rgb * 2.0, v_color0.b);
    diffuse.rgb *= v_color0.aaa;
#else
    // REMOVED AMBIENT OCCLUSION
   vec3 ncol_0 = normalize(v_color0.rgb);
        if(abs(ncol_0.r - ncol_0.g) > 0.001 || abs(ncol_0.g - ncol_0.b) > 0.001) {
        diffuse = vec4(diffuse.rgb * mix(ncol_0.rgb, v_color0.rgb, 0.45), v_color0.a);
    }
    //diffuse *= v_color0;
#endif
#endif

#ifndef TRANSPARENT
    diffuse.a = 1.0;
#endif
#include <azify/utils/components.glsl> // Components Files
  vec3 dx = dFdx(v_cpos);
  vec3 dy = dFdy(v_cpos);
  vec3 dXY = cross(dx,dy);
  vec3 normal = normalize(dXY);

  // DIRECT LIGHT REPLICA
  #ifdef DIRLIGHT_BOTTOM
    float raterDirY = max(0.0, -normal.y);
    diffuse.rgb *= mix(vec3(1.0,1.0,1.0), v_color1.rgb, raterDirY);
  #endif

  // SMOOTH AMBIENT OCCLUSION
  #ifndef ALPHA_TEST
   //diffuse.rgb *= v_color2.rgb;
  #endif

  // WORLD COLORS
  #ifdef ENABLE_LIGHTS
    diffuse.rgb *= v_color3.rgb;
  #endif



    diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);
    gl_FragColor = diffuse;
}