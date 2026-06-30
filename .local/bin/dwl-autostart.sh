#!/bin/sh
# Runs once dwl is up (via `dwl -s`). WAYLAND_DISPLAY is available here.

# Export wayland env to the user D-Bus + systemd --user so portals (screencast) work
dbus-update-activation-environment --systemd \
	WAYLAND_DISPLAY XDG_CURRENT_DESKTOP XDG_SESSION_TYPE XDG_SESSION_DESKTOP 2>/dev/null
# ponytail: portals are dbus-activated on demand; we only need the env above.

# Notifications
pgrep -x dunst >/dev/null || dunst &

# Clipboard history (text + images) -> cliphist store; pick with MODKEY+v (see config.h)
wl-paste --type text  --watch cliphist store &
wl-paste --type image --watch cliphist store &

# Wallpaper
swaybg -i "$HOME/Pictures/wallpapers/a_black_and_white_image_of_a_zombie.jpeg" -m fill &
