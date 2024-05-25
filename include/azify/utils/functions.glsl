#ifndef FUNCTIONS
 #define FUNCTIONS



// TIME AND WORLD DETECTIONS
 #define detect(a,b,x) clamp(((x)-(a))/((b)-(a)), 0.0, 1.0)
 #define disf ((FogAndDistanceControl.w-80.0)/112.0)
 lowp float AFnether = detect( 0.24, 0.13 - ( 0.08 * disf ), FogAndDistanceControl.x );
 lowp float AFrain = detect( 0.75 - ( 0.15 * disf ), 0.24, FogAndDistanceControl.x ) * ( 1.0 -  AFnether );
 lowp float AFnight = mix( detect( 0.65, 0.02, FogColor.r ), detect( 0.15, 0.01, FogColor.g ), AFrain );
 lowp float AFdusk = mix( detect( 1.0, 0.0, FogColor.b ), detect( 0.25, 0.15, FogColor.g ), AFrain );
 #define timecycle( a, b, c ) mix( mix( a, b, AFdusk ), c, AFnight )

// NOISE
float hash( float n ) {
    return fract(sin(n)*43758.5453);
}
float noise(vec2 x) {
 vec2 p = floor(x);
 vec2 f = fract(x);
   f = f*f*(3.0-2.0*f);
   float n = p.x + p.y*57.0;
   return mix(mix( hash(n+  0.0), hash(n+  1.0),f.x), mix( hash(n+ 57.0), hash(n+ 58.0),f.x),f.y);
}

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

vec3 dynamicSky(lowp vec3 diff, mediump vec3 skyPos) {
  mediump float costheta = dot(skyPos, vec3(0.0, 1.6, 0.0));
  mediump float smoothing = pow(smoothstep(0.0, 1.0, costheta), 0.5);
  mediump float smoothingX = pow(smoothstep(0.3, 1.5, costheta), 0.5);

  mediump vec3 skyCol_X;
       skyCol_X = mix (mix (mix (vec3(0.0, 0.2, 0.45), vec3(0.65,0.2,0.35)*0.85, AFdusk), mix(vec3(0.8, 0.8, 0.8), vec3(0.1), AFnight), AFrain), vec3(0.1,0.15,0.3), AFnight);
  mediump vec3 skyCol_1;
       skyCol_1 = mix (mix (mix (vec3(SkyColor.xyz), vec3(0.9,0.8,1.04), AFdusk), mix(vec3(0.8, 0.8, 0.8), vec3(0.1), AFnight), AFrain), vec3(0.1,0.15,0.3), AFnight);


  mediump vec3 skyCol_2;
       skyCol_2 = mix (mix (mix (vec3(FogColor.xyz)+0.25, vec3(1.0, 0.43, 0.23)+0.16, AFdusk), mix(vec3(0.43, 0.43, 0.43), vec3(0.05), AFnight), AFrain), vec3(0.35, 0.6, 0.8)+0.1, AFnight);

  mediump vec3 darkCol_1;
       darkCol_1 = mix (mix (mix (vec3(SkyColor.xyz)*0.8, vec3(0.66, 0.1, 0.0), AFdusk),mix(vec3(0.43, 0.43, 0.43), vec3(0.05), AFnight), AFrain), vec3(0.0, 0.0, 0.1), AFnight);

  mediump vec3 darkCol_2;
       darkCol_2 = (mix (mix (mix (vec3(0.1, 0.6, 1.0), vec3(0.98, 0.2, 0.08), AFdusk),mix(vec3(0.43, 0.43, 0.43), vec3(0.05), AFnight), AFrain), vec3(0.0, 0.1, 0.3), AFnight));


  mediump vec3 color;
    color = mix(skyCol_2, skyCol_1, smoothing);
    color = mix(color, skyCol_X, smoothingX);
    color = mix(color, darkCol_2, clamp(((costheta - 0.0)/(-0.3 - 0.0)), 0.0, 1.0));
    color = mix(color, darkCol_1, clamp(((costheta - -0.15)/(-0.65 - -0.15)), 0.0, 1.0));
    diff = (color);
  return diff;
}

#endif