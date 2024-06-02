#ifndef COMPONENTS
 #define COMPONENTS

  // FINALIZED DETECTIONS =====================>>>>
  bool dev_UnWater = detectUnderwater(FogColor.rgb, FogAndDistanceControl.xy);
  bool dev_Nether = detectNether(FogColor.rgb, FogAndDistanceControl.xy);
  bool dev_End = detectEnd(FogColor.rgb, FogAndDistanceControl.xy);
  bool waterFlag = v_color0.b > 0.3 && v_color0.a < 0.95;
  // END OF DETECTIONS ==============>>>>
 
 float AFrain = smoothstep(0.66, 0.3, FogAndDistanceControl.x);
 float AFnight = mix( detect( 0.65, 0.02, FogColor.r ), detect( 0.15, 0.01, FogColor.g ), AFrain);
 float AFdusk = mix( detect( 1.0, 0.0, FogColor.b ), detect( 0.25, 0.15, FogColor.g ), AFrain);
 
 #endif