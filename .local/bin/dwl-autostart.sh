#!/bin/sh
# Runs once dwl is up (via `dwl -s`). WAYLAND_DISPLAY is available here.

# Export wayland env to the user D-Bus + systemd --user so portals (screencast) work
dbus-update-activation-environment --systemd \
	WAYLAND_DISPLAY XDG_CURRENT_DESKTOP XDG_SESSION_TYPE XDG_SESSION_DESKTOP 2>/dev/null
# dbus-update-activation-environment only fixes FUTURE activations. If the portal
# stack was already socket-activated at login (stale env, no WAYLAND_DISPLAY) OBS
# would talk to those and screencast fails. Restart them so they re-inherit the
# env above. Portal-only (not pipewire) — pipewire negotiates fds regardless and
# restarting it risks audio glitches. `|| true`: units absent on first login.
systemctl --user restart xdg-desktop-portal xdg-desktop-portal-wlr 2>/dev/null || true

# Secret Service: stores network-share / saved passwords so file managers
# (Thunar/Nautilus/Dolphin) and gvfs don't re-prompt every session. Best
# auto-unlocked by PAM at login (see README); this is the fallback start so the
# daemon at least exists. dep: gnome-keyring
if ! pgrep -f gnome-keyring-daemon >/dev/null; then
	eval "$(gnome-keyring-daemon --start --components=secrets,ssh,pkcs11 2>/dev/null)"
	export SSH_AUTH_SOCK
	dbus-update-activation-environment --systemd SSH_AUTH_SOCK 2>/dev/null
fi

# Polkit authentication agent: without it the session has nothing to authorize
# privileged actions, so mounting a drive (udisks2) — which Thunar does on open
# — fails or nags. Pair with the no-password mount rule in README. dep: polkit-gnome
pgrep -f polkit-gnome-authentication-agent-1 >/dev/null \
	|| /usr/lib/polkit-gnome/polkit-gnome-authentication-agent-1 &

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
