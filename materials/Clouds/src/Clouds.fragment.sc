$input v_color0, CubePosition, v_fogColor, v_fogControl, v_cpos

#include <bgfx_shader.sh>
#include <azify/utils/functions.glsl>

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;
/*
vec3 CloudBase(vec3 color, vec3 normalPos, float isDusk, float isNight, float isRain) {
  vec3 CloudCol_1; // Day
  CloudCol_1 = vec3(0.48, 0.65, 1.0);
  CloudCol_1 = mix (CloudCol_1, vec3(0.8,0.85,1.0), abs(normalPos.z));
  CloudCol_1 = mix (CloudCol_1, vec3(0.4, 0.5, 0.7), (-normalPos.y));

  vec3 CloudCol_2; // Dusk
  CloudCol_2 = vec3(0.41,0.39,0.55);
  CloudCol_2 = mix (CloudCol_2, vec3(0.9,0.8,1.04), abs(normalPos.z));
  CloudCol_2 = mix (CloudCol_2, vec3(0.75,0.52,0.58), (-normalPos.y));

  vec3 CloudCol_3; // Night
  CloudCol_3 = vec3(0.5,0.55,0.65);
  CloudCol_3 = mix (CloudCol_3, vec3(0.4,0.45,0.55), abs(normalPos.z));
  CloudCol_3 = mix (CloudCol_3, vec3(0.15,0.21,0.37), (-normalPos.y));

  vec3 CloudCol_4; // Rain
  CloudCol_4 = vec3(0.75, 0.75, 0.75);
  CloudCol_4 = mix (CloudCol_4, vec3(0.6,0.6,0.6), abs(normalPos.z));
  CloudCol_4 = mix (CloudCol_4, vec3(0.5,0.5,0.5), (-normalPos.y));

  vec3 CloudF;
  CloudF = mix(mix(CloudCol_1, CloudCol_2, isDusk), CloudCol_3, isNight);
  CloudF = mix (CloudF, mix(CloudCol_4, CloudCol_4*0.7, isNight), isRain);

  color = CloudF;
 return color;
}
*/
void main() {

#if defined(DEPTH_ONLY_OPAQUE)
   gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
#else
#include <azify/utils/components.glsl> // Components Files
  vec4 albedo;
  vec3 normal = normalize(cross(dFdx(v_cpos), dFdy(v_cpos)));
  //albedo.rgb = CloudBase(albedo.rgb, normal, AFdusk, AFnight, AFrain);
   gl_FragColor = vec4(albedo.rgb, v_color0.a);
#endif
}
