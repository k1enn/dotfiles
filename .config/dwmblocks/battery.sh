#!/bin/sh
battery_percent=$(cat /sys/class/power_supply/BAT1/capacity 2>/dev/null || echo "N/A")
printf "%s%%\n" "$battery_percent"

