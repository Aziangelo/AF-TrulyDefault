#ifndef FUNCTIONS
 #define FUNCTIONS
#include <azify/shader_inputs.glsl>

  // TIME AND WORLD DETECTIONS ========>>>>>>
 #define detect(a,b,x) clamp(((x)-(a))/((b)-(a)), 0.0, 1.0)
 #define disf ((FogAndDistanceControl.w-80.0)/112.0)
  float AFnether = detect( 0.24, 0.13 - ( 0.08 * disf ), FogAndDistanceControl.x );
  float AFrain = smoothstep(0.66, 0.3, FogAndDistanceControl.x);
  float AFnight = mix( detect( 0.65, 0.02, FogColor.r ), detect( 0.15, 0.01, FogColor.g ), AFrain );
  float AFdusk = mix( detect( 1.0, 0.0, FogColor.b ), detect( 0.25, 0.15, FogColor.g ), AFrain );
 #define timecycle3( a, b, c ) mix(mix( a, b, AFdusk ), c, AFnight )
 #define timecycle4( a, b, c , d) mix(mix( mix( a, b, AFdusk ), c, AFnight ), d, AFrain)
 // END OF TIME DETECTIONS =========>>>>>>>>
 
 
 // DIMENSIONS DETECTIONS =====================>>>>
 bool detectUnderwater(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
    return (FOG_CONTROL.x==0.0 && FOG_CONTROL.y<0.8) && (FOG_COLOR.b>FOG_COLOR.r || FOG_COLOR.g>FOG_COLOR.r);
  }
  
  bool detectNether(vec3 FOG_COLOR, vec2 FOG_CONTROL) {
    float expectedFogX = 0.029 + (0.09*FOG_CONTROL.y*FOG_CONTROL.y);
    bool netherFogCtrl = (FOG_CONTROL.x<0.14  && abs(FOG_CONTROL.x-expectedFogX) < 0.02);
    bool netherFogCol = (FOG_COLOR.r+FOG_COLOR.g)>0.0;
    bool underLava = FOG_CONTROL.x == 0.0 && FOG_COLOR.b == 0.0 && FOG_COLOR.g < 0.18 && FOG_COLOR.r-FOG_COLOR.g > 0.1;
    return (netherFogCtrl && netherFogCol) || underLava;
  }
  
  bool detectEnd(vec3 fogColor, vec2 fogControl) {
    bool isColInRange = all(lessThanEqual(fogColor, vec3(0.05)));
    bool isColV = fogColor.r > fogColor.g && fogColor.b > fogColor.g;
    bool isFogV = fogControl.x >= 0.56 && fogControl.x <= 0.8 && fogControl.y >= 0.59;
    return isColInRange && isColV && isFogV;
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
vec3 dynamicSky(vec3 diff, vec3 skyPos) {
    // Precomputed constant Colors
    const vec3 skyUpperColor = vec3(0.0, 0.2, 0.45);
    const vec3 skyDuskUpperColor = vec3(0.2, 0.11, 0.3);
    const vec3 skyNightUpperColor = vec3(0.0, 0.0, 0.15);
    const vec3 skyRainUpperColor = vec3(0.65, 0.65, 0.65);
    
    vec3 skyBaseColor = vec3(SkyColor.xyz);
    const vec3 skyDuskBaseColor = vec3(0.9, 0.8, 1.04);
    const vec3 skyNightBaseColor = vec3(0.1, 0.15, 0.3);
    const vec3 skyRainBaseColor = vec3(0.8, 0.8, 0.8);
    
    vec3 fogColor = vec3(FogColor.xyz) + 0.14;
    const vec3 duskMiddleColor = vec3(1.0, 0.43, 0.23) + 0.16;
    const vec3 nightMiddleColor = vec3(0.35, 0.6, 0.8) + 0.1;
    const vec3 rainMiddleColor = vec3(0.43, 0.43, 0.43);
    
    const vec3 upperBottomColor = vec3(0.45, 0.5, 0.7);
    const vec3 duskUpperBottomColor = vec3(0.98, 0.4, 0.28);
    const vec3 nightUpperBottomColor = vec3(0.0, 0.1, 0.3);
    const vec3 rainUpperBottomColor = vec3(0.43, 0.43, 0.43);
    
    const vec3 bottomColor = vec3(0.4, 0.5, 0.7);
    const vec3 duskBottomColor = vec3(0.76, 0.3, 0.2);
    const vec3 nightBottomColor = vec3(0.0, 0.0, 0.1);
    const vec3 rainBottomColor = vec3(0.43, 0.43, 0.43);

    // Calculate sky positions
    float costheta = dot(skyPos, vec3(0.0, 1.6, 0.0));
    float smoothing1 = pow(smoothstep(0.0, 1.0, costheta), 0.5);
    float smoothingX = smoothstep(0.3, 1.8, costheta);
    float smoothing2 = clamp((costheta - 0.0) / (-0.3 - 0.0), 0.0, 1.0);
    float smoothing3 = clamp((costheta - (-0.15)) / (-0.65 - (-0.15)), 0.0, 1.0);

    // Calculate sky colors
    vec3 skyCol_X = mix(mix(mix(skyUpperColor, skyDuskUpperColor, AFdusk), skyNightUpperColor, AFnight), mix(skyRainUpperColor, vec3(0.1), AFnight), AFrain);
    vec3 skyCol_1 = mix(mix(mix(skyBaseColor, skyDuskBaseColor, AFdusk), skyNightBaseColor, AFnight), mix(skyRainBaseColor, vec3(0.25), AFnight), AFrain);
    vec3 skyCol_2 = mix(mix(mix(fogColor, duskMiddleColor, AFdusk), nightMiddleColor, AFnight), mix(rainMiddleColor, vec3(0.3), AFnight), AFrain);
    vec3 darkCol_1 = mix(mix(mix(upperBottomColor, duskUpperBottomColor, AFdusk), nightUpperBottomColor, AFnight), mix(rainUpperBottomColor, vec3(0.05), AFnight), AFrain);
    vec3 darkCol_2 = mix(mix(mix(bottomColor, duskBottomColor, AFdusk), nightBottomColor, AFnight), mix(rainBottomColor, vec3(0.05), AFnight), AFrain);

    vec3 color = mix(skyCol_2, skyCol_1, smoothing1);
    color = mix(color, skyCol_X, smoothingX);
    color = mix(color, darkCol_1, smoothing2);
    color = mix(color, darkCol_2, smoothing3);

    diff = color;
    return diff;
}

float sunDirShadow(vec4 color0, vec2 lightmapUV) {
     float shadow = smoothstep(0.885, 0.71, color0.y + 0.2 * (color0.y - color0.z));
    return mix(shadow, 0.0, pow(lightmapUV.x, 3.0));
}

#endif