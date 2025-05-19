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
    # Signal waybar to update immediately
    pkill -RTMIN+1 waybar
    ;;
  down)
    pactl set-sink-volume "$ACTIVE_SINK" -5%
    # Signal waybar to update immediately
    pkill -RTMIN+1 waybar
    ;;
  mute)
    pactl set-sink-mute "$ACTIVE_SINK" toggle
    # Signal waybar to update immediately
    pkill -RTMIN+1 waybar
    ;;
  get-volume)
    # Get volume and mute status
    VOLUME=$(pactl get-sink-volume "$ACTIVE_SINK" | grep -i "volume" | awk '{print $5}' | sed 's/%//')
    MUTE=$(pactl get-sink-mute "$ACTIVE_SINK" | grep -i "mute" | awk '{print $2}')
    
    # Get device description
    SINK_DESC=$(pactl list sinks | grep -A 20 "Name: $ACTIVE_SINK" | grep -i "description" | sed 's/.*Description: //')
    
    # Set icon based on device description and output plain text icons
    if [ "$MUTE" = "yes" ]; then
      ICON=""
    elif [[ "$SINK_DESC" == *"headphone"* ]]; then
      ICON=""
    elif [[ "$SINK_DESC" == *"headset"* ]]; then
      ICON=""
    elif [[ "$SINK_DESC" == *"handsfree"* ]]; then
      ICON=""
    elif [[ "$SINK_DESC" == *"car"* ]]; then
      ICON=""
    elif [[ "$SINK_DESC" == *"portable"* ]]; then
      ICON=""
    elif [[ "$SINK_DESC" == *"phone"* ]]; then
      ICON=""
    elif [[ "$ACTIVE_SINK" == *"usb"* ]]; then
      ICON=""
    else
      # Default icon based on volume level
      if [ "$VOLUME" -ge 70 ]; then
        ICON=""
      elif [ "$VOLUME" -ge 30 ]; then
        ICON=""
      else
        ICON=""
      fi
    fi
    
    # Output formatted text for waybar
    if [ "$MUTE" = "yes" ]; then
      echo "{\"text\":\"${ICON} muted\", \"class\":\"muted\"}"
    else
      echo "{\"text\":\"${ICON} ${VOLUME}%\", \"percentage\":$VOLUME}"
    fi
    ;;
  *)
    echo "Usage: $0 [up|down|mute|get-volume]"
    exit 1
    ;;
esac