#!/bin/bash

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo "ImageMagick is required but not installed. Please install it first."
    echo "sudo dnf install -y ImageMagick"
    exit 1
fi

ICONS_DIR="/home/k1en/dotfiles/config/.config/wlogout/icons"
TEMP_DIR="/tmp/wlogout_icons"

# Create temporary directory
mkdir -p "$TEMP_DIR"

# One Dark Pro colors
BLUE="#528bff"
RED="#e06c75"
GREEN="#98c379"
YELLOW="#e5c07b"
CYAN="#56b6c2"
MAGENTA="#c678dd"

# Create colored versions of each icon
echo "Creating One Dark Pro themed icons..."

# Lock - Blue
convert "$ICONS_DIR/lock.png" -fill "$BLUE" -colorize 100 "$ICONS_DIR/lock.png"
echo "✓ Created lock icon (blue)"

# Logout - Cyan
convert "$ICONS_DIR/logout.png" -fill "$CYAN" -colorize 100 "$ICONS_DIR/logout.png"
echo "✓ Created logout icon (cyan)"

# Suspend - Yellow
convert "$ICONS_DIR/suspend.png" -fill "$YELLOW" -colorize 100 "$ICONS_DIR/suspend.png"
echo "✓ Created suspend icon (yellow)"

# Hibernate - Magenta
convert "$ICONS_DIR/hibernate.png" -fill "$MAGENTA" -colorize 100 "$ICONS_DIR/hibernate.png"
echo "✓ Created hibernate icon (magenta)"

# Shutdown - Red
convert "$ICONS_DIR/shutdown.png" -fill "$RED" -colorize 100 "$ICONS_DIR/shutdown.png"
echo "✓ Created shutdown icon (red)"

# Reboot - Green
convert "$ICONS_DIR/reboot.png" -fill "$GREEN" -colorize 100 "$ICONS_DIR/reboot.png"
echo "✓ Created reboot icon (green)"

echo "Done! All icons have been themed with One Dark Pro colors." 