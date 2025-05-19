#!/bin/bash
# ~/.config/sway/scripts/volume-control.sh

function get_active_sink {
  pacmd list-sinks | grep "* index:" | awk '{print $3}'
}

case $1 in
  up)
    pactl set-sink-volume $(get_active_sink) +5%
    ;;
  down)
    pactl set-sink-volume $(get_active_sink) -5%
    ;;
  mute)
    pactl set-sink-mute $(get_active_sink) toggle
    ;;
esac

# Update Waybar
pkill -RTMIN+1 waybar
