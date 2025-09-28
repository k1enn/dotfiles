#!/bin/sh

# Handle click actions
case $BLOCK_BUTTON in
    1) /home/k1en/.config/dwmblocks/cycle-power-profile.sh && kill -44 $(pidof dwmblocks) ;; # Left click to cycle
    3) tuned-adm list ;; # Right click to show all profiles (in terminal)
esac

# Get current tuned profile
current_profile=$(tuned-adm active 2>/dev/null | awk '{print $4}')

# Display with shortened names for status bar
case "$current_profile" in
    "balanced") echo "balanced" ;;
    "powersave") echo "saver" ;;
    "throughput-performance") echo "performance" ;;
    "laptop-battery-powersave") echo "battery" ;;
    "laptop-ac-powersave") echo "AC" ;;
    "desktop") echo "desktop" ;;
    *) echo "Power: $current_profile" ;;
esac

