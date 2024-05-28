#ifndef FUNCTIONS
 #define FUNCTIONS
#include <azify/shader_inputs.glsl>

  // TIME AND WORLD DETECTIONS ========>>>>>>
 #define detect(a,b,x) clamp(((x)-(a))/((b)-(a)), 0.0, 1.0)
 #define disf ((FogAndDistanceControl.w-80.0)/112.0)
 lowp float AFnether = detect( 0.24, 0.13 - ( 0.08 * disf ), FogAndDistanceControl.x );
 lowp float AFrain = detect( 0.66 - ( 0.32 * disf ), 0.24, FogAndDistanceControl.x ) * ( 1.0 -  AFnether );
 lowp float AFnight = mix( detect( 0.65, 0.02, FogColor.r ), detect( 0.15, 0.01, FogColor.g ), AFrain );
 lowp float AFdusk = mix( detect( 1.0, 0.0, FogColor.b ), detect( 0.25, 0.15, FogColor.g ), AFrain );
 #define timecycle3( a, b, c ) mix( mix( a, b, AFdusk ), c, AFnight )
 #define timecycle4( a, b, c , d) mix(mix( mix( a, b, AFdusk ), c, AFnight ), d, AFrain)
 // END OF TIME DETECTIONS =========>>>>>>>>
 
 
 // DIMENSIONS DETECTIONS =====================>>>>
 bool detectUnderwater(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
    return FOG_CONTROL.x==0.0 && FOG_CONTROL.y<0.8 && (FOG_COLOR.b>FOG_COLOR.r || FOG_COLOR.g>FOG_COLOR.r);
  }
  
  bool detectNether(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
    float expectedFogX = 0.029 + (0.09*FOG_CONTROL.y*FOG_CONTROL.y);
    bool netherFogCtrl = (FOG_CONTROL.x<0.14  && abs(FOG_CONTROL.x-expectedFogX) < 0.02);
    bool netherFogCol = (FOG_COLOR.r+FOG_COLOR.g)>0.0;
    bool underLava = FOG_CONTROL.x == 0.0 && FOG_COLOR.b == 0.0 && FOG_COLOR.g < 0.18 && FOG_COLOR.r-FOG_COLOR.g > 0.1;
    return (netherFogCtrl && netherFogCol) || underLava;
  }
  
  bool detectEnd(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
      return (FOG_COLOR.r > FOG_COLOR.g && FOG_COLOR.b > FOG_COLOR.g && FOG_COLOR.r <= 0.05 && FOG_COLOR.g <= 0.05 && FOG_COLOR.b <= 0.05 && FOG_CONTROL.x >= 0.56 && FOG_CONTROL.x <= 0.8 && FOG_CONTROL.y >= 0.59);
  }
// END OF DIMENSIONS DETECTIONS ================>>>>>>>>>
 

// HASH FUNCTION 
float hash( float n ) {
    return fract(sin(n) * 43758.5453);
}

// NOISE FUNCTION
float noise(vec2 x) {
   vec2 p = floor(x);
   vec2 f = fract(x);
   float n = p.x + p.y * 57.0;
     f = f*f*(3.0-2.0*f);
   return mix(mix( hash(n+  0.0), hash(n+  1.0),f.x), mix( hash(n+ 57.0), hash(n+ 58.0),f.x),f.y);
}

float voronei( vec2 pos ) {
    vec2 p = vec2(pos);
    float tt = (ViewPositionAndTime.w * 0.85);
    mat2 m = mat2(7, -5, 5, 7) * 0.1;
    return min (length (fract (p + tt) - 0.5), length (fract ((p + 0.5 - tt * 0.2) * m) - 0.5));
}

// SKY FUNCTION
vec3 dynamicSky(lowp vec3 diff, mediump vec3 skyPos) {
  // CALCULATE SKY POSITIONS
  mediump float costheta = dot(skyPos, vec3(0.0, 1.6, 0.0));
  mediump float smoothing = pow(smoothstep(0.0, 1.0, costheta), 0.5);
  mediump float smoothingX = smoothstep(0.3, 1.8, costheta);
  // ABOVE UPPER COLOR
  lowp vec3 skyCol_X;
            skyCol_X = mix (mix (mix (vec3(0.0, 0.2, 0.45), vec3(0.2,0.11,0.3), AFdusk), vec3(0.0,0.0,0.15), AFnight), mix(vec3(0.8, 0.8, 0.8), vec3(0.1), AFnight), AFrain);
  // UPPER COLOR
  lowp vec3 skyCol_1;
            skyCol_1 = mix (mix (mix (vec3(SkyColor.xyz), vec3(0.9,0.8,1.04), AFdusk), vec3(0.1,0.15,0.3), AFnight), mix(vec3(0.8, 0.8, 0.8), vec3(0.1), AFnight), AFrain);
  // MIDDLE POINT COLOR
  lowp vec3 skyCol_2;
            skyCol_2 = mix (mix (mix (vec3(FogColor.xyz)+0.3, vec3(1.0, 0.43, 0.23)+0.16, AFdusk), vec3(0.35, 0.6, 0.8)+0.1, AFnight), mix(vec3(0.43, 0.43, 0.43), vec3(0.05), AFnight), AFrain);
  // UPPER BOTTOM COLOR
  lowp vec3 darkCol_1;
            darkCol_1 = mix (mix (mix (vec3(0.45, 0.5, 0.7), vec3(0.98, 0.2, 0.08), AFdusk), vec3(0.0, 0.1, 0.3), AFnight), mix(vec3(0.43, 0.43, 0.43), vec3(0.05), AFnight), AFrain);
  // BOTTOM COLOR
  lowp vec3 darkCol_2;
            darkCol_2 = mix (mix (mix (vec3(0.4, 0.5, 0.7), vec3(0.76, 0.1, 0.0), AFdusk), vec3(0.0, 0.0, 0.1), AFnight), mix(vec3(0.43, 0.43, 0.43), vec3(0.05), AFnight), AFrain);
// CALCULATE SKY COLORS
  mediump vec3 color;
    color = mix(skyCol_2, skyCol_1, smoothing);
    color = mix(color, skyCol_X, smoothingX);
    color = mix(color, darkCol_1, clamp(((costheta - 0.0)/(-0.3 - 0.0)), 0.0, 1.0));
    color = mix(color, darkCol_2, clamp(((costheta - -0.15)/(-0.65 - -0.15)), 0.0, 1.0));
    diff = (color);
  return diff;
}

float sunDirShadow(vec4 color0, vec2 lightmapUV) {
    mediump float shadow = smoothstep(0.885, 0.71, color0.y + 0.2 * (color0.y - color0.z));
    return mix(shadow, 0.0, pow(lightmapUV.x, 3.0));
}


#endif