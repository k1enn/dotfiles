#!/bin/bash
# Auto-fullscreen: Makes a window fullscreen when it's the only one in a workspace

handle() {
    case $1 in
        openwindow*|closewindow*|movewindow*)
            # Small delay to let Hyprland finish the operation
            sleep 0.1

            # Get active workspace
            workspace=$(hyprctl activeworkspace -j | jq '.id')

            # Count windows in current workspace (exclude special workspaces)
            count=$(hyprctl clients -j | jq "[.[] | select(.workspace.id == $workspace and .workspace.id > 0)] | length")

            # Get current fullscreen state
            is_fullscreen=$(hyprctl activewindow -j | jq '.fullscreen')

            if [ "$count" -eq 1 ] && [ "$is_fullscreen" != "1" ]; then
                hyprctl dispatch fullscreen 1
            elif [ "$count" -gt 1 ] && [ "$is_fullscreen" == "1" ]; then
                hyprctl dispatch fullscreen 0
            fi
            ;;
    esac
}

# Listen to Hyprland socket events
socat -U - UNIX-CONNECT:"$XDG_RUNTIME_DIR/hypr/$HYPRLAND_INSTANCE_SIGNATURE/.socket2.sock" | while read -r line; do
    handle "$line"
done
