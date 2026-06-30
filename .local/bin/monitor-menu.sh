#!/bin/sh
# Interactive monitor manager for dwl / wlroots.
# Plug a monitor, hit MODKEY+o, pick a layout.
# deps: wlr-randr, fuzzel, wl-mirror (mirror only)
set -eu

have() { command -v "$1" >/dev/null 2>&1; }
have wlr-randr || { notify-send "monitor-menu" "wlr-randr not installed"; exit 1; }
have fuzzel   || { notify-send "monitor-menu" "fuzzel not installed"; exit 1; }

# Output names are the un-indented lines in `wlr-randr`
outputs=$(wlr-randr | grep -E '^[A-Za-z]' | awk '{print $1}')
internal=$(printf '%s\n' "$outputs" | grep -iE '^eDP|^LVDS' | head -1)
external=$(printf '%s\n' "$outputs" | grep -ivE '^eDP|^LVDS' | head -1)
[ -n "$internal" ] || internal=$(printf '%s\n' "$outputs" | head -1)

# Current pixel width of an output (from its "(current)" mode line)
width_of() {
	wlr-randr | awk -v o="$1" '
		$1==o {f=1; next}
		f && /current/ { split($1,a,"x"); print a[1]; exit }
		f && /^[A-Za-z]/ { exit }'
}

if [ -z "$external" ]; then
	choice=$(printf 'Internal Only' | fuzzel --dmenu -p 'Display (no external detected): ')
else
	choice=$(printf 'Extend Right\nExtend Left\nMirror\nExternal Only\nInternal Only' \
		| fuzzel --dmenu -p 'Display: ')
fi

case "$choice" in
"Extend Right")
	wlr-randr --output "$internal" --on --pos 0,0
	w=$(width_of "$internal"); w=${w:-1920}
	wlr-randr --output "$external" --on --pos "${w},0"
	;;
"Extend Left")
	wlr-randr --output "$external" --on --pos 0,0
	w=$(width_of "$external"); w=${w:-1920}
	wlr-randr --output "$internal" --on --pos "${w},0"
	;;
"External Only")
	wlr-randr --output "$external" --on --pos 0,0
	wlr-randr --output "$internal" --off
	;;
"Internal Only")
	wlr-randr --output "$internal" --on --pos 0,0
	[ -n "$external" ] && wlr-randr --output "$external" --off || true
	;;
"Mirror")
	# wlroots cannot mirror natively; wl-mirror renders one output into a window.
	# Both outputs stay on (stacked at 0,0); wl-mirror shows internal, fullscreen it on external.
	have wl-mirror || { notify-send "monitor-menu" "wl-mirror not installed (needed for mirror)"; exit 1; }
	wlr-randr --output "$internal" --on --pos 0,0
	wlr-randr --output "$external" --on --pos 0,0
	pkill -x wl-mirror 2>/dev/null || true
	wl-mirror -f "$internal" &   # floats (see config.h rule); move to external + MODKEY+e
	;;
*)
	exit 0
	;;
esac
