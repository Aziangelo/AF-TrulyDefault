$input v_color0, v_color1, v_color2, v_color3, v_color4, v_color5, v_color6, v_color7, v_color8, v_color9, v_color10, v_color11, v_color12, v_fog, v_texcoord0, v_lightmapUV, v_cpos, v_wpos, v_wDisp, v_waterFlag

#include <bgfx_shader.sh>

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_SeasonsTexture, 1);
SAMPLER2D(s_LightMapTexture, 2);
#include <azify/utils/functions.glsl>

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
    float diff_rg = abs(ncol_0.r - ncol_0.g);
    float diff_gb = abs(ncol_0.g - ncol_0.b);
    if (diff_rg > 0.001 || diff_gb > 0.001) {
    vec3 mixedColor = mix(ncol_0.rgb, v_color0.rgb, 0.35);
    diffuse = vec4(diffuse.rgb * mixedColor.rgb, v_color0.a);
    }
#endif
#endif /*End of DEPTH_ONLY_OPAQUE */

#ifndef TRANSPARENT
    diffuse.a = 1.0;
#endif
#include <azify/utils/components.glsl> // Components Files
 
  // CALCULATE POSITIONS & FUNCTIONS
  vec3 dx = dFdx(v_cpos);
  vec3 dy = dFdy(v_cpos);
  vec3 dXY = cross(dx,dy);
  vec3 normal = normalize(dXY);
  float isCaveX = smoothstep(1.0, 0.35, v_lightmapUV.y);
  
  // DIRECT LIGHT REPLICA
  #ifdef DIRLIGHT_BOTTOM
    float raterDirY = max(0.0, -normal.y);
    diffuse.rgb *= mix(vec3(1.0), v_color1.rgb, raterDirY);
  #endif

  // SMOOTH AMBIENT OCCLUSION
  #ifndef ALPHA_TEST
   diffuse.rgb *= v_color2.rgb;
  #endif

  // WORLD COLORS
  #ifdef ENABLE_LIGHTS
    diffuse.rgb *= v_color3.rgb;
  #endif

  // WATER WAVES
  #if defined(TRANSPARENT)
    if (v_waterFlag > 0.5) {
    float cmin = 0.23;    
    float cmax = 1.0;
    float noiseScale = 24.0;
    float dispScale = 6.0;

    float wdisp = clamp(sin(noise(v_wDisp)), cmin, cmax);
    float noiseVal = noise(vec2(atan2(v_wpos.x, v_wpos.z) * noiseScale) - wdisp * dispScale);
    #ifdef SIMULATED_WATER
      diffuse.rgb = mix(v_color6.rgb, v_color7.rgb, noiseVal * v_color7.a);
    #endif
    #ifdef WATER_SUNRAY // WATER SUN RAYS BLOOM
    float slent = length(v_wpos.zy / v_wpos.x * 12.0 * vec2(0.4, 0.1));
    float sunRayFactor = clamp(1.0 - slent - wdisp * 1.5, 0.0, 1.0);
    float rlent = length(v_wpos.zy / v_wpos.x * 7.8 * vec2(0.2, 0.1));
    float rayBlendFactor = clamp(1.0 - rlent - wdisp * 0.3, 0.0, 1.0);
    
    float rbf = smoothstep(0.0, 1.0, rayBlendFactor);
    float srf = smoothstep(0.0, 0.75, sunRayFactor);
    diffuse = mix(diffuse, vec4(v_color8.rgb,0.9), rbf * v_color8.a);
    diffuse = mix(diffuse, vec4(v_color9.rgb,1.0), srf * v_color9.a);
    #endif
    }
  #endif

  // GROUND BLOOM WHEN DUSK
  #ifdef GROUND_BLOOM
  if (normal.y > 0.5) {
    diffuse.rgb = mix(diffuse.rgb, v_color5.rgb, v_color5.a);
  }
  #endif

  // SUN BLOOM WHEN DUSK
  #ifdef SUN_BLOOM
    diffuse.rgb = mix(diffuse.rgb, v_color4.rgb, v_color4.a);
  #endif
/*
  // WET EFFECT POS CALCULATION
  #ifndef TRANSPARENT
   float sunShadow = sunDirShadow(v_color0, v_lightmapUV);
   float dotn = dot(normal, normalize (-v_wpos));
   float nDot = clamp (smoothstep (0.8, 0.0, dotn), 0.0, 1.0);
   float glCv = smoothstep (0.89, 0.75, v_lightmapUV.y);
   float roughness = 0.85; 
   float noiseValue = noise(v_cpos.xz * 1.0);
   float roughnessFactor = (noiseValue * roughness);
  #endif

  // GLOSSY WET EFFECT
  #ifdef GLOSSY_WET_EFFECT
  #ifndef TRANSPARENT
    vec3 glosCol1 = timecycle3(vec3(0.7), vec3(0.65), vec3(0.45));
    diffuse = mix(diffuse, vec4(glosCol1, 1.0), nDot * mix(0.6, 0.35, AFnight) * clamp(max(0.0, normal.y), 0.0, 1.0) * roughnessFactor * (1.0- max(sunShadow, glCv)) * AFrain);
  #endif
  #endif

  // TERRAIN REFLECTION REPLICA
  #ifdef RAIN_TERRAIN_REFLECTION
  #ifndef TRANSPARENT
    float wetDisp = clamp(voronei((v_cpos.xz + v_cpos.y) * 0.8), 0.0, 1.0);
    vec2 wetNoisePos = vec2(atan2(v_wpos.x, v_wpos.z) * 13.0)- wetDisp * 2.5;
    float wetVal = noise(wetNoisePos);
    float wetfadeFact = clamp(length(vec2(v_wpos.xz * 0.3 / v_wpos.y * 0.5)), 0.0, 1.0);
    diffuse.xyz = mix (diffuse.xyz, vec3(0.1), wetVal * clamp(max(0.0, normal.y), 0.0, 1.0) * wetfadeFact * roughnessFactor * AFrain * (1.0- max(sunShadow, glCv)));
  #endif
  #endif
*/
  // THICK RAIN FOG
  #ifdef RAIN_THICK_FOG
    diffuse.rgb = mix(diffuse.rgb, v_color10.rgb, v_color10.a);
  #endif

    diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);
    gl_FragColor = diffuse;
}