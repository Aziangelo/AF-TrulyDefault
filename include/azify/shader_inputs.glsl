#ifndef SHADER_INPUTS
 #define SHADER_INPUTS
 
// PLEASE NOTE BEFORE EDITING!!!
// ==> READ THE (README.txt) FIRST!!!
#define AzifyTrulyDefaultv1.0


// FUNTCTIONS SETTING ==================>>>>>>>>>>>>
#define ENABLE_LIGHTS               //  [ TOGGLE ] <- DISABLE TO GET NIGHT VISION EFFECT

//#define RAIN_THICK_FOG              //  [ TOGGLE ]

#define SIMULATED_WATER             //  [ TOGGLE ]
#define WATER_SUNRAY                //  [ TOGGLE ]

#define SUN_BLOOM                   //  [ TOGGLE ]
#define SUN_BLOOM_STRENGTH 3.5      // MORE HIGH MORE BRIGHT!

#define GROUND_BLOOM                //  [ TOGGLE ]
#define GROUND_BLOOM_STRENGTH 0.3   // MORE HIGH MORE BRIGHT!

#define TORCHLIGHT_MODES 1          // [0]Vanilla [1]Smooth Vanilla [2]Low Light

#define DIRLIGHT_BOTTOM             //  [ TOGGLE ]

#define PLANTS_WAVE                 //  [ TOGGLE ]
#define PLANTS_WAVE_SPEED 1.0       // MORE HIGH MORE SPEED!

// COLORS SETTING ======================>>>>>>>>>>>>
lowp vec3 torchColor = vec3(1.0, 0.74, 0.33);       // OVERALL COLOR
lowp vec3 UNDERWATER_COLOR = vec3(0.25, 0.5, 0.7);

#endif