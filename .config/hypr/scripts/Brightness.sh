#!/bin/bash
notification_timeout=1000
step=10 # INCREASE/DECREASE BY THIS VALUE

# Get current brightness as an integer (without %)
get_brightness() {
  brightnessctl -m | cut -d, -f4 | tr -d '%'
}

# Send notification
send_notification() {
  local brightness=$1

  notify-send -e \
    -h string:x-canonical-private-synchronous:brightness_notif \
    -h int:value:"$brightness" \
    -u low \
    "Screen" "Brightness: ${brightness}%"
}

# Change brightness and notify
change_brightness() {
  local delta=$1
  local current new

  current=$(get_brightness)
  new=$((current + delta))

  # Clamp between 5 and 100
  ((new < 5)) && new=5
  ((new > 100)) && new=100

  brightnessctl set "${new}%"

  send_notification "$new"
}

# Main
case "$1" in
"--get")
  get_brightness
  ;;
"--inc")
  change_brightness "$step"
  ;;
"--dec")
  change_brightness "-$step"
  ;;
*)
  get_brightness
  ;;
esac

