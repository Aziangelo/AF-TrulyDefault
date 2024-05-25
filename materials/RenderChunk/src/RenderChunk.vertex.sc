$input a_color0, a_position, a_texcoord0, a_texcoord1
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2
#endif
$output v_color0, v_fog, v_texcoord0, v_lightmapUV, v_cpos, v_wpos

#include <bgfx_shader.sh>

uniform vec4 RenderChunkFogAlpha;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
uniform vec4 FogColor;

void main() {
    mat4 model;
#ifdef INSTANCING
    model = mtxFromCols(i_data0, i_data1, i_data2, vec4(0, 0, 0, 1));
#else
    model = u_model[0];
#endif

    vec3 worldPos = mul(model, vec4(a_position, 1.0)).xyz;
    vec4 color;
#ifdef RENDER_AS_BILLBOARDS
    worldPos += vec3(0.5, 0.5, 0.5);
    vec3 viewDir = normalize(worldPos - ViewPositionAndTime.xyz);
    vec3 boardPlane = normalize(vec3(viewDir.z, 0.0, -viewDir.x));
    worldPos = (worldPos -
        ((((viewDir.yzx * boardPlane.zxy) - (viewDir.zxy * boardPlane.yzx)) *
        (a_color0.z - 0.5)) +
        (boardPlane * (a_color0.x - 0.5))));
    color = vec4(1.0, 1.0, 1.0, 1.0);
#else
    color = a_color0;
#endif

    vec3 modelCamPos = (ViewPositionAndTime.xyz - worldPos);
    float camDis = length(modelCamPos);
    float relativeDist = camDis / FogAndDistanceControl.z;
    relativeDist += RenderChunkFogAlpha.x;
    vec4 fogColor;
    fogColor.rgb = FogColor.rgb;
    float density = 0.2*(19.0 - 18.0*FogColor.g);
    fogColor.a = smoothstep(FogAndDistanceControl.x, FogAndDistanceControl.y, relativeDist);
    fogColor.a += (1.0-fogColor.a)*(0.3-0.3*exp(-relativeDist*relativeDist*density));

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

bool isColors = color.r != color.g || color.r != color.b;
#if defined(ALPHA_TEST)
if (isColors) {
  float time = ViewPositionAndTime.w;
  vec3 tlpos = a_position;
     tlpos.x = mod(a_position.x, 2.0);
     tlpos.z = mod(a_position.z, 2.0);
     tlpos.y = mod(a_position.y, 2.0);
  float wz1 = sin(tlpos.z * 1.0 + time * 1.5) * 0.07;
  float wz2 = sin(tlpos.x * 1.0 + time * 0.4) * 0.04;
  float wz3 = cos(tlpos.z * 1.0 + time * 1.2) * 0.05;
  float wz4 = sin(tlpos.y * 1.5 + time * 3.0) * 0.1;
  float wz5 = sin(tlpos.z) * 0.1;
     gl_Position.x += wz1 + wz2 + wz3 + wz4 * wz5;
  }
#endif
}
