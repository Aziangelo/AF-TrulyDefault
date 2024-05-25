$input v_color0, cl
#if defined(TRANSPARENT) && NL_CLOUD_TYPE == 2
  $input v_color1, v_color2, v_fogColor
#endif

#include <bgfx_shader.sh>

void main() {
  vec4 color = v_color0;
  
#if defined(TRANSPARENT)
  vec3 vDir = normalize(v_color0.xyz);

  color.a *= v_color0.a;

#endif

color = cl;

  gl_FragColor = color;
}
