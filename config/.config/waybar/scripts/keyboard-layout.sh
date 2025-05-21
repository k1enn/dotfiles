#!/bin/bash

layout=$(fcitx5-remote -n)

case "$layout" in
    "keyboard-us")
        echo "US"
        ;;
    "unikey")
        echo "VN"
        ;;
    *)
        echo "US"
        ;;
esac 
