#!/bin/bash

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
