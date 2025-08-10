#!/bin/bash
# /* ---- 💫 https://github.com/JaKooLit 💫 ---- */  ##
# For disabling touchpad.
# Edit the Touchpad_Device on ~/.config/hypr/UserConfigs/Laptops.conf according to your system
# use hyprctl devices to get your system touchpad device name
# source https://github.com/hyprwm/Hyprland/discussions/4283?sort=new#discussioncomment-8648109
#!/bin/bash

notif="$HOME/.config/swaync/images/ja.png"
export STATUS_FILE="$XDG_RUNTIME_DIR/touchpad.status"

# Function to find the correct touchpad device name
find_touchpad_device() {
    # Try to find touchpad device from hyprctl devices output
    hyprctl devices | grep -i "touchpad" | head -1 | awk '{print $1}' | tr -d ':'
}

# Set your actual touchpad device name
# You can either set this manually or let the script auto-detect
TOUCHPAD_DEVICE="asup1205:00-093a:2008-touchpad"  # Your current setting
# Uncomment the line below to auto-detect:
# TOUCHPAD_DEVICE=$(find_touchpad_device)

enable_touchpad() {
    printf "true" > "$STATUS_FILE"
    notify-send -u low -i "$notif" "Enabling touchpad"
    if ! hyprctl keyword "device[$TOUCHPAD_DEVICE]:enabled" true; then
        notify-send -u critical "Error: Failed to enable touchpad"
        return 1
    fi
}

disable_touchpad() {
    printf "false" > "$STATUS_FILE"
    notify-send -u low -i "$notif" "Disabling touchpad"
    if ! hyprctl keyword "device[$TOUCHPAD_DEVICE]:enabled" false; then
        notify-send -u critical "Error: Failed to disable touchpad"
        return 1
    fi
}

# Check if hyprctl is available
if ! command -v hyprctl &> /dev/null; then
    notify-send -u critical "Error: hyprctl not found"
    exit 1
fi

# Main logic
if ! [ -f "$STATUS_FILE" ]; then
    enable_touchpad
else
    current_status=$(cat "$STATUS_FILE")
    if [ "$current_status" = "true" ]; then
        disable_touchpad
    elif [ "$current_status" = "false" ]; then
        enable_touchpad
    else
        # Reset to enabled state if status file is corrupted
        enable_touchpad
    fi
fi
