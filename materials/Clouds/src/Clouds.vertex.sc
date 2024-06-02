$input a_color0, a_position
#ifdef INSTANCING
    $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, CubePosition, v_fogColor, v_fogControl, v_cpos

#include <bgfx_shader.sh>

uniform highp vec4 DistanceControl;
uniform highp vec4 CloudColor;

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
//#include <azify/utils/functions.glsl>

void main(){

  mat4 model;
#ifdef INSTANCING
  model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
  model = u_model[0];
#endif

     float tm = ViewPositionAndTime.w;
     vec3 p = a_position;
     vec3 worldPos = mul(model,vec4(p.x, p.y, p.z, 1.0)).xyz;
     worldPos.y *= 0.7;

#if defined(DEPTH_ONLY_OPAQUE)
    v_color0 = vec4(0.0, 0.0, 0.0, 0.0);
#else
    v_color0 = (a_color0);
#endif

#ifdef TRANSPARENT
     v_color0.a = mix(v_color0.a*0.9,1.0 - max((sqrt(dot(worldPos,worldPos))/DistanceControl.x) - 1.0,0.0),1.0);
#endif

     CubePosition = mul(model,vec4(a_position,1.0)).xyz;
     v_fogColor = FogColor;
     v_fogControl = FogAndDistanceControl.xy;
     v_cpos = worldPos;

     gl_Position = mul(u_viewProj, vec4(worldPos,1.0));
     float tm1 = ViewPositionAndTime.w;
     vec3 p3 = vec3(a_position.x, a_position.y, a_position.z);
     float w1 = sin(p3.z * 1.0 + tm1 * 1.5) * 0.47;
     float w2 = cos(p3.x * 1.0 + tm1 * 1.2) * 0.25;
     float w3 = sin(p3.y * 1.5 + tm1 * 3.0) * 0.13;
     gl_Position.x += w1+w2+w3;

}