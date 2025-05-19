#!/bin/bash

# Get the USB-C headphone sink name
USB_SINK="alsa_output.usb-*"

# Get current default sink
DEFAULT_SINK=$(pactl get-default-sink)

# Find USB sink if connected
USB_CONNECTED=$(pactl list short sinks | grep -i "$USB_SINK" | awk '{print $2}')

# Determine active sink - use USB if connected, otherwise use default
ACTIVE_SINK=${USB_CONNECTED:-$DEFAULT_SINK}

case "$1" in
  up)
    pactl set-sink-volume "$ACTIVE_SINK" +5%
    ;;
  down)
    pactl set-sink-volume "$ACTIVE_SINK" -5%
    ;;
  mute)
    pactl set-sink-mute "$ACTIVE_SINK" toggle
    ;;
  get-volume)
    # Get volume and mute status
    VOLUME=$(pactl get-sink-volume "$ACTIVE_SINK" | grep -i "volume" | awk '{print $5}')
    MUTE=$(pactl get-sink-mute "$ACTIVE_SINK" | grep -i "mute" | awk '{print $2}')
    
    # Get device type
    TYPE="default"
    if [[ "$ACTIVE_SINK" == *"usb"* ]]; then
      TYPE="headphones"
    fi
    
    # Output plain text for waybar
    if [ "$MUTE" = "yes" ]; then
      echo " muted"
    else
      echo " $VOLUME"
    fi
    ;;
  *)
    echo "Usage: $0 [up|down|mute|get-volume]"
    exit 1
    ;;
esac