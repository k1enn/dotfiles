#!/bin/sh
# dwl session launcher

# Display managers don't source the shell config -> dwl and the menu scripts in ~/.local/bin wouldn't be found
export PATH="$HOME/.local/bin:$PATH"

# Env so xdg-desktop-portal picks the wlroots backend (screensharing under dwl)
export XDG_CURRENT_DESKTOP=wlroots
export XDG_SESSION_TYPE=wayland
export XDG_SESSION_DESKTOP=dwl
# Set Qt Theme Engine
export QT_QPA_PLATFORMTHEME=qt6ct
export QT_QPA_PLATFORM=wayland
# Scale 1 (sharp text) -> enlarge Qt fonts 1.25x (96*1.25=120)
export QT_FONT_DPI=120
# Input method: set here (not autostart) so dwl and every app it spawns inherit it.
# GTK/Qt reach fcitx5 via these DBus IM modules; others (foot, Electron) use dwl text-input-v3 relay.
export GTK_IM_MODULE=fcitx QT_IM_MODULE=fcitx XMODIFIERS=@im=fcitx
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
