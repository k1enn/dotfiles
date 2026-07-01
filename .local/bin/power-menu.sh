#!/bin/sh
# Power-profile picker for dwl. MODKEY+a -> pick asusctl profile.
# deps: asusctl, fuzzel
set -eu

have() { command -v "$1" >/dev/null 2>&1; }
have asusctl || { notify-send "power-menu" "asusctl not installed"; exit 1; }
have fuzzel  || { notify-send "power-menu" "fuzzel not installed";  exit 1; }

# Current profile (asusctl -p prints e.g. "Active profile is Balanced")
cur=$(asusctl profile get 2>/dev/null | grep -oE 'Quiet|Balanced|Performance' | tail -1 || true)

choice=$(asusctl profile list 2>/dev/null | grep -E '^(Quiet|Balanced|Performance)$' \
	| fuzzel --dmenu -p "Power [${cur:-?}]: ") || exit 0

[ -n "$choice" ] || exit 0
asusctl profile set "$choice"
notify-send "Power profile" "$choice"
