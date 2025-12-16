#!/bin/bash
# /* ---- 💫 https://github.com/JaKooLit 💫 ---- */  ##
# Scripts for volume controls for audio and mic 

sDIR="$HOME/.config/hypr/scripts"

# Get Volume
get_volume() {
    volume=$(pamixer --get-volume)
    if [[ "$volume" -eq "0" ]]; then
        echo "Muted"
    else
        echo "$volume %"
    fi
}

# Notify
notify_user() {
    if [[ "$(get_volume)" == "Muted" ]]; then
        notify-send -e -h string:x-canonical-private-synchronous:volume_notif -h boolean:SWAYNC_BYPASS_DND:true -u low "Volume:" "Muted"
    else
        notify-send -e -h int:value:"$(get_volume | sed 's/%//')" -h string:x-canonical-private-synchronous:volume_notif -h boolean:SWAYNC_BYPASS_DND:true -u low "Volume Level:" "$(get_volume)" &&
        "$sDIR/Sounds.sh" --volume
    fi
}

# Increase Volume
inc_volume() {
    if [ "$(pamixer --get-mute)" == "true" ]; then
        toggle_mute
    else
        pamixer -i 5 --allow-boost --set-limit 150 && notify_user
    fi
}

# Decrease Volume
dec_volume() {
    if [ "$(pamixer --get-mute)" == "true" ]; then
        toggle_mute
    else
        pamixer -d 5 && notify_user
    fi
}

# Toggle Mute
toggle_mute() {
	if [ "$(pamixer --get-mute)" == "false" ]; then
		pamixer -m && notify-send -e -u low -h boolean:SWAYNC_BYPASS_DND:true "Mute"
	elif [ "$(pamixer --get-mute)" == "true" ]; then
		pamixer -u && notify-send -e -u low -h boolean:SWAYNC_BYPASS_DND:true "Volume:" "Switched ON"
	fi
}

# Toggle Mic
toggle_mic() {
	if [ "$(pamixer --default-source --get-mute)" == "false" ]; then
		pamixer --default-source -m && notify-send -e -u low -h boolean:SWAYNC_BYPASS_DND:true "Microphone:" "Switched OFF"
	elif [ "$(pamixer --default-source --get-mute)" == "true" ]; then
		pamixer -u --default-source u && notify-send -e -u low -h boolean:SWAYNC_BYPASS_DND:true "Microphone:" "Switched ON"
	fi
}

# Get Microphone Volume
get_mic_volume() {
    volume=$(pamixer --default-source --get-volume)
    if [[ "$volume" -eq "0" ]]; then
        echo "Muted"
    else
        echo "$volume %"
    fi
}

# Notify for Microphone
notify_mic_user() {
    volume=$(get_mic_volume)
    notify-send -e -h int:value:"$volume" -h "string:x-canonical-private-synchronous:volume_notif" -h boolean:SWAYNC_BYPASS_DND:true -u low "Mic Level:" "$volume"
}

# Increase MIC Volume
inc_mic_volume() {
    if [ "$(pamixer --default-source --get-mute)" == "true" ]; then
        toggle_mic
    else
        pamixer --default-source -i 5 && notify_mic_user
    fi
}

# Decrease MIC Volume
dec_mic_volume() {
    if [ "$(pamixer --default-source --get-mute)" == "true" ]; then
        toggle-mic
    else
        pamixer --default-source -d 5 && notify_mic_user
    fi
}

# Execute accordingly
if [[ "$1" == "--get" ]]; then
	get_volume
elif [[ "$1" == "--inc" ]]; then
	inc_volume
elif [[ "$1" == "--dec" ]]; then
	dec_volume
elif [[ "$1" == "--toggle" ]]; then
	toggle_mute
elif [[ "$1" == "--toggle-mic" ]]; then
	toggle_mic
elif [[ "$1" == "--get-icon" ]]; then
	get_icon
elif [[ "$1" == "--get-mic-icon" ]]; then
	get_mic_icon
elif [[ "$1" == "--mic-inc" ]]; then
	inc_mic_volume
elif [[ "$1" == "--mic-dec" ]]; then
	dec_mic_volume
else
	get_volume
fi
