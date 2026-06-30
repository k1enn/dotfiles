#!/bin/sh
# Refresh-rate / mode picker for dwl. MODKEY+r -> pick a mode for the internal output.
# Uses wlr-randr (Wayland). xrandr would only hit XWayland's virtual screen, not the panel.
# deps: wlr-randr, fuzzel
set -eu

have() { command -v "$1" >/dev/null 2>&1; }
have wlr-randr || { notify-send "refresh-menu" "wlr-randr not installed"; exit 1; }
have fuzzel    || { notify-send "refresh-menu" "fuzzel not installed";    exit 1; }

# Internal output (eDP*/LVDS*), else first output. Output names are un-indented lines.
out=$(wlr-randr | grep -E '^[A-Za-z]' | awk '{print $1}' | grep -iE '^eDP|^LVDS' | head -1)
[ -n "$out" ] || out=$(wlr-randr | grep -E '^[A-Za-z]' | awk '{print $1}' | head -1)
[ -n "$out" ] || { notify-send "refresh-menu" "no output found"; exit 1; }

# Mode lines under an output look like:  1536x864 px, 143.880 Hz  (current)
# Emit "1536x864@143.880Hz" for the picker; wlr-randr --mode accepts WxH@RHz.
mode=$(wlr-randr | awk -v o="$out" '
		$1==o {f=1; next}
		f && /^[A-Za-z]/ {exit}
		f && /px,/ { gsub(/,/,""); printf "%s@%sHz\n", $1, $3 }' \
	| fuzzel --dmenu -p "Refresh ($out): ") || exit 0

[ -n "$mode" ] || exit 0
wlr-randr --output "$out" --mode "$mode"
notify-send "Display" "$out -> $mode"
