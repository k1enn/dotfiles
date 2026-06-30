#!/bin/sh
# dwl status bar feed. One line per tick -> dwl reads stdin into stext.
# Items: fcitx5 layout | battery | power mode | network | brightness | volume | mic
# Each component prints nothing if its source/tool is absent, so the bar
# degrades gracefully on machines missing a given tool.
# ponytail: pure shell + sysfs; only pamixer/nmcli/fcitx5-remote are external.

kbd() {
	command -v fcitx5-remote >/dev/null 2>&1 || return
	im=$(fcitx5-remote -n 2>/dev/null) || return
	[ -n "$im" ] && printf ' %s' "$im"
}

bat() {
	for b in /sys/class/power_supply/BAT*; do
		[ -r "$b/capacity" ] || continue
		cap=$(cat "$b/capacity")
		st=$(cat "$b/status" 2>/dev/null)
		if [ "$st" = Charging ]; then g=
		elif [ "$cap" -ge 90 ]; then g=
		elif [ "$cap" -ge 70 ]; then g=
		elif [ "$cap" -ge 50 ]; then g=
		elif [ "$cap" -ge 30 ]; then g=
		elif [ "$cap" -ge 10 ]; then g=
		else g=; fi
		printf '%s %d%%' "$g" "$cap"
		return
	done
}

pwr() {
	command -v powerprofilesctl >/dev/null 2>&1 || return
	case "$(powerprofilesctl get 2>/dev/null)" in
	performance) printf '󰓅 perf' ;;
	power-saver) printf '󰾆 save' ;;
	balanced)    printf '󰾅 bal'  ;;
	esac
}

net() {
	command -v nmcli >/dev/null 2>&1 || return
	line=$(nmcli -t -f TYPE,STATE,CONNECTION device status 2>/dev/null \
		| awk -F: '$2=="connected" && ($1=="ethernet"||$1=="wifi"){print;exit}')
	if [ -z "$line" ]; then printf '󰤭'; return; fi
	type=${line%%:*}
	conn=${line##*:}
	if [ "$type" = ethernet ]; then printf '󰈀'; else printf ' %s' "$conn"; fi
}

bri() {
	for d in /sys/class/backlight/*; do
		[ -r "$d/brightness" ] && [ -r "$d/max_brightness" ] || continue
		cur=$(cat "$d/brightness"); max=$(cat "$d/max_brightness")
		[ "$max" -gt 0 ] && printf ' %d%%' $((cur * 100 / max))
		return
	done
}

vol() {
	command -v pamixer >/dev/null 2>&1 || return
	[ "$(pamixer --get-mute 2>/dev/null)" = true ] && { printf ''; return; }
	printf ' %s%%' "$(pamixer --get-volume 2>/dev/null)"
}

mic() {
	command -v pamixer >/dev/null 2>&1 || return
	[ "$(pamixer --default-source --get-mute 2>/dev/null)" = true ] && { printf ''; return; }
	printf ' %s%%' "$(pamixer --default-source --get-volume 2>/dev/null)"
}

build() {
	out=
	for f in kbd bat pwr net bri vol mic; do
		s=$($f)
		[ -n "$s" ] && out="${out}${out:+  }$s"
	done
	printf '%s\n' "$out"
}

[ "$1" = once ] && { build; exit 0; }
while :; do build; sleep 2; done
