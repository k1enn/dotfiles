#!/bin/bash
# /* ---- 💫 https://github.com/JaKooLit 💫 ---- */  ##
# Rofi menu for KooL Hyprland Quick Settings (SUPER SHIFT E)

# Modify this config file for default terminal and EDITOR
config_file="$HOME/.config/hypr/configs/_variables.conf"

tmp_config_file=$(mktemp)
sed 's/^\$//g; s/ = /=/g' "$config_file" >"$tmp_config_file"
source "$tmp_config_file"
# ##################################### #

# variables
configs="$HOME/.config/hypr/configs"
rofi_theme="$HOME/.config/rofi/config-edit.rasi"
scriptsDir="$HOME/.config/hypr/scripts"
UserScripts="$HOME/.config/hypr/UserScripts"

# Function to display the menu options without numbers
menu() {
  cat <<EOF
_variables
animations
appearance
env
input
keybinds
layouts
rules
settings
startup
Choose Kitty Terminal Theme
Configure Monitors (nwg-displays)
Configure Workspace Rules (nwg-displays)
GTK Settings (nwg-look)
QT Apps Settings (qt6ct)
QT Apps Settings (qt5ct)
Choose Hyprland Animations
Search for Keybinds
Toggle Game Mode
Switch Dark-Light Theme
EOF
}

# Main function to handle menu selection
main() {
  choice=$(menu | rofi -i -dmenu -config $rofi_theme)

  # Map choices to corresponding files
  case "$choice" in
  "_variables") file="$configs/_variables.conf" ;;
  "animations") file="$configs/animations.conf" ;;
  "appearance") file="$configs/appearance.conf" ;;
  "env") file="$configs/env.conf" ;;
  "input") file="$configs/input.conf" ;;
  "keybinds") file="$configs/keybinds.conf" ;;
  "layouts") file="$configs/layouts.conf" ;;
  "rules") file="$configs/rules.conf" ;;
  "settings") file="$configs/settings.conf" ;;
  "startup") file="$configs/startup.conf" ;;
  "Configure Monitors (nwg-displays)")
    if ! command -v nwg-displays &>/dev/null; then
      notify-send "E-R-R-O-R" "Install nwg-displays first"
      exit 1
    fi
    nwg-displays
    ;;
  "Configure Workspace Rules (nwg-displays)")
    if ! command -v nwg-displays &>/dev/null; then
      notify-send "E-R-R-O-R" "Install nwg-displays first"
      exit 1
    fi
    nwg-displays
    ;;
  "GTK Settings (nwg-look)")
    if ! command -v nwg-look &>/dev/null; then
      notify-send "E-R-R-O-R" "Install nwg-look first"
      exit 1
    fi
    nwg-look
    ;;
  "QT Apps Settings (qt6ct)")
    if ! command -v qt6ct &>/dev/null; then
      notify-send "E-R-R-O-R" "Install qt6ct first"
      exit 1
    fi
    qt6ct
    ;;
  "QT Apps Settings (qt5ct)")
    if ! command -v qt5ct &>/dev/null; then
      notify-send "E-R-R-O-R" "Install qt5ct first"
      exit 1
    fi
    qt5ct
    ;;
  "Choose Hyprland Animations") $scriptsDir/Animations.sh ;;
  "Search for Keybinds") $scriptsDir/KeyBinds.sh ;;
  "Toggle Game Mode") $scriptsDir/GameMode.sh ;;
  "Switch Dark-Light Theme") $scriptsDir/DarkLight.sh ;;
  *) return ;; # Do nothing for invalid choices
  esac

  # Open the selected file in the terminal with the text editor
  if [ -n "$file" ]; then
    $term -e $editor "$file"
  fi
}

# Check if rofi is already running
if pidof rofi >/dev/null; then
  pkill rofi
fi

main
