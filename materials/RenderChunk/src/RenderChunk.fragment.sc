$input v_color0, v_fog, v_texcoord0, v_lightmapUV, v_cpos, v_wpos, v_wDisplace

#include <bgfx_shader.sh>

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform float RenderDistance;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_SeasonsTexture, 1);
SAMPLER2D(s_LightMapTexture, 2);
#include <azify/utils/functions.glsl>

void main() {
    lowp vec4 diffuse;

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
    diffuse.rgb *= mix(vec3(1.0, 1.0, 1.0),texture2D(s_SeasonsTexture, v_color0.xy).rgb * 2.0, v_color0.b);
    diffuse.rgb *= v_color0.aaa;
#else
  // REMOVED AMBIENT OCCLUSION
    mediump vec3 ncol_0 = normalize(v_color0.rgb);
        if(abs(ncol_0.r - ncol_0.g) > 0.001 || abs(ncol_0.g - ncol_0.b) > 0.001) {
        diffuse = vec4(diffuse.rgb * mix(ncol_0.rgb, v_color0.rgb, 0.35), v_color0.a);
    }
#endif
#endif

#ifndef TRANSPARENT
    diffuse.a = 1.0;
#endif

  // DETERMINE WATER TEXTURE
  lowp float waterFlag = 0.0;
  #ifdef TRANSPARENT
      if (v_color0.r != v_color0.g || v_color0.g != v_color0.b || v_color0.r != v_color0.b) {
          waterFlag = 1.0;
      }
  #endif

  // CALCULATE POSITIONS & FUNCTIONS
  vec3 normal = normalize(cross(dFdx(v_cpos), dFdy(v_cpos)));
  mediump vec3 skyPos = (v_wpos.xyz + vec3(0.0, 0.128, 0.0));
  mediump vec3 nskyposP = normalize(skyPos);
  mediump vec3 skyMIEP = dynamicSky(diffuse.xyz, nskyposP);
  mediump vec3 nskyposN = normalize(-skyPos);
  mediump vec3 skyMIEX = dynamicSky(diffuse.xyz, nskyposN);
  mediump float isCaveX = smoothstep(1.0, 0.35, v_lightmapUV.y);


  // DIRECT LIGHT REPLICA
  #ifdef DIRLIGHT_BOTTOM
    float raterDirY = max(0.0,-normal.y);
    diffuse.rgb *= mix(vec3(1.0), mix(vec3(1.0), vec3(0.7, 0.75, 0.8), raterDirY), v_lightmapUV.y);
  #endif


  // SMOOTH AMBIENT OCCLUSION
  #ifndef ALPHA_TEST
   mediump float vanillaAO = (1.0 - (v_color0.y * 2.0 - (v_color0.x < v_color0.z ? v_color0.x : v_color0.z)) * 1.4);
   mediump float fakeAO = clamp(1.0 - (vanillaAO * 0.5), 0.0, 1.0);
   diffuse.rgb *= mix(vec3(1.0), vec3(0.1, 0.11, 0.15), (1.0-fakeAO));
  #endif
 
 
  // WORLD COLORS
  #ifdef ENABLE_LIGHTS
    if (dev_UnWater) {
      diffuse.rgb *= mix(vec3(0.6,0.7,0.85), vec3(0.2, 0.3, 0.6), smoothstep(0.95, 0.8, v_lightmapUV.y));
    } else if (dev_Nether) {
      mediump vec3 netherColor = mix(vec3(1.0), vec3(0.55), smoothstep(0.5, 0.0, v_lightmapUV.y));
      netherColor = mix(netherColor, vec3(1.0), pow(v_lightmapUV.x, 3.0));
      diffuse.rgb *= netherColor;
    } else if (dev_End) {
      mediump vec3 endColor = vec3(0.5);
      endColor = mix(endColor, vec3(1.0), pow(v_lightmapUV.x, 3.0));
      diffuse.rgb *= endColor;
    } else {
    mediump vec3 worldColor = timecycle3(vec3(0.9, 0.94, 1.0), vec3(0.34,0.26,0.22), vec3(0.43,0.43,0.67));
    worldColor = mix(worldColor, vec3(0.14,0.14,0.14), isCaveX);
    worldColor = mix(worldColor, timecycle3(vec3(0.45, 0.5, 0.6), vec3(0.2),vec3(0.2, 0.2, 0.3)), smoothstep(0.91, 0.77, v_lightmapUV.y)*(1.0-isCaveX));
    worldColor = mix(worldColor, vec3(1.0), pow(v_lightmapUV.x, 3.0));
    diffuse.rgb *= worldColor;
    }
  #endif


  // WATER WAVES
  #if defined(TRANSPARENT)
    if (waterFlag > 0.5) {
      float waterDisp = clamp(sin(noise(vec2(v_cpos.p * 1.0 + ViewPositionAndTime.w * 0.03, v_cpos.s * 6.0 + ViewPositionAndTime.w * 1.45 + v_cpos.p * 1.0 + v_cpos.t * 0.5))), 0.23, 1.0);
      diffuse.a = 0.3;
      diffuse *= vec4(skyMIEX, 1.0);
      
      vec2 noisePos = vec2(atan(v_wpos.x, v_wpos.z) * 24.0) - waterDisp * 6.0;
      float noiseVal = noise(noisePos);
      float blendFactor = clamp(length(vec2(v_wpos.xz * 0.3 / v_wpos.y * 0.6)), 0.0, 1.0) * 0.8;
      #ifdef SIMULATED_WATER // WATER SIMULATION ENABLE
      diffuse = mix(diffuse, vec4(skyMIEX * mix(1.0, 2.5, AFrain * AFnight), 1.0), noiseVal * blendFactor);
      #endif
      
      float sunRayFactor = clamp(1.0 - length(v_wpos.zy / v_wpos.x * 12.0 * vec2(0.4, 0.1)) - waterDisp * 1.5, 0.0, 1.0);
      float rayBlendFactor = clamp(1.0 - length(v_wpos.zy / v_wpos.x * 7.8 * vec2(0.2, 0.1)) - waterDisp * 0.3, 0.0, 1.0);
      
      #ifdef WATER_SUNRAY // WATER SUN RAYS BLOOM
      diffuse = mix(diffuse, vec4(skyMIEP, 1.0), smoothstep(0.0, 1.0, rayBlendFactor) * v_lightmapUV.y* (1.0- AFnight)* (1.0-AFrain)* (AFdusk));
      diffuse = mix(diffuse, vec4(1.0, 0.7, 0.15, 1.0) * 1.5, smoothstep(0.0, 0.75, sunRayFactor) * v_lightmapUV.y* (1.0- AFnight)* (1.0-AFrain)* (AFdusk));
      #endif
    }
  #endif


  // GROUND BLOOM WHEN DUSK
  #ifdef GROUND_BLOOM
    float rayGrPos = max(0.0,normal.y);
    float sunRayBloom = clamp(1.0 - length(v_wpos.zy / v_wpos.x * 5.0 * vec2(0.2, 0.1)), 0.0, 1.0);
    diffuse = mix(diffuse, vec4(skyMIEP, 1.0), smoothstep(0.0, 1.0, sunRayBloom)* GROUND_BLOOM_STRENGTH* rayGrPos* v_lightmapUV.y* (1.0- AFnight)* (1.0-AFrain)* (AFdusk));
  #endif


  // SUN BLOOM WHEN DUSK
  #ifdef SUN_BLOOM
    float sunBLdist = clamp(length((v_wpos))/FogAndDistanceControl.w,.0,1.);
    float sunBloom = clamp(1.0 - length(v_wpos.zy / v_wpos.x * 45.0 * vec2(0.1, 0.1)), 0.0, 1.0);
    diffuse = mix(diffuse, vec4(1.0, 0.7, 0.1, 1.0), smoothstep(0.0, 1.0, sunBloom)* SUN_BLOOM_STRENGTH* v_fog.a* v_lightmapUV.y* (1.0- AFnight)* (1.0-AFrain)* (AFdusk));
  #endif


  // TORCH LIGHTS
  #if TORCHLIGHT_MODES == 0
      diffuse +=  diffuse* vec4(0.9)* v_lightmapUV.x;
  #elif TORCHLIGHT_MODES == 1
      mediump float torchPower = pow(v_lightmapUV.x * 1.06, 4.0);
      diffuse += diffuse * (vec4(torchColor, 1.0) * torchPower);
  #elif TORCHLIGHT_MODES == 2
      mediump float smotherLight = smoothstep(0.7, 1.1, v_lightmapUV.x);
      diffuse += diffuse * (vec4(torchColor, 1.0) * smotherLight);
  #endif


  // THICK RAIN FOG
  #ifdef RAIN_THICK_FOG
   if (AFrain > 0.5) {
     lowp float fogDist = clamp(dot(v_wpos, v_wpos) * 0.0008, 0.0, 1.0);
     diffuse.rgb = mix(diffuse.rgb, skyMIEP, fogDist);
  }
  #endif


  // SKY BASED FOG
  if (dev_UnWater) {
    diffuse.rgb = mix(diffuse.rgb, UNDERWATER_COLOR, v_fog.a);
  } else if (dev_Nether) {
    diffuse.rgb = mix(diffuse.rgb, FogColor.rgb, v_fog.a);
  } else if (dev_End) {
    diffuse.rgb = mix(diffuse.rgb, FogColor.rgb, v_fog.a);
  } else {
    diffuse.rgb = mix(diffuse.rgb, skyMIEP, v_fog.a);
  }
    gl_FragColor = diffuse;
}