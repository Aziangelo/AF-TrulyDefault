#ifndef COMPONENTS
 #define COMPONENTS
 
 
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





  // FINALIZED DETECTIONS =====================>>>>
  bool dev_UnWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
  bool dev_Nether = detectNether(FogColor.rgb, FogAndDistanceControl.xy);
  bool dev_End = detectEnd(FogColor.rgb, FogAndDistanceControl.xy);
  // END OF DETECTIONS ==============>>>>
 
 #endif