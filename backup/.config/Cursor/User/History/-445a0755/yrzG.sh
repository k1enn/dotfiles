#!/bin/bash
# Save as ~/.config/sway/scripts/usb-audio-control.sh

# Get the USB-C headphone sink name (run 'pactl list short sinks' to find this)
USB_SINK="alsa_output.usb-*" # Replace with your actual USB-C device pattern

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
  *)
    echo "Usage: $0 [up|down|mute]"
    exit 1
    ;;
esac

# For debugging
echo "Command executed: $1" >> /tmp/audio-debug.log