#!/usr/bin/env bash
# Toggle touchpad on/off via sysfs inhibited attribute
# For ASUS TUF A15 2023 (ASUP1205:00 093A:2008 Touchpad)
#
# Requires write access to /sys/class/input/eventXX/device/inhibited
# Option 1: Create udev rule (recommended, one-time setup):
#   sudo tee /etc/udev/rules.d/99-touchpad-toggle.rules <<'EOF'
#   ACTION=="add|change", KERNEL=="event*", ATTRS{name}=="*[Tt]ouchpad*", RUN+="/bin/chmod 0666 /sys%p/device/inhibited"
#   EOF
#   sudo udevadm control --reload-rules && sudo udevadm trigger
#
# Option 2: The script will fall back to sudo if the file is not writable.

set -euo pipefail

# --- Find the touchpad event device dynamically ---
TOUCHPAD_EVENT=""
while IFS= read -r line; do
    if [[ "$line" =~ ^N:\ Name=\".*[Tt]ouchpad.*\" ]]; then
        found_touchpad=1
    elif [[ "${found_touchpad:-}" == "1" && "$line" =~ ^H:\ Handlers=.*event([0-9]+) ]]; then
        TOUCHPAD_EVENT="event${BASH_REMATCH[1]}"
        break
    elif [[ "${found_touchpad:-}" == "1" && "$line" == "" ]]; then
        found_touchpad=0
    fi
done < /proc/bus/input/devices

if [[ -z "$TOUCHPAD_EVENT" ]]; then
    notify-send -u critical "Touchpad Toggle" "Could not find touchpad device"
    exit 1
fi

INHIBITED_PATH="/sys/class/input/${TOUCHPAD_EVENT}/device/inhibited"

if [[ ! -f "$INHIBITED_PATH" ]]; then
    notify-send -u critical "Touchpad Toggle" "Sysfs inhibited file not found: ${INHIBITED_PATH}"
    exit 1
fi

# --- Read current state and toggle ---
current_state=$(cat "$INHIBITED_PATH")

if [[ "$current_state" == "0" ]]; then
    new_state=1
    status="OFF"
    icon="input-touchpad-symbolic"
else
    new_state=0
    status="ON"
    icon="input-touchpad-symbolic"
fi

# --- Write new state ---
if [[ -w "$INHIBITED_PATH" ]]; then
    echo "$new_state" > "$INHIBITED_PATH"
else
    echo "$new_state" | sudo tee "$INHIBITED_PATH" > /dev/null
fi

notify-send -i "$icon" -t 2000 "Touchpad ${status}" "Touchpad has been turned ${status}"
