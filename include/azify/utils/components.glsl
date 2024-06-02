#ifndef COMPONENTS
 #define COMPONENTS

  // FINALIZED DETECTIONS =====================>>>>
  bool dev_UnWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
  bool dev_Nether = detectNether(FogColor.rgb, FogAndDistanceControl.xy);
  bool dev_End = detectEnd(FogColor.rgb, FogAndDistanceControl.xy);
  bool waterFlag = v_color0.b > 0.3 && v_color0.a < 0.95;
  // END OF DETECTIONS ==============>>>>
 
 
 
 #endif