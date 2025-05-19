#!/bin/bash
# Save as ~/.config/sway/scripts/audio-control.sh

# Determine which audio system is running
if pgrep -x pipewire >/dev/null; then
  # PipeWire approach
  case "$1" in
    up) wpctl set-volume -l 1.5 @DEFAULT_AUDIO_SINK@ 5%+ ;;
    down) wpctl set-volume @DEFAULT_AUDIO_SINK@ 5%- ;;
    mute) wpctl set-mute @DEFAULT_AUDIO_SINK@ toggle ;;
  esac
else
  # PulseAudio approach
  case "$1" in
    up) pactl set-sink-volume @DEFAULT_SINK@ +5% ;;
    down) pactl set-sink-volume @DEFAULT_SINK@ -5% ;;
    mute) pactl set-sink-mute @DEFAULT_SINK@ toggle ;;
  esac
fi

# For debugging
echo "Command executed: $1" >> /tmp/audio-debug.log