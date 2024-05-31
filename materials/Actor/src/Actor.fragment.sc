$input v_color0, v_fog, v_light, v_texcoord0, v_wpos, v_color1, v_color2, v_color3, v_color4, v_color5

#include <bgfx_shader.sh>
#include <MinecraftRenderer.Materials/ActorUtil.dragonh>
#include <MinecraftRenderer.Materials/FogUtil.dragonh>

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
uniform vec4 LightDiffuseColorAndIlluminance;
uniform vec4 LightWorldSpaceDirection;
uniform vec4 HudOpacity;
uniform vec4 UVAnimation;
uniform mat4 Bones[8];
uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

SAMPLER2D(s_MatTexture, 0);
SAMPLER2D(s_MatTexture1,1);
#include <azify/utils/functions.glsl>

void main() {
#if DEPTH_ONLY
    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    return;
#else

    vec4 albedo = getActorAlbedoNoColorChange(v_texcoord0, s_MatTexture, s_MatTexture1, MatColor);

#if ALPHA_TEST
    float alpha = mix(albedo.a, (albedo.a * OverlayColor.a), TintedAlphaTestEnabled.x);
    if(shouldDiscard(albedo.rgb, alpha, ActorFPEpsilon.x)) {
        discard;
    }
#endif // ALPHA_TEST

#if CHANGE_COLOR_MULTI
    albedo = applyMultiColorChange(albedo, ChangeColor.rgb, MultiplicativeTintColor.rgb);
#elif CHANGE_COLOR
    albedo = applyColorChange(albedo, ChangeColor, albedo.a);
    albedo.a *= ChangeColor.a;
#endif // CHANGE_COLOR_MULTI

#if ALPHA_TEST
    albedo.a = max(UseAlphaRewrite.r, albedo.a);
#endif
#include <azify/utils/components.glsl> // Components Files

    //albedo = applyActorDiffuse(albedo, v_color0.rgb, vec4(1.0), ColorBased.x, OverlayColor);

    #ifdef ENABLE_LIGHTS    
    albedo *= v_color1;
    #endif

#if TRANSPARENT
    albedo = applyHudOpacity(albedo, HudOpacity.x);
#endif

  // THICK RAIN FOG
  #ifdef RAIN_THICK_FOG
     albedo.rgb = mix(albedo.rgb, v_color2.rgb, v_color2.a);
  #endif

    // SKY BASED FOG
    albedo.rgb = mix(albedo.rgb, v_fog.rgb, v_fog.a);
    gl_FragColor = albedo;
#endif // DEPTH_ONLY
}
