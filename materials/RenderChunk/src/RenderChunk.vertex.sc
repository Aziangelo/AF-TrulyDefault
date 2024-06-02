$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2
#endif
$output v_color0, v_fog, v_texcoord0, v_lightmapUV, v_cpos, v_wpos, v_color1, v_color2, v_color3, v_color4, v_color5, v_color6, v_color7, v_color8, v_color9

#include <bgfx_shader.sh>
#include <azify/utils/functions.glsl>

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;
uniform vec4 SkyColor;

void main() {
    mat4 model;
#ifdef INSTANCING
    model = mtxFromCols(i_data0, i_data1, i_data2, vec4(0, 0, 0, 1));
#else
    model = u_model[0];
#endif
#include <azify/utils/components.glsl> // Components Files

    vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;
    vec4 color;
    color = a_color0;

    vec3 modelCamPos = (ViewPositionAndTime.xyz - worldPos);
    float camDis = length(modelCamPos);
    float relativeDist = camDis / FogAndDistanceControl.z;
    relativeDist += RenderChunkFogAlpha.x;
    vec4 fogColor;
    fogColor.rgb = FogColor.rgb;
    float density = mix(mix(0.15, 5.5, AFdusk), 0.25, AFnight);
    float rrt = exp(-relativeDist*relativeDist*density);
    float fog;
    fog = smoothstep(FogAndDistanceControl.x, FogAndDistanceControl.y, relativeDist);
    fog += (1.0-fog)*(0.5-0.5*rrt);

    // SKY BASED FOG
    vec3 skyPos = (worldPos.xyz + vec3(0.0, 0.128, 0.0));
    vec3 nskyposP = normalize(skyPos);
    vec3 fogMie;
    vec3 skyMIEP = dynamicSky(FogColor.rgb, nskyposP, AFnight, AFdusk, AFrain, SkyColor.rgb, FogColor.rgb);
    if (dev_UnWater) {
      fogMie = UNDERWATER_COLOR;
    } else if (dev_Nether || dev_End) {
      fogMie = FogColor.rgb;
    } else {
      fogMie = skyMIEP;
    }
    fogColor.rgb = fogMie;
    fogColor.a = fog;
#ifdef TRANSPARENT
    if(a_color0.a < 0.95) {
        color.a = mix(a_color0.a, 1.0, clamp((camDis / FogAndDistanceControl.w), 0.0, 1.0));
    };
#endif

  // MANIFIGULATOR (-_-?)
  float isCaveX = smoothstep(1.0, 0.35, a_texcoord1.y);
  float isCave = smoothstep(0.91, 0.77, a_texcoord1.y);
  float isLight = pow(a_texcoord1.x, 3.0);

  // DIRECT LIGHT REPLICA
  vec4 DirectLightColor;
  #ifdef DIRLIGHT_BOTTOM
    DirectLightColor.rgb = mix(vec3(1.0,1.0,1.0), vec3(0.7, 0.75, 0.8), a_texcoord1.y);
  #endif
  
  // SMOOTH AMBIENT OCCLUSION
  vec4 AOColor;
   float minColor = min(a_color0.r, a_color0.b);
   float aoFactor = a_color0.g * 2.0 - minColor;
   float vanillaAO = 1.0 - aoFactor * 1.4;
   float fakeAO = clamp(1.0- vanillaAO * 0.5, 0.0, 1.0);
   AOColor.rgb = mix(vec3(1.0,1.0,1.0), vec3(0.1, 0.11, 0.15), 1.0-fakeAO);
  
  
  // WORLD COLORS
  vec3 WorldColor;
  #ifdef ENABLE_LIGHTS
    WorldColor = mix(mix(vec3(0.9, 0.94, 1.0), vec3(0.54, 0.46, 0.42), AFdusk), vec3(0.43, 0.43, 0.67), AFnight);
    WorldColor = mix(WorldColor, vec3(0.14,0.14,0.14), isCaveX);
    WorldColor = mix(WorldColor, mix(mix(vec3(0.45, 0.5, 0.6), vec3(0.2,0.2,0.2), AFdusk), vec3(0.2, 0.2, 0.3), AFnight), isCave * (1.0 - isCaveX));
    WorldColor = mix(WorldColor, vec3(1.0,1.0,1.0), isLight);
  #endif

  // TORCH LIGHTS
  vec3 TorchColor;
  #if TORCHLIGHT_MODES == 0
    TorchColor = vec3(0.9,0.9,0.9) * a_texcoord1.x;
  #elif TORCHLIGHT_MODES == 1
    float torchPower = pow(a_texcoord1.x * 1.09, 4.0);
    TorchColor = vec3(torchColor) * torchPower;
  #elif TORCHLIGHT_MODES == 2
    float smotherLight = smoothstep(0.7, 1.1, a_texcoord1.x);
    TorchColor = vec3(torchColor) * smotherLight;
  #endif
  vec4 finalWColor;
  finalWColor = vec4(AOColor.rgb * WorldColor + TorchColor,1.0);


  // SUN BLOOM WHEN DUSK
  vec4 sunblPos;
  #ifdef SUN_BLOOM
  float sunlength = length(worldPos.zy / worldPos.x * 4.5);
  float sunBloom = clamp(1.0 - sunlength, 0.0, 1.0);
  float bloomFactor = smoothstep(0.0, 1.0, sunBloom) * SUN_BLOOM_STRENGTH * (1.0-AFnight) * (1.0-AFrain) * (AFdusk * a_texcoord1.y)  * fogColor.a;
  vec4 bloomColor = vec4(1.0, 0.7, 0.1, 1.0);
  sunblPos.rgb = bloomColor.rgb;
  sunblPos.a = bloomFactor;
  #endif


  // GROUND BLOOM WHEN DUSK
  vec4 grblColor;
  #ifdef GROUND_BLOOM
  vec2 posRatio = worldPos.zy / worldPos.x;
  vec2 scaleF = vec2(1.0, 0.5); 
  float grblLen = length(posRatio * scaleF);
  float sunRayBloom = clamp(1.0 - grblLen, 0.0, 1.0);
  float mixFactor = smoothstep(0.0, 1.0, sunRayBloom) * GROUND_BLOOM_STRENGTH /* rayGrPos */* (1.0-isCave) * (1.0 - AFnight) * (1.0 - AFrain) * AFdusk;
  grblColor.rgb = skyMIEP;
  grblColor.a = mixFactor;
  #endif


  // CALCULATE NOISE  =============>>>>>>>>
  float wpx = a_position.x * 6.0;
  float wpy = a_position.y * 0.5;
  float wpz = a_position.z * 1.0;
  vec2 waterDisp = vec2(wpz + ViewPositionAndTime.w* 0.03, wpx + ViewPositionAndTime.w* 1.45 + wpz + wpy);


  // WATER WAVES  =============>>>>>>>>
  vec3 nskyposN = normalize(-skyPos);
  vec3 skyMIEX = dynamicSky(FogColor.rgb, nskyposN, AFnight, AFdusk, AFrain, SkyColor.rgb, FogColor.rgb);
  vec4 waterOpa;
  vec4 waterSim;
  vec4 waterRy1;
  vec4 waterRy2;
    float wdisp = clamp(sin(noise(waterDisp)), 0.23, 1.0);
    waterOpa = vec4(skyMIEX, 1.0) * 0.3;
    
    //float at2 = vec2(atan2(worldPos.x, worldPos.z) * 24.0);
    vec2 noisePos = vec2(vec2(atan2(worldPos.x, worldPos.z) * 24.0) - wdisp * 6.0);
    //float noiseVal = noise(noisePos);
    //float fadeFact = clamp(length(vec2(worldPos.xz * 0.3 / worldPos.y * 0.6)), 0.0, 1.0) * 0.8;
    
    //#ifdef SIMULATED_WATER // WATER SIMULATION ENABLE
    //vec3 simCol = vec3(skyMIEX * mix(1.0, 2.5, AFrain * AFnight));
    //waterSim.rgb = simCol;
    //waterSim.a = fadeFact;
    //#endif
    /*
    //#ifdef WATER_SUNRAY // WATER SUN RAYS BLOOM
    waterRy1.rgb = skyMIEP;
    waterRy1.a = a_texcoord1.y * (1.0 - AFnight) * (1.0 - AFrain) * (AFdusk);
    waterRy2.rgb = vec3(1.0, 0.7, 0.15) * 1.8;
    waterRy2.a = a_texcoord1.y * (1.0 - AFnight) * (1.0 - AFrain) * (AFdusk);
    //#endif

  // THICK RAIN FOG
  vec4 rainTfog;
  #ifdef RAIN_THICK_FOG
    float fogDist = clamp(dot(worldPos, worldPos) * 0.0008, 0.0, 1.0);
    rainTfog.rgb = skyMIEP;
    rainTfog.a = 0.85 * fogDist * AFrain;
  #endif
*/

    v_texcoord0 = a_texcoord0;
    v_lightmapUV = a_texcoord1;
    v_color0 = color;
    v_fog = fogColor;
    v_cpos = a_position;
    v_wpos = worldPos;
    v_color1 = DirectLightColor;
    v_color2 = finalWColor;
    v_color3 = sunblPos;
    v_color4 = grblColor;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
}
