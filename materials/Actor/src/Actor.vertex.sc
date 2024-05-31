$input a_position, a_color0, a_texcoord0, a_indices, a_normal
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2
#endif

$output v_color0, v_fog, v_light, v_texcoord0, v_wpos, v_color1, v_color2, v_color3, v_color4, v_color5

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>
#include <MinecraftRenderer.Materials/DynamicUtil.dragonh>
#include <MinecraftRenderer.Materials/TAAUtil.dragonh>

uniform vec4 ColorBased;
uniform vec4 ChangeColor;
uniform vec4 UseAlphaRewrite;
uniform vec4 TintedAlphaTestEnabled;
uniform vec4 MatColor;
uniform vec4 OverlayColor;
uniform vec4 TileLightColor;
uniform vec4 MultiplicativeTintColor;
uniform vec4 FogControl;
uniform vec4 ActorFPEpsilon;
uniform vec4 LightDiffuseColorAndIntensity;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform vec4 RenderChunkFogAlpha;
uniform mat4 Bones[8];
uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

#include <azify/utils/functions.glsl>
void main() {
    mat4 World = u_model[0];
    
    //StandardTemplate_InvokeVertexPreprocessFunction
    World = mul(World, Bones[int(a_indices)]);

    vec2 texcoord0 = a_texcoord0;
    texcoord0 = applyUvAnimation(texcoord0, UVAnimation);

    float lightIntensity = calculateLightIntensity(World, vec4(a_normal.xyz, 0.0), TileLightColor);
    lightIntensity += OverlayColor.a * 0.35;
    vec4 light = vec4(lightIntensity * TileLightColor.rgb, 1.0);
    
    //StandardTemplate_VertSharedTransform
    vec3 worldPosition;
#ifdef INSTANCING
    mat4 model;
    model[0] = vec4(i_data0.x, i_data1.x, i_data2.x, 0);
    model[1] = vec4(i_data0.y, i_data1.y, i_data2.y, 0);
    model[2] = vec4(i_data0.z, i_data1.z, i_data2.z, 0);
    model[3] = vec4(i_data0.w, i_data1.w, i_data2.w, 1);
    worldPosition = instMul(model, vec4(a_position, 1.0)).xyz;
#else
    worldPosition = mul(World, vec4(a_position, 1.0)).xyz;
#endif
#include <azify/utils/components.glsl> // Components Files
    vec4 position;// = mul(u_viewProj, vec4(worldPosition, 1.0));

    //StandardTemplate_InvokeVertexOverrideFunction
    position = jitterVertexPosition(worldPosition);
    float cameraDepth = position.z;
    float relativeDist = cameraDepth / FogControl.z;
    //relativeDist = saturate((relativeDist - FogControl.x) / (FogControl.y - FogControl.x));
     float density = timecycle3(0.15, 5.5, 0.25);
    float fogIntensity;
    fogIntensity = smoothstep(FogAndDistanceControl.x, FogAndDistanceControl.y, relativeDist);
    fogIntensity += (1.0-fogIntensity)*(0.5-0.5*exp(-relativeDist*relativeDist*density));
    
    
    vec3 skyPos = (worldPosition.xyz + vec3(0.0, 0.128, 0.0));
    vec3 nskyposP = normalize(skyPos);
    vec3 fogMie;
    vec3 skyMIEP = dynamicSky(FogColor.rgb, nskyposP);
    if (dev_UnWater) {
      fogMie = UNDERWATER_COLOR;
    } else if (dev_Nether || dev_End) {
      fogMie = FogColor.rgb;
    } else {
      fogMie = skyMIEP;
    }
    vec4 fogColor;
    fogColor.rgb = fogMie;
    fogColor.a = fogIntensity;

#if defined(DEPTH_ONLY)
    v_texcoord0 = vec2(0.0, 0.0);
    v_color0 = vec4(0.0, 0.0, 0.0, 0.0);
#else
    v_texcoord0 = texcoord0;
    v_color0 = a_color0;
#endif

  float isCaveX = smoothstep(0.65, 0.1, light.b);
  float isTorch = smoothstep(0.5, 1.0, light.r);
  isTorch =  (pow(isTorch, 6.)*0.5+isTorch*0.5);

  vec4 WorldColor;
  #ifdef ENABLE_LIGHTS    
  if (dev_UnWater) {
    WorldColor.rgb = vec3(0.4, 0.5, 0.8);
  } else {
    //vec3 red = vec3(1.0,0.0, 0.0);
    //vec3 gren = vec3(0.0, 1.0, 0.0);
    //vec3 blue = vec3(0.0, 0.0, 1.0);
    vec3 wcolor = timecycle3(vec3(0.9, 0.94, 1.0), vec3(0.54, 0.46, 0.42), vec3(0.43, 0.43, 0.67));
    wcolor = mix(wcolor, vec3(0.14,0.14,0.14), isCaveX);
    wcolor = mix(wcolor, vec3(1.0,1.0,1.0), isTorch);
    WorldColor.rgb = wcolor;
    }
    #endif

  // THICK RAIN FOG
  vec4 rainTfog;
  #ifdef RAIN_THICK_FOG
    float fogDist = clamp(dot(worldPosition, worldPosition) * 0.0008, 0.0, 1.0);
    rainTfog.rgb = skyMIEP;
    rainTfog.a = 0.85 * fogDist * AFrain;
  #endif
  
    v_color1 = WorldColor;
    v_color2 = rainTfog;
    v_wpos = position.xyz;
    v_fog = fogColor; 
    v_light = light;
    gl_Position = position;
}
