# ~/.dotfiles/fish/.config/fish/config.fish

# If not running interactively, don't do anything
if not status is-interactive
    return
end

# Auto-start dwl on tty1 login (no display manager). exec replaces the shell,
# so quitting dwl returns to the console instead of a half-dead login.
if status is-login; and test (tty) = /dev/tty1; and not set -q WAYLAND_DISPLAY; and not set -q DISPLAY
    exec ~/.local/bin/start-dwl.sh
end

# -----------------------------------------------------------------------------
# CUSTOM GREETING
# -----------------------------------------------------------------------------
# Change this string to whatever you want, or leave it empty "" to silence it completely
set -g fish_greeting "✨ Welcome back, k1en. Let's build something."

# -----------------------------------------------------------------------------
# ENVIRONMENT VARIABLES & PATHS
# -----------------------------------------------------------------------------
set -gx BROWSER 'firefox'
set -gx SUDO_PROMPT "Password for root access:"

fish_add_path ~/.local/bin
fish_add_path /home/k1en/.opencode/bin

# Bun
set -gx BUN_INSTALL "$HOME/.bun"
fish_add_path "$BUN_INSTALL/bin"

# PNPM
set -gx PNPM_HOME "/home/k1en/.local/share/pnpm"
fish_add_path "$PNPM_HOME"

# -----------------------------------------------------------------------------
# ALIASES
# -----------------------------------------------------------------------------
alias mirrors="sudo reflector --verbose --latest 5 --country 'Vietnam' --age 6 --sort rate --save /etc/pacman.d/mirrorlist"
alias grub-update="sudo grub-mkconfig -o /boot/grub/grub.cfg"
alias ls='lsd'
alias l='ls -l'
alias la='ls -a'
alias lla='ls -la'
alias lt='ls --tree'

# -----------------------------------------------------------------------------
# INITIALIZATIONS & TOOLS
# -----------------------------------------------------------------------------

# Set-up FZF key bindings
if type -q fzf
    fzf --fish | source
end

# Mise
if type -q mise
    mise activate fish | source
end

# Zoxide (Smart directory jumper)
if type -q zoxide
    zoxide init fish | source
end

# -----------------------------------------------------------------------------
# THE PROMPT
# -----------------------------------------------------------------------------
function fish_prompt
    set -l last_status $status
    set_color white --bold
    echo -n $USER " "
    echo -n (prompt_pwd)
    set_color normal
    
    printf '%s' (fish_git_prompt)
    
    set_color white --bold
    echo -n " "
    set_color normal
end

set -g __fish_git_prompt_show_informative_status 1
set -g __fish_git_prompt_showcolorhints 1
set -g __fish_git_prompt_char_stateseparator ' '