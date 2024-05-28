$input v_texcoord0, v_color0, v_light, v_fog

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/DynamicUtil.dragonh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>

uniform vec4 ChangeColor;
uniform vec4 OverlayColor;
uniform vec4 ColorBased;
uniform vec4 MultiplicativeTintColor;
uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
#include <azify/utils/functions.glsl>

void main() {
#if DEPTH_ONLY
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
#else
    vec4 albedo;
    albedo.rgb = mix(vec3(1.0, 1.0, 1.0), v_color0.rgb, ColorBased.x);
    albedo.a = 1.0;

#if MULTI_COLOR_TINT
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
#else
    albedo = applyColorChange(albedo, ChangeColor, v_color0.a);
#endif
#include <azify/utils/components.glsl> // Components Files

    albedo = applyOverlayColor(albedo, OverlayColor);
    //albedo = applyLighting(albedo, v_light);
    float isCaveX = smoothstep(0.65, 0.1, v_light.b);
    float isTorch = smoothstep(0.5, 1.0, v_light.r);
    isTorch =  (pow(isTorch, 6.)*0.5+isTorch*0.5);

    #ifdef ENABLE_LIGHTS    
    if (dev_UnWater) {
      albedo.rgb *= vec3(0.4, 0.5, 0.8);
      
    } else if (dev_Nether) {
      mediump vec3 netherColor = vec3(0.38);
      netherColor = mix(netherColor, vec3(1.0), isTorch);
      albedo.rgb *= netherColor;
     
    } else if (dev_End) {
      mediump vec3 endColor = vec3(0.5);
      endColor = mix(endColor, vec3(1.0), isTorch);
      albedo.rgb *= endColor;
      
    } else {
      vec3 red = vec3(1.0,0.0, 0.0);
      vec3 gren = vec3(0.0, 1.0, 0.0);
      vec3 blue = vec3(0.0, 0.0, 1.0);
      mediump vec3 worldColor = timecycle3(vec3(0.9, 0.94, 1.0), vec3(0.34,0.26,0.22), vec3(0.43,0.43,0.67));
      worldColor = mix(worldColor, vec3(0.14,0.14,0.14), isCaveX);
      worldColor = mix(worldColor, vec3(1.0), isTorch);
      albedo.rgb *= worldColor;
    }
    #endif


#if ALPHA_TEST
    if (albedo.a < 0.5) {
        discard;
    }
#endif

    albedo.rgb = applyFog(albedo.rgb, v_fog.rgb, v_fog.a);
    gl_FragColor = albedo;
#endif // DEPTH_ONLY
}
