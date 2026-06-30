#!/bin/sh
# dwl status bar feed. One line per tick -> dwl reads stdin into stext.
# Items: fcitx5 layout | battery | power mode | network | brightness | volume | mic
# Each component prints nothing if its source/tool is absent, so the bar
# degrades gracefully on machines missing a given tool.
# ponytail: pure shell + sysfs; only pamixer/nmcli/fcitx5-remote are external.
# Icons are Nerd Font glyphs (deps: a Nerd Font as the dwl bar font).

# --- icons (Nerd Font: fa + md) ---
I_KBD=""
I_BAT_CHG=""; I_BAT_FULL=""; I_BAT_75=""
I_BAT_50="";  I_BAT_25="";  I_BAT_EMP=""
I_PERF="󰓅"; I_SAVE="󰾆"; I_BAL="󰾅"
I_ETH="󰈀";  I_WIFI="";     I_NONET="󰤭"
I_BRI=""
I_VOL=""; I_VOL_MUTE=""
I_MIC=""; I_MIC_MUTE=""

kbd() {
	command -v fcitx5-remote >/dev/null 2>&1 || return
	im=$(fcitx5-remote -n 2>/dev/null) || return
	[ -n "$im" ] && printf '%s %s' "$I_KBD" "$im"
}

bat() {
	for b in /sys/class/power_supply/BAT*; do
		[ -r "$b/capacity" ] || continue
		cap=$(cat "$b/capacity")
		st=$(cat "$b/status" 2>/dev/null)
		if [ "$st" = Charging ]; then g=$I_BAT_CHG
		elif [ "$cap" -ge 90 ]; then g=$I_BAT_FULL
		elif [ "$cap" -ge 65 ]; then g=$I_BAT_75
		elif [ "$cap" -ge 40 ]; then g=$I_BAT_50
		elif [ "$cap" -ge 15 ]; then g=$I_BAT_25
		else g=$I_BAT_EMP; fi
		printf '%s %d%%' "$g" "$cap"
		return
	done
}

pwr() {
	command -v powerprofilesctl >/dev/null 2>&1 || return
	case "$(powerprofilesctl get 2>/dev/null)" in
	performance) printf '%s perf' "$I_PERF" ;;
	power-saver) printf '%s save' "$I_SAVE" ;;
	balanced)    printf '%s bal'  "$I_BAL"  ;;
	esac
}

net() {
	command -v nmcli >/dev/null 2>&1 || return
	line=$(nmcli -t -f TYPE,STATE,CONNECTION device status 2>/dev/null 		| awk -F: '$2=="connected" && ($1=="ethernet"||$1=="wifi"){print;exit}')
	if [ -z "$line" ]; then printf '%s' "$I_NONET"; return; fi
	type=${line%%:*}
	conn=${line##*:}
	if [ "$type" = ethernet ]; then printf '%s' "$I_ETH"; else printf '%s %s' "$I_WIFI" "$conn"; fi
}

bri() {
	for d in /sys/class/backlight/*; do
		[ -r "$d/brightness" ] && [ -r "$d/max_brightness" ] || continue
		cur=$(cat "$d/brightness"); max=$(cat "$d/max_brightness")
		[ "$max" -gt 0 ] && printf '%s %d%%' "$I_BRI" $((cur * 100 / max))
		return
	done
}

vol() {
	command -v pamixer >/dev/null 2>&1 || return
	[ "$(pamixer --get-mute 2>/dev/null)" = true ] && { printf '%s' "$I_VOL_MUTE"; return; }
	printf '%s %s%%' "$I_VOL" "$(pamixer --get-volume 2>/dev/null)"
}

mic() {
	command -v pamixer >/dev/null 2>&1 || return
	[ "$(pamixer --default-source --get-mute 2>/dev/null)" = true ] && { printf '%s' "$I_MIC_MUTE"; return; }
	printf '%s %s%%' "$I_MIC" "$(pamixer --default-source --get-volume 2>/dev/null)"
}

build() {
	out=
	for f in kbd bat pwr net bri vol mic; do
		s=$($f)
		[ -n "$s" ] && out="${out}${out:+  }$s"
	done
	printf '%s
' "$out"
}

[ "$1" = once ] && { build; exit 0; }

# Signal-driven refresh: USR1 forces an immediate rebuild so volume/brightness
# keypresses update the bar instantly instead of waiting out the 2s poll.
# `sleep & wait` lets the signal interrupt the sleep (plain `sleep` would block
# it until the 2s elapse). Keybinds send USR1 to the pid we drop here.
echo $$ > "${XDG_RUNTIME_DIR:-/tmp}/dwl-status.pid"
trap build USR1
while :; do build; sleep 2 & wait; done
