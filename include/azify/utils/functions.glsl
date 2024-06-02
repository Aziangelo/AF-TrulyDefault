#ifndef FUNCTIONS
 #define FUNCTIONS
#include <azify/shader_inputs.glsl>

  // TIME AND WORLD DETECTIONS ========>>>>>>
 #define detect(a,b,x) clamp(((x)-(a))/((b)-(a)), 0.0, 1.0)
// #define timecycle3( a, b, c , FGC,FND) mix(mix( a, b, AFdusk( FND,FGC), c, AFnight(FND,FGC )
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
    return fogColor.r>fogColor.g&&fogColor.b>fogColor.g&&fogColor.r<=0.05&&fogColor.g<=0.05&&fogColor.b<=0.05&&fogControl.x>=0.56&&fogControl.x<=0.8&&fogControl.y>=0.59;
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

// Define custom fract function if it's not available

vec2 rand2( vec2 p)	{
  return fract(vec2(sin(p.x * 591.32 + p.y * 154.077), cos(p.x * 391.32 + p.y * 49.077)));
}
float voronoi(vec2 x, float time){
		vec2 p = floor(x);
		vec2 f = fract(x);
		float res=8.0;
		for (int j=-1;j<=8;j++) {
			for (int i=-1;i<=1;i++){
				vec2 b = vec2(i,j);
				vec2 r = b-f+rand2(p+(b));
				float d = (r.x*r.x+r.y*r.y);
				res = min(res, d);
			}}
		return res;
}

// SKY FUNCTION
vec3 dynamicSky(vec3 diff, vec3 skyPos, float isNight, float isDusk, float isRain, vec3 skycolor, vec3 fogcolor) {
    // Precomputed constant Colors
    vec3 skyUpperColor = vec3(0.0, 0.2, 0.45);
    vec3 skyDuskUpperColor = vec3(0.2, 0.11, 0.3);
    vec3 skyNightUpperColor = vec3(0.0, 0.0, 0.15);
    vec3 skyRainUpperColor = vec3(0.65, 0.65, 0.65);
    
    vec3 skyBaseColor = skycolor;
    vec3 skyDuskBaseColor = vec3(0.9, 0.8, 1.04);
    vec3 skyNightBaseColor = vec3(0.1, 0.15, 0.3);
    vec3 skyRainBaseColor = vec3(0.8, 0.8, 0.8);
    
    vec3 midfogColor = fogcolor + 0.14;
    vec3 duskMiddleColor = vec3(1.0, 0.43, 0.23) + 0.16;
    vec3 nightMiddleColor = vec3(0.35, 0.6, 0.8) + 0.1;
    vec3 rainMiddleColor = vec3(0.43, 0.43, 0.43);
    
    vec3 upperBottomColor = vec3(0.45, 0.5, 0.7);
    vec3 duskUpperBottomColor = vec3(0.98, 0.4, 0.28);
    vec3 nightUpperBottomColor = vec3(0.0, 0.1, 0.3);
    vec3 rainUpperBottomColor = vec3(0.43, 0.43, 0.43);
    
    vec3 bottomColor = vec3(0.4, 0.5, 0.7);
    vec3 duskBottomColor = vec3(0.76, 0.3, 0.2);
    vec3 nightBottomColor = vec3(0.0, 0.0, 0.1);
    vec3 rainBottomColor = vec3(0.43, 0.43, 0.43);

    // Calculate sky positions
    float costheta = dot(skyPos, vec3(0.0, 1.6, 0.0));
    float smoothing1 = pow(smoothstep(0.0, 1.0, costheta), 0.5);
    float smoothingX = smoothstep(0.3, 1.8, costheta);
    float smoothing2 = clamp((costheta - 0.0) / (-0.3 - 0.0), 0.0, 1.0);
    float smoothing3 = clamp((costheta - (-0.15)) / (-0.65 - (-0.15)), 0.0, 1.0);

    // Calculate sky colors
    vec3 skyCol_X = mix(skyUpperColor, skyDuskUpperColor, isDusk);
    skyCol_X = mix(skyCol_X, skyNightUpperColor, isNight);
    skyCol_X = mix(skyCol_X, mix(skyRainUpperColor, vec3(0.15,0.15,0.15), isNight), isRain);
    vec3 skyCol_1 = mix(mix(mix(skyBaseColor, skyDuskBaseColor, isDusk), skyNightBaseColor, isNight), mix(skyRainBaseColor, vec3(0.25,0.25,0.25), isNight), isRain);
    vec3 skyCol_2 = mix(mix(mix(midfogColor, duskMiddleColor, isDusk), nightMiddleColor, isNight), mix(rainMiddleColor, vec3(0.3,0.3,0.3), isNight), isRain);
    vec3 darkCol_1 = mix(mix(mix(upperBottomColor, duskUpperBottomColor, isDusk), nightUpperBottomColor, isNight), mix(rainUpperBottomColor, vec3(0.05,0.05,0.05), isNight), isRain);
    vec3 darkCol_2 = mix(mix(mix(bottomColor, duskBottomColor, isDusk), nightBottomColor, isNight), mix(rainBottomColor, vec3(0.05,0.05,0.05), isNight), isRain);

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