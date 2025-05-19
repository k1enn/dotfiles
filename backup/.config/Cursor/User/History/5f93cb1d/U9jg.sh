#!/bin/bash

# Create icons directory if it doesn't exist
mkdir -p /home/k1en/dotfiles/config/.config/wlogout/icons

# Download default icons from GitHub
icons=("hibernate" "lock" "logout" "reboot" "shutdown" "suspend")

for icon in "${icons[@]}"; do
  curl -s "https://raw.githubusercontent.com/ArtsyMacaw/wlogout/master/icons/${icon}.png" -o "/home/k1en/dotfiles/config/.config/wlogout/icons/${icon}.png"
  echo "Downloaded ${icon}.png"
done

echo "All icons downloaded to /home/k1en/dotfiles/config/.config/wlogout/icons/" 