#!/bin/sh

# Define your preferred tuned profiles in cycle order
profiles="balanced powersave throughput-performance"

# Get current profile
current=$(tuned-adm active 2>/dev/null | awk '{print $4}')

# Find next profile in cycle
found_current=false
next_profile=""

for profile in $profiles; do
    if [ "$found_current" = "true" ]; then
        next_profile="$profile"
        break
    fi
    if [ "$profile" = "$current" ]; then
        found_current=true
    fi
done

# If no next profile found, cycle back to first
if [ -z "$next_profile" ]; then
    next_profile=$(echo $profiles | awk '{print $1}')
fi

# Switch to next profile
sudo tuned-adm profile "$next_profile"

# Optional: Send notification
if command -v notify-send >/dev/null 2>&1; then
    notify-send "Power Profile" "Switched to: $next_profile"
fi

