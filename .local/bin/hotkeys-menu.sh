#!/bin/sh
# Hotkey cheatsheet for dwl. MODKEY+/ -> list keys[] from config.h, pick one to run it.
# Reads config.h every time, so it always matches the source (rebuild dwl after edits).
# Run = write "<modmask> <keysym>" to the fifo dwl reads (see keyfifoin in dwl.c).
# deps: fuzzel
set -eu

cfg="$HOME/.dotfiles/.local/src/dwl/config.h"
fifo="${XDG_RUNTIME_DIR:-/run/user/$(id -u)}/dwl-keys.fifo"

# Output: "<mask>\t<keysym>\t<label>" per bind. Masks are wlroots WLR_MODIFIER_* values.
# shortcut: MODKEY assumed = LOGO and TAGKEYS mods hardcoded, update if those defines change
list=$(awk '
function mods(s,   n, i, p, m) {
	M = 0; L = ""
	n = split(s, p, "|")
	for (i = 1; i <= n; i++) {
		if (p[i] == "MODKEY" || p[i] == "WLR_MODIFIER_LOGO") { M += 64; L = L "Super+" }
		else if (p[i] == "WLR_MODIFIER_CTRL")  { M += 4; L = L "Ctrl+" }
		else if (p[i] == "WLR_MODIFIER_SHIFT") { M += 1; L = L "Shift+" }
		else if (p[i] == "WLR_MODIFIER_ALT")   { M += 8; L = L "Alt+" }
	}
}
function out(m, sym, label, act) { printf "%d\t%s\t%-28s %s\n", m, sym, label, act }
/^static const Key keys/ { on = 1; next }
on && /^};/ { exit }
!on { next }
/^[ \t]*TAGKEYS\(/ {
	s = $0; gsub(/[ \t]|XKB_KEY_/, "", s); sub(/^TAGKEYS\(/, "", s); sub(/\).*/, "", s)
	split(s, a, ","); t = a[3] + 1
	out(64, a[1], "Super+" a[1],            "view tag " t)
	out(68, a[1], "Super+Ctrl+" a[1],       "toggleview tag " t)
	out(65, a[2], "Super+Shift+" a[1],      "tag " t)
	out(69, a[2], "Super+Ctrl+Shift+" a[1], "toggletag " t)
	next
}
/^[ \t]*\{/ {
	s = $0; sub(/^[ \t]*\{[ \t]*/, "", s); sub(/[ \t]*\},?[ \t]*$/, "", s)
	n = split(s, f, ",")
	gsub(/[ \t]/, "", f[1]); gsub(/[ \t]|XKB_KEY_/, "", f[2]); gsub(/[ \t]/, "", f[3])
	arg = ""; for (i = 4; i <= n; i++) arg = arg (i > 4 ? "," : "") f[i]
	gsub(/^[ \t{]+|[ \t}]+$|\.[a-z]+[ \t]*=[ \t]*|SHCMD\(|"RF|\)$/, "", arg); gsub(/"/, "", arg)
	if (arg == "0") arg = ""
	mods(f[1])
	out(M, f[2], L f[2], f[3] (arg == "" ? "" : "  " arg))
}
' "$cfg")

i=$(printf '%s\n' "$list" | cut -f3 | fuzzel --dmenu --index -p "Keys: " -w 70) || exit 0
[ -p "$fifo" ] || { notify-send "hotkeys-menu" "dwl key fifo missing, rebuild dwl"; exit 1; }
printf '%s\n' "$list" | sed -n "$((i + 1))p" | cut -f1,2 | tr '\t' ' ' > "$fifo"
