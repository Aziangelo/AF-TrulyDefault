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
    diffuse.rgb *= v_color2.rgb;
  #endif
  
  // GROUND BLOOM WHEN DUSK
  #ifdef GROUND_BLOOM
    diffuse.rgb = mix(diffuse.rgb, v_color4.rgb, v_color4.a * max(0.0, normal.y));
  #endif

  // SUN BLOOM WHEN DUSK
  #ifdef SUN_BLOOM
    diffuse.rgb = mix(diffuse.rgb, v_color3.rgb, v_color3.a);
  #endif


  // WATER WAVES
  #if !defined(DEPTH_ONLY_OPAQUE) || defined(DEPTH_ONLY)
  #ifdef TRANSPARENT
  if (waterFlag) {
    float noiseScale = 24.0;
    float dispScale = 6.0;

    float wpx = v_cpos.x * 6.0;
    float wpy = v_cpos.y * 0.5;
    float wpz = v_cpos.z * 1.0;
    vec2 waterDisp = vec2(wpz + ViewPositionAndTime.w* 0.03, wpx + ViewPositionAndTime.w* 1.45 + wpz + wpy);
    float wdisp = clamp(sin(noise(waterDisp)), 0.23, 1.0);
    float noiseVal = noise(vec2(atan2(v_wpos.x, v_wpos.z) * noiseScale, atan2(v_wpos.x, v_wpos.z) * noiseScale) - wdisp * dispScale);
    #ifdef SIMULATED_WATER
      diffuse.rgb = mix(v_color5.rgb, v_color6.rgb, noiseVal * v_color6.a);
    #endif
    #ifdef WATER_SUNRAY // WATER SUN RAYS BLOOM
    float slent = length(v_wpos.zy / v_wpos.x * 12.0 * vec2(0.4, 0.1));
    float sunRayFactor = clamp(1.0 - slent - wdisp * 1.5, 0.0, 1.0);
    float rlent = length(v_wpos.zy / v_wpos.x * 7.8 * vec2(0.2, 0.1));
    float rayBlendFactor = clamp(1.0 - rlent - wdisp * 0.3, 0.0, 1.0);
    
    float rbf = smoothstep(0.0, 1.0, rayBlendFactor);
    float srf = smoothstep(0.0, 0.75, sunRayFactor);
    diffuse = mix(diffuse, vec4(v_color7.rgb,0.9), rbf * v_color7.a);
    diffuse = mix(diffuse, vec4(v_color8.rgb,1.0), srf * v_color8.a);
    #endif
  }
  #endif
  #endif


  // WET EFFECT POS CALCULATION
  #if defined(OPAQUE)
   float sunShadow = sunDirShadow(v_color0, v_lightmapUV);
   float dotn = dot(normal, normalize (-v_wpos));
   float nDot = clamp (smoothstep (0.8, 0.0, dotn), 0.0, 1.0);
   float glCv = smoothstep (0.89, 0.75, v_lightmapUV.y);
   float roughness = 0.85; 
   float noiseValue = noise(v_cpos.xz * 1.0);
   float roughnessFactor = (noiseValue * roughness);

  // GLOSSY WET EFFECT
  #ifdef GLOSSY_WET_EFFECT
  if (dev_UnWater) {
  } else {
    vec3 glosCol1 = mix(mix(vec3(0.7,0.7,0.7), vec3(0.65,0.65,0.65), AFdusk), vec3(0.45,0.45,0.45), AFnight);
    diffuse = mix(diffuse, vec4(glosCol1, 1.0), nDot * mix(0.6, 0.35, AFnight) * clamp(max(0.0, normal.y), 0.0, 1.0) * roughnessFactor * (1.0- max(sunShadow, glCv)) * AFrain);
  }
  #endif


  // TERRAIN REFLECTION REPLICA
  #ifdef RAIN_TERRAIN_REFLECTION
    float wetDisp = clamp(voronoi((v_cpos.xz + v_cpos.y) * 0.8), 0.0, 1.0);
    vec2 wetNoisePos = vec2(atan2(v_wpos.x, v_wpos.z) * 13.0, atan2(v_wpos.x, v_wpos.z) * 13.0)- wetDisp * 2.5;
    float wetVal = noise(wetNoisePos);
    float wetfadeFact = clamp(length(vec2(v_wpos.xz * 0.3 / v_wpos.y * 0.5)), 0.0, 1.0);
    diffuse.xyz = mix (diffuse.xyz, vec3(0.1,0.1,0.1), wetVal * clamp(max(0.0, normal.y), 0.0, 1.0) * wetfadeFact * roughnessFactor * AFrain * (1.0- max(sunShadow, glCv)));
  #endif
  #endif

  // THICK RAIN FOG
  #ifdef RAIN_THICK_FOG
    diffuse.rgb = mix(diffuse.rgb, v_color9.rgb, v_color9.a);
  #endif

   // diffuse.rgb = mix(diffuse.rgb, v_fog.rgb, v_fog.a);
    gl_FragColor = diffuse;
}