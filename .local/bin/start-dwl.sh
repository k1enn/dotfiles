#!/bin/sh
# dwl session launcher

# Env so xdg-desktop-portal picks the wlroots backend (screensharing under dwl)
export XDG_CURRENT_DESKTOP=wlroots
export XDG_SESSION_TYPE=wayland
export XDG_SESSION_DESKTOP=dwl
# Set Qt Theme Engine
export QT_QPA_PLATFORMTHEME=qt6ct
export QT_QPA_PLATFORM=wayland
# Electron/Chromium apps default to XWayland -> blurry under fractional scale.
# Hint makes them run native Wayland. Covers VSCode, Discord, Chromium, etc.
export ELECTRON_OZONE_PLATFORM_HINT=auto
# code wrapper reads $XDG_CONFIG_HOME/code-flags.conf; guarantee it points at
# ~/.config so those flags (--disable-features=WaylandFractionalScaleV1 for
# crisp fractional scaling) actually load.
export XDG_CONFIG_HOME="${XDG_CONFIG_HOME:-$HOME/.config}"

# -s runs the autostart AFTER the compositor is up (WAYLAND_DISPLAY is set then)
# dwl-status.sh feeds the bar over stdin (dwl reads one line -> stext)
# stdbuf -oL: force line-buffering so each status line reaches dwl immediately
# instead of sitting in a block-buffered pipe (bar stays empty otherwise)
exec stdbuf -oL "$HOME/.local/bin/dwl-status.sh" | dwl -s "$HOME/.local/bin/dwl-autostart.sh"
