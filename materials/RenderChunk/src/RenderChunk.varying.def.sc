vec4 a_color0     : COLOR0;
vec3 a_position   : POSITION;
vec2 a_texcoord0  : TEXCOORD0;
vec2 a_texcoord1  : TEXCOORD1;

vec4 i_data0 : TEXCOORD7;
vec4 i_data1 : TEXCOORD6;
vec4 i_data2 : TEXCOORD5;

vec4          v_color0     : COLOR0;
vec4          v_fog        : COLOR2;
centroid vec2 v_texcoord0  : TEXCOORD0;
vec2          v_lightmapUV : TEXCOORD1;
//vec2          v_wDisp      : TEXCOORD2;
vec4          v_color1     : COLOR1; // START OF DIFFUSING
vec4          v_color2     : COLOR3;
vec4          v_color3     : COLOR4;
vec4          v_color4     : COLOR5;
vec4          v_color5     : COLOR6;
vec4          v_color6     : COLOR7;
vec4          v_color7     : COLOR8;
vec4          v_color8     : COLOR9;
vec4          v_color9     : COLOR10;
vec4          v_color10    : COLOR11;
vec4          v_color11    : COLOR12;
vec4          v_color12    : COLOR13; // end
vec3          v_cpos       : POSITION1; // Chunk Pos
vec3          v_wpos       : POSITION2; // World Pos