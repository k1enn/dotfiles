#!/bin/bash
# /* ---- 💫 https://github.com/JaKooLit 💫 ---- */  ##
# Script for keyboard backlights (if supported) using brightnessctl

# Get keyboard brightness
get_kbd_backlight() {
	echo $(brightnessctl -d '*::kbd_backlight' -m | cut -d, -f4)
}

# Notify
notify_user() {
	current=$(get_kbd_backlight | sed 's/%//')
	notify-send -e -h string:x-canonical-private-synchronous:brightness_notif -h int:value:$current -h boolean:SWAYNC_BYPASS_DND:true -u low "Keyboard" "Brightness:$current%"
}

# Change brightness
change_kbd_backlight() {
	brightnessctl -d *::kbd_backlight set "$1" && notify_user
}

# Execute accordingly
case "$1" in
	"--get")
		get_kbd_backlight
		;;
	"--inc")
		change_kbd_backlight "+30%"
		;;
	"--dec")
		change_kbd_backlight "30%-"
		;;
	*)
		get_kbd_backlight
		;;
esac
