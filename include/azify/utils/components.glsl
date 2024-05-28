#ifndef COMPONENTS
 #define COMPONENTS

  // FINALIZED DETECTIONS =====================>>>>
  bool dev_UnWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
  bool dev_Nether = detectNether(FogColor.rgb, FogAndDistanceControl.xy);
  bool dev_End = detectEnd(FogColor.rgb, FogAndDistanceControl.xy);
  // END OF DETECTIONS ==============>>>>
 
 #endif