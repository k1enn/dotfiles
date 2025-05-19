#!/bin/bash

# WARNING:
# This script is made by Cursor. I don't know if it works or not. You can stow each directory manually.
# I'm not responsible for any damage it may cause.

# Warning about overriding dotfiles
read -p "Warning: This script will override existing dotfiles in your home directory. Do you want to continue? (yes/no) " answer
if [[ "$answer" != "yes" ]]; then
  echo "Aborted by user."
  exit 0
fi

# Exit on error
set -e

# Function for error handling
error_exit() {
    echo "ERROR: $1" >&2
    exit 1
}

# Function to log progress
log() {
    echo "INFO: $1"
}

# Function to backup a file or directory
backup_item() {
    local src="$1"
    local backup_dir="$2"
    local relative_path="${src#$HOME/}"
    local backup_path="$backup_dir/$relative_path"
    
    if [ -e "$src" ]; then
        # Create the parent directory if it doesn't exist
        mkdir -p "$(dirname "$backup_path")"
        log "Backing up $relative_path"
        cp -r "$src" "$backup_path"
    fi
}

# Get the dotfiles repository path (where this script is located)
DOTFILES_DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -z "$DOTFILES_DIR" ]; then
    error_exit "Failed to determine script directory"
fi

log "Dotfiles directory: $DOTFILES_DIR"

# Ask for confirmation
read -p "Warning: This script will install dotfiles to your home directory. Continue? (y/yes) " answer
case "${answer,,}" in
    y|yes) ;;
    *) 
        log "Operation canceled by user"
        exit 0
        ;;
esac

# Create backup directory with timestamp
BACKUP_DIR="$HOME/dotfiles_backup_$(date +%Y%m%d%H%M%S)"
mkdir -p "$BACKUP_DIR" || error_exit "Failed to create backup directory $BACKUP_DIR"
log "Created backup directory: $BACKUP_DIR"

# List of home dotfiles to backup
HOME_DOTFILES=(.zshrc .p10k.zsh .ideavimrc .gitconfig .tmux.conf .zshenv)
for file in "${HOME_DOTFILES[@]}"; do
    backup_item "$HOME/$file" "$BACKUP_DIR"
done

# Config directories to backup
CONFIG_DIRS=(
  nvim sway waybar fcitx5 wlogout mako kitty 
  rofi environment.d backgrounds ghostty alias tmux swaylock
)

# Backup .config directories
for dir in "${CONFIG_DIRS[@]}"; do
    backup_item "$HOME/.config/$dir" "$BACKUP_DIR/.config"
done

# Backup electron-flags.conf if it exists
backup_item "$HOME/.config/electron-flags.conf" "$BACKUP_DIR/.config"

# Check for GNU stow and install if missing
if ! command -v stow &> /dev/null; then
    log "GNU stow not found, attempting to install..."
    if command -v brew &> /dev/null; then
        brew install stow || error_exit "Failed to install stow with brew"
    elif command -v dnf &> /dev/null; then
        sudo dnf install -y stow || error_exit "Failed to install stow with dnf"
    elif command -v apt &> /dev/null; then
        sudo apt install -y stow || error_exit "Failed to install stow with apt"
    elif command -v pacman &> /dev/null; then
        sudo pacman -S --noconfirm stow || error_exit "Failed to install stow with pacman"
    else
        error_exit "Please install GNU stow manually and try again"
    fi
fi

log "Installing dotfiles..."

# Navigate to the dotfiles directory
cd "$DOTFILES_DIR" || error_exit "Failed to change directory to $DOTFILES_DIR"

# Stow each configuration directory only if it exists
STOW_DIRS=(zsh ideavim p10k git config tmux zshenv cursor)

for dir in "${STOW_DIRS[@]}"; do
    if [ -d "$dir" ]; then
        log "Stowing $dir..."
        stow --restow --target="$HOME" "$dir" || error_exit "Failed to stow $dir"
    else
        log "Skipping $dir (directory not found)"
    fi
done

log "Dotfiles installation complete! 🎉"
log "Your previous configuration files have been backed up to: $BACKUP_DIR"