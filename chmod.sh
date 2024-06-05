#!/bin/bash

G='\e[0;32m'
RESET='\e[0m'

echo -n -e "\e[0;33m"
text="Chmodding All Files with .sh, .bat and shaderc...
"
delay=0.000001
for i in $(seq 0 $((${#text} - 1))); do
echo -n "${text:$i:1}"
sleep $delay
done

for file in *.sh *.bat env/bin/shaderc env/lib/*.so; do
  chmod +x "$file"
done
echo -e "$G>> Done $RESET"


echo -n -e "\e[0;33m"
text="Run setup.sh automatically?
"
delay=0.000001
for i in $(seq 0 $((${#text} - 1))); do
echo -n "${text:$i:1}"
sleep $delay
done
echo -n -e "$RESET"

read -p "click y to confirm n to exit (y/n): " choice
if [ "$choice" = "y" ] || [ "$choice" = "Y" ]; then
    echo "setup.sh is Running..."
    ./setup.sh
else
    text="Exiting... 
"
    delay=0.0001
    for i in $(seq 0 $((${#text} - 1))); do
    echo -n "${text:$i:1}"
    sleep $delay
    done
fi

echo -e "$G>> Done $RESET"
