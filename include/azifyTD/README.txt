
Hi! 
The main editing section for this shader is in the { shader_inputs.glsl }
PLEASE be informed that this is just a test Feature for this shader and some functions might be limited.


You can edit the following:
• Enable and Disable Functions
• Increase and Decrease some Functions
• Change Any Colors of the Functions
• And lastly you can Enable some Beta Features!


Editing Tutorial:

==> Enable and Disabled:
#define color <== (This is Enabled)
//define color <== (This is Disabled)
- Just by adding > // < this will disable any functions and by removing it will enable the function. But you can't just do it carelessly because there is some code that can't be disabled like this.
#define color 1.0 <== (this function can't be disabled because it has a numeric indicator that process the functions)
#define color vec3(1.0, 1.0, 1.0) <== (this is insanely not good to disable)
- But don't worry I'll add an indicator like (toggle) if it can be disabled and can be edited at the same time. like this.
// [toggle]
#define color 1.0
- that means the function can be disabled and edited at the same time. But if the (toggle) is not mentioned on that function don't ever disable it!!!

==> Color Values:
vec3(RED, GREEN, BLUE)* BRIGHTNESS
- It should be decimal like this (1.0) not this (1)
- (1.0) is max you can always go further than that but only for certain situations, (0.0) is lowest.


- lmao, i hope my tutorial is understandable for you peace!! 😆
Have a nice day!!! 
- Azi Angelo