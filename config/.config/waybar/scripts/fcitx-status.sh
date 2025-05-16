#!/bin/bash

# Get current Fcitx input method
current_im=$(fcitx5-remote -n 2>/dev/null)

# Map to friendly names
case $current_im in
    "keyboard-us") echo "EN" ;;
    "unikey") echo "VN" ;;
    *) echo "?" ;;
esac

