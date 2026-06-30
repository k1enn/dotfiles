#!/bin/sh
# dwl status bar feed. One line per tick -> dwl reads stdin into stext.
# Items: battery | network | volume | power-profile | refresh | date-time
# Each component prints nothing if its source/tool is absent, so the bar
# degrades gracefully on machines missing a given tool.
#
# Clickable blocks: each block is prefixed with a signal byte (\001..\006).
# dwl's buttonpress maps a click on the status area back to that byte and runs
# `dwl-status.sh click <signal> <button>` (button: 1=left, 3=right). See the
# `click` dispatch below for what each block does.
# ponytail: pure shell + sysfs; externals are pamixer/nmcli/asusctl/wlr-randr,
# each guarded by command -v so a missing one just drops its block.
# Icons are Nerd Font glyphs (deps: a Nerd Font as the dwl bar font).

PIDFILE="$HOME/.cache/dwl-status.pid"
PROFILE_CACHE="$HOME/.cache/dwl-profile"

# --- icons (Nerd Font: fa + md) ---
I_BAT_CHG=""; I_BAT_FULL=""; I_BAT_75=""
I_BAT_50="";  I_BAT_25="";  I_BAT_EMP=""
I_ETH="󰈀";  I_WIFI="";     I_NONET="󰤭"
I_VOL=""; I_VOL_MUTE=""
I_PROF=""; I_HZ="󰍹"
I_CLK="󰥔"

# emit the signal byte for a block (octal -> raw control byte)
sigbyte() { printf "\\$(printf '%03o' "$1")"; }

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

# ponytail: asusctl's "get current profile" output varies by version; grep the
# name out of `profile -p`, fall back to the last value we set (cache file).
pp_current() {
	c=$(asusctl profile -p 2>/dev/null | grep -oE 'Quiet|Balanced|Performance' | head -1)
	[ -n "$c" ] && { printf '%s' "$c"; return; }
	cat "$PROFILE_CACHE" 2>/dev/null || printf 'Balanced'
}

pp() {
	command -v asusctl >/dev/null 2>&1 || return
	printf '%s %s' "$I_PROF" "$(pp_current)"
}

# wlr-randr helpers (dwl is a wlroots compositor; xrandr would only see Xwayland)
wlr_out() { wlr-randr 2>/dev/null | awk 'NR==1{print $1; exit}'; }
wlr_res() { wlr-randr 2>/dev/null | awk '/current/{print $1; exit}'; }
wlr_hz()  { wlr-randr 2>/dev/null | awk '/current/{for(i=1;i<=NF;i++) if($i=="Hz"){print int($(i-1)); exit}}'; }

refresh() {
	command -v wlr-randr >/dev/null 2>&1 || return
	hz=$(wlr_hz)
	[ -n "$hz" ] && printf '%s %sHz' "$I_HZ" "$hz"
}

clk() {
	printf '%s %s' "$I_CLK" "$(date '+%d/%m/%y %H:%M')"
}

build() {
	out=
	# "func:signal" — signal byte lets clicks route back to the right block
	for spec in bat:1 net:2 vol:3 pp:4 refresh:5 clk:6; do
		f=${spec%%:*}; sig=${spec##*:}
		s=$($f)
		[ -n "$s" ] || continue
		out="${out}${out:+  }$(sigbyte "$sig")$s"
	done
	printf '%s\n' "$out"
}

# --- click actions (run via: dwl-status.sh click <signal> <button>) ---
net_click() {
	if command -v nm-connection-editor >/dev/null 2>&1; then
		nm-connection-editor &
	elif command -v nmtui >/dev/null 2>&1; then
		foot -e nmtui &
	fi
}

vol_click() {
	command -v pamixer >/dev/null 2>&1 || return
	case $1 in
		1) pamixer -t ;;                                            # left: mute toggle
		3) command -v pavucontrol >/dev/null 2>&1 && pavucontrol & ;; # right: mixer
	esac
}

pp_click() {
	command -v asusctl >/dev/null 2>&1 || return
	case $(pp_current) in
		Quiet)    next=Balanced ;;
		Balanced) next=Performance ;;
		*)        next=Quiet ;;
	esac
	asusctl profile set "$next" 2>/dev/null && printf '%s' "$next" > "$PROFILE_CACHE"
}

refresh_click() {
	command -v wlr-randr >/dev/null 2>&1 || return
	out=$(wlr_out); res=$(wlr_res)
	[ -n "$out" ] && [ -n "$res" ] || return
	# Full-precision rates: wlr-randr only matches the exact mode value, so
	# 60 != 60.061001 and `--mode res@60` silently no-ops. Keep the floats.
	cur=$(wlr-randr 2>/dev/null | awk '/current/{for(i=1;i<=NF;i++) if($i=="Hz"){print $(i-1); exit}}')
	rates=$(wlr-randr 2>/dev/null | awk -v r="$res" \
		'$1==r{for(i=1;i<=NF;i++) if($i=="Hz") print $(i-1)}')
	# next rate after the current one (listed order), wrapping to the first
	next=$(printf '%s\n' "$rates" | awk -v c="$cur" \
		'{a[NR]=$1} END{for(i=1;i<=NR;i++) if(a[i]==c){print (i<NR)?a[i+1]:a[1]; exit}}')
	[ -n "$next" ] || next=$(printf '%s\n' "$rates" | head -1)
	[ -n "$next" ] && wlr-randr --output "$out" --mode "${res}@${next}" 2>/dev/null
}

if [ "$1" = click ]; then
	case $2 in
		2) net_click ;;
		3) vol_click "$3" ;;
		4) pp_click ;;
		5) refresh_click ;;
		# 1 (battery) and 6 (clock) are display-only
	esac
	# nudge the running feed so the bar reflects the change immediately
	kill -USR1 "$(cat "$PIDFILE" 2>/dev/null)" 2>/dev/null
	exit 0
fi

[ "$1" = once ] && { build; exit 0; }

# Signal-driven refresh: USR1 forces an immediate rebuild so volume keypresses
# update the bar instantly instead of waiting out the 2s poll. `sleep & wait`
# lets the signal interrupt the sleep. Volume keybinds send USR1 to the pid we
# drop here. Fixed $HOME path (not XDG_RUNTIME_DIR) so the keybind's shell
# resolves the same pidfile regardless of its environment.
mkdir -p "$HOME/.cache"
echo $$ > "$PIDFILE"
# `:` noop just interrupts the wait; the loop then rebuilds once (no double build).
trap : USR1
while :; do build; sleep 2 & wait; done
