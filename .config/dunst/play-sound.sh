#!/bin/sh
# Called by dunst per notification (args: appname summary body icon urgency).
# Plays a bell; needs paplay (pipewire-pulse or pulseaudio).
exec paplay /usr/share/sounds/freedesktop/stereo/bell.oga
