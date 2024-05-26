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

  // CALCULATE POSITIONS
  vec3 normal = normalize(cross(dFdx(v_cpos), dFdy(v_cpos)));

  // DIRECT LIGHT REPLICA
  float raterDirY = max(0.0,-normal.y);
  diffuse.rgb *= mix(vec3(1.0), mix(vec3(1.0), vec3(0.7, 0.75, 0.8), raterDirY), v_lightmapUV.y);

  // SMOOTH AMBIENT OCCLUSION
  #ifndef ALPHA_TEST
   mediump float vanillaAO = (1.0 - (v_color0.y * 2.0 - (v_color0.x < v_color0.z ? v_color0.x : v_color0.z)) * 1.4);
   mediump float fakeAO = clamp(1.0 - (vanillaAO * 0.5), 0.0, 1.0);
   diffuse.rgb *= mix(vec3(1.0), vec3(0.1, 0.11, 0.15), (1.0-fakeAO));
  #endif

  // WORLD COLORS
  mediump float isCaveX = smoothstep(1.0, 0.35, v_lightmapUV.y);
  mediump vec3 worldColor = timecycle3(vec3(0.9, 0.94, 1.0), vec3(0.34,0.26,0.22), vec3(0.43,0.43,0.67));
  worldColor = mix(worldColor, vec3(0.14,0.14,0.14), isCaveX);
  worldColor = mix(worldColor, mix(vec3(0.45, 0.5, 0.6), vec3(0.2, 0.2, 0.3), AFnight), smoothstep(0.95, 0.8, v_lightmapUV.y)*(1.0-isCaveX));
  worldColor = mix(worldColor, vec3(1.0), pow(v_lightmapUV.x, 3.0));
  diffuse.rgb *= worldColor;

  // TORCH LIGHTS
  lowp vec3 torchColor = vec3(1.0, 0.74, 0.33);
  //float smotherLight = smoothstep(0.5, 1.1, v_lightmapUV.x);
  mediump float torchPower = pow(v_lightmapUV.x * 1.06, 4.0);
  diffuse += diffuse * (vec4(torchColor, 1.0) * torchPower);

  // THICK RAIN FOG
  mediump vec3 v_skypos = (v_wpos.xyz + vec3(0.0, 0.128, 0.0));
  mediump vec3 v_viewpos = normalize(v_skypos);
  mediump vec3 skyMie = dynamicSky(diffuse.xyz, v_viewpos);
  #ifdef RAIN_THICK_FOG
   if (AFrain > 0.5) {
     lowp float fogDist = clamp(dot(v_wpos, v_wpos) * 0.0008, 0.0, 1.0);
     diffuse.rgb = mix(diffuse.rgb, skyMie, fogDist);
  }
  #endif

  // SKY BASED FOG
  diffuse.rgb = mix(diffuse.rgb, skyMie, v_fog.a);
    gl_FragColor = diffuse;
}