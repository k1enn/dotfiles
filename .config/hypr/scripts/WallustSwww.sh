#!/bin/bash
# /* ---- 💫 https://github.com/JaKooLit 💫 ---- */  ##
# Wallust Colors for current wallpaper

# Use passed argument if provided, otherwise fall back to swww cache
if [[ -n "$1" && -f "$1" ]]; then
    wallpaper_path="$1"
    ln_success=true
else
    # Define the path to the swww cache directory
    cache_dir="$HOME/.cache/swww/"

    # Initialize a flag to determine if the ln command was executed
    ln_success=false

    # Get current focused monitor
    current_monitor=$(hyprctl monitors | awk '/^Monitor/{name=$2} /focused: yes/{print name}')
    # Construct the full path to the cache file
    cache_file="$cache_dir$current_monitor"
    # Check if the cache file exists for the current monitor output
    if [ -f "$cache_file" ]; then
        # Get the wallpaper path from the cache file
        wallpaper_path=$(grep -v 'Lanczos3' "$cache_file" | head -n 1)
    fi
fi

if [[ -n "$wallpaper_path" && -f "$wallpaper_path" ]]; then
    # symlink the wallpaper to the location Rofi can access
    if ln -sf "$wallpaper_path" "$HOME/.config/rofi/.current_wallpaper"; then
        ln_success=true  # Set the flag to true upon successful execution
    fi
    # copy the wallpaper for wallpaper effects
	cp -r "$wallpaper_path" "$HOME/.config/hypr/wallpaper_effects/.wallpaper_current"
fi

# Check the flag before executing further commands
if [ "$ln_success" = true ]; then
    # execute wallust
	echo 'about to execute wallust'
    # execute wallust skipping tty and terminal changes
    wallust run "$wallpaper_path" -s

    # Update ASUS keyboard color from wallust colors
    wallust_colors="$HOME/.config/hypr/wallust/wallust-hyprland.conf"
    if [[ -f "$wallust_colors" ]] && command -v asusctl &>/dev/null; then
        # Extract color4 (usually a prominent accent color), remove 'rgb(' prefix and ')' suffix
        kbd_color=$(grep '^\$color4' "$wallust_colors" | sed 's/.*rgb(\([^)]*\)).*/\1/')
        if [[ -n "$kbd_color" ]]; then
            asusctl aura static -c "$kbd_color"
        fi
    fi
fi
