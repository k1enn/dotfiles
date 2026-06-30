#!/bin/sh
# dwl session launcher

# Env so xdg-desktop-portal picks the wlroots backend (screensharing under dwl)
export XDG_CURRENT_DESKTOP=wlroots
export XDG_SESSION_TYPE=wayland
export XDG_SESSION_DESKTOP=dwl

# -s runs the autostart AFTER the compositor is up (WAYLAND_DISPLAY is set then)
exec slstatus -s | dwl -s "$HOME/.local/bin/dwl-autostart.sh"
