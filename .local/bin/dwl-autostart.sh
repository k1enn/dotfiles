#!/bin/sh
# Runs once dwl is up (via `dwl -s`). WAYLAND_DISPLAY is available here.

# Export wayland env to the user D-Bus + systemd --user so portals (screencast) work
dbus-update-activation-environment --systemd \
	WAYLAND_DISPLAY XDG_CURRENT_DESKTOP XDG_SESSION_TYPE XDG_SESSION_DESKTOP 2>/dev/null
# ponytail: portals are dbus-activated on demand; we only need the env above.

# Input method (Vietnamese/CJK). Env helps XWayland apps; native Wayland uses text-input.
export GTK_IM_MODULE=fcitx QT_IM_MODULE=fcitx XMODIFIERS=@im=fcitx
pgrep -x fcitx5 >/dev/null || fcitx5 -d &

# Idle: lock at 5min, lock before sleep (deps: swayidle, swaylock)
pgrep -x swayidle >/dev/null || swayidle -w \
	timeout 300 'swaylock -f -c 000000' \
	before-sleep 'swaylock -f -c 000000' &

# Notifications
pgrep -x dunst >/dev/null || dunst &

# Clipboard history (text + images) -> cliphist store; pick with MODKEY+v (see config.h)
wl-paste --type text  --watch cliphist store &
wl-paste --type image --watch cliphist store &

# Wallpaper
swaybg -i "$HOME/Pictures/wallpapers/a_black_and_white_image_of_a_zombie.jpeg" -m fill &
