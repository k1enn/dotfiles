#!/bin/bash

# Warning about overriding dotfiles
read -p "Warning: This script will override existing dotfiles in your home directory. Do you want to continue? (yes/no) " answer
if [[ "$answer" != "yes" ]]; then
  echo "Aborted by user."
  exit 0
fi

# Backup existing dotfiles
backup_dir="$HOME/dotfiles_backup_$(date +%Y%m%d%H%M%S)"
mkdir -p "$backup_dir"

# List of dotfiles to backup
files=(.zshrc .p10k.zsh .ideavimrc .gitconfig)

for file in "${files[@]}"; do
  if [ -e "$HOME/$file" ]; then
    echo "Backing up $file to $backup_dir"
    mv "$HOME/$file" "$backup_dir/"
  fi
done

# Handle .config subdirectories
config_dirs=(nvim tmux)
for dir in "${config_dirs[@]}"; do
  if [ -d "$HOME/.config/$dir" ]; then
    echo "Backing up .config/$dir to $backup_dir/.config/"
    mkdir -p "$backup_dir/.config"
    mv "$HOME/.config/$dir" "$backup_dir/.config/"
  fi
done

# Install stow if not present
if ! command -v stow &> /dev/null; then
  if command -v brew &> /dev/null; then
    brew install stow
  elif command -v dnf &> /dev/null; then
    sudo dnf install -y stow
  elif command -v apt &> /dev/null; then
    sudo apt install -y stow
  elif command -v pacman &> /dev/null; then
    sudo pacman -S --noconfirm stow
  else
    echo "Please install GNU Stow manually."
    exit 1
  fi
fi

cd "$(dirname "$0")"

stow zsh
stow ideavim
stow p10k
stow git
stow config
