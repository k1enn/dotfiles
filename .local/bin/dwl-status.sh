#!/bin/sh
# dwl status bar feed. One line per tick -> dwl reads stdin into stext.
# Items: battery | network | volume | date-time
# Each component prints nothing if its source/tool is absent, so the bar
# degrades gracefully on machines missing a given tool.
# ponytail: pure shell + sysfs; only pamixer/nmcli are external.
# Icons are Nerd Font glyphs (deps: a Nerd Font as the dwl bar font).

# --- icons (Nerd Font: fa + md) ---
I_BAT_CHG=""; I_BAT_FULL=""; I_BAT_75=""
I_BAT_50="";  I_BAT_25="";  I_BAT_EMP=""
I_ETH="󰈀";  I_WIFI="";     I_NONET="󰤭"
I_VOL=""; I_VOL_MUTE=""
I_CLK="󰥔"

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

net() {
	command -v nmcli >/dev/null 2>&1 || return
	line=$(nmcli -t -f TYPE,STATE,CONNECTION device status 2>/dev/null \
		| awk -F: '$2=="connected" && ($1=="ethernet"||$1=="wifi"){print;exit}')
	if [ -z "$line" ]; then printf '%s' "$I_NONET"; return; fi
	type=${line%%:*}
	conn=${line##*:}
	if [ "$type" = ethernet ]; then printf '%s' "$I_ETH"; else printf '%s %s' "$I_WIFI" "$conn"; fi
}

vol() {
	command -v pamixer >/dev/null 2>&1 || return
	[ "$(pamixer --get-mute 2>/dev/null)" = true ] && { printf '%s' "$I_VOL_MUTE"; return; }
	printf '%s %s%%' "$I_VOL" "$(pamixer --get-volume 2>/dev/null)"
}

clk() {
	printf '%s %s' "$I_CLK" "$(date '+%d/%m/%y %H:%M')"
}

build() {
	out=
	for f in bat net vol clk; do
		s=$($f)
		[ -n "$s" ] && out="${out}${out:+  }$s"
	done
	printf '%s\n' "$out"
}

[ "$1" = once ] && { build; exit 0; }

# Signal-driven refresh: USR1 forces an immediate rebuild so volume keypresses
# update the bar instantly instead of waiting out the 2s poll. `sleep & wait`
# lets the signal interrupt the sleep. Volume keybinds send USR1 to the pid we
# drop here. Fixed $HOME path (not XDG_RUNTIME_DIR) so the keybind's shell
# resolves the same pidfile regardless of its environment.
PIDFILE="$HOME/.cache/dwl-status.pid"
mkdir -p "$HOME/.cache"
echo $$ > "$PIDFILE"
# `:` noop just interrupts the wait; the loop then rebuilds once (no double build).
trap : USR1
while :; do build; sleep 2 & wait; done
