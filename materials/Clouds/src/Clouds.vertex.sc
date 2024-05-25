$input a_color0, a_position
#ifdef INSTANCING
  $input i_data0, i_data1, i_data2, i_data3
#endif
$output v_color0, cl

#include <bgfx_shader.sh>

uniform vec4 CloudColor;
uniform vec4 FogColor;
uniform vec4 FogAndDistanceControl;
uniform vec4 ViewPositionAndTime;

float noise(vec2 a) {
    a *= 1e1;
    mat2 m = mat2(-9, -11, 5, 7);
    float z = dot(sin(a), cos(a * 2.4e-2 * m));
    return exp(z / 3.0) / 1.5;
}

vec3 mCC(vec3 wt) {
return mix(mix(mix(vec3(0.9,0.95,1.0)+0.45, vec3(0.1,0.4,0.5), wt.z), vec3(1.0, 0.8, 0.75), wt.y), mix(vec3(0.7), vec3(0.25), wt.z), wt.x);}
vec3 sCC(vec3 wt) {
return mix(mix(mix(vec3(0.4,0.25,0.15
), vec3(0.06,0.11,0.18), wt.z), vec3(1.1, 0.3, 0.2), wt.y), mix(vec3(0.35), vec3(0.09), wt.z), wt.x);}

highp float calculateHash(highp vec3 p5) {
    return fract((p5.x * p5.y) + p5.z);
}

vec3 generateCloud1(vec3 col, highp vec3 pos, vec3 wT) {
 highp float dens, opac;
  for (int lIdx = 0; lIdx < 5; lIdx++) {
    highp vec2 dPos = (pos.xz / pos.y * (1.0 + float(lIdx) * 0.016)) * 4.0;
    highp vec2 dPosD = floor(dPos + ViewPositionAndTime.w * 0.195);
    highp vec3 hash = fract(vec3(dPosD.xyx) * 0.1031);
               hash += dot(hash, hash.yzx + 33.33);
    highp float hVal = calculateHash(hash);
    dens += (hVal > 0.78) ? 1.0 : 0.0;
    opac = mix(opac, 1.0, dens / ((float(10) * float(10) * float(10)) * 0.016));
  };

 dens = clamp(dens * 5.0, 0.0, 1.0);
 highp vec2 combFactors = (mix(0.5, 0.0, smoothstep(0.0, 1.0, length(pos.xz / (pos.y * 14.0)))) * vec2(dens, opac));

  if (pos.y > 0.0) {
    col = mix(mix(col, vec3(1.0), combFactors.x), vec3(0.5), combFactors.y);
  };
    return col;
}

void main() {
#ifdef TRANSPARENT

#ifdef INSTANCING
  mat4 model = mtxFromCols(i_data0, i_data1, i_data2, i_data3);
#else
  mat4 model = u_model[0];
#endif
  float t = ViewPositionAndTime.w;

  vec3 pos = a_position;
  vec4 color;
    pos.xz = pos.xz - 32.0;
    pos.y *= 0.01;
    vec3 worldPos;
    worldPos.x = pos.x*model[0][0];
    worldPos.z = pos.z*model[2][2];
    #if BGFX_SHADER_LANGUAGE_GLSL
      worldPos.y = pos.y+model[3][1];
    #else
      worldPos.y = pos.y+model[1][3];
    #endif
vec4 albedo0; vec3 wt;
float len = length(worldPos.xz)*0.01;
worldPos.y -= len*len*clamp(0.2*worldPos.y, -1.0, 1.0);
albedo0.rgb = generateCloud1(albedo0.rgb, worldPos.xyz, wt);
worldPos.y -= 1.5*color.a*3.3;
cl = albedo0;
  v_color0 = a_color0;
  gl_Position = mul(u_viewProj, vec4(worldPos, 1.0));
#else
  v_color0 = vec4(0.0,0.0,0.0,0.0);
  gl_Position = vec4(0.0,0.0,0.0,0.0);
#endif
}
