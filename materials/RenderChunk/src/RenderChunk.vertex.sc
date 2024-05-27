$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2
#endif
$output v_color0, v_fog, v_texcoord0, v_lightmapUV, v_cpos, v_wpos, v_wDisplace

#include <bgfx_shader.sh>

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 SkyColor;
uniform vec4 FogColor;
#include <azify/utils/functions.glsl>

void main() {
    mat4 model;
#ifdef INSTANCING
    model = mtxFromCols(i_data0, i_data1, i_data2, vec4(0, 0, 0, 1));
#else
    model = u_model[0];
#endif

    vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;
    vec4 color;
    color = a_color0;
    vec3 modelCamPos = (ViewPositionAndTime.xyz - worldPos);
    float camDis = length(modelCamPos);
    float relativeDist = camDis / FogAndDistanceControl.z;
    relativeDist += RenderChunkFogAlpha.x;
    vec4 fogColor;
    fogColor.rgb = FogColor.rgb;
    lowp float density = timecycle3(0.15, 5.5, 0.25);
    float fog;
    fog = smoothstep(FogAndDistanceControl.x, FogAndDistanceControl.y, relativeDist);
    fog += (1.0-fog)*(0.5-0.5*exp(-relativeDist*relativeDist*density));
    fogColor.a = fog;

#ifdef TRANSPARENT
    if(a_color0.a < 0.95) {
        color.a = mix(a_color0.a, 1.0, clamp((camDis / FogAndDistanceControl.w), 0.0, 1.0));
    };
#endif

    v_texcoord0 = a_texcoord0;
    v_lightmapUV = a_texcoord1;
    v_color0 = color;
    v_fog = fogColor;
    v_cpos = a_position;
    v_wpos = worldPos;
    gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));

  // WAVE MOVEMENTS
  #ifdef PLANTS_WAVE
  bool isColors = color.r != color.g || color.r != color.b;
  #if defined(ALPHA_TEST)
   if (isColors) {
     float time = ViewPositionAndTime.w * PLANTS_WAVE_SPEED;
     mediump vec3 tlpos = vec3(mod (a_position, vec3(2.0, 2.0, 2.0)));
     mediump float wz1 = sin(tlpos.z * 1.0 + time * 1.5) * 0.07;
     mediump float wz2 = sin(tlpos.x * 1.0 + time * 0.4) * 0.04;
     mediump float wz3 = cos(tlpos.z * 1.0 + time * 1.2) * 0.05;
     mediump float wz4 = sin(tlpos.y * 1.5 + time * 3.0) * 0.1;
     mediump float wz5 = sin(tlpos.z) * 0.1;
     gl_Position.x += wz1 + wz2 + wz3 + wz4 * wz5;
   }
  #endif
  #endif
}
