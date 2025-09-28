# If you come from bash you might have to change your $PATH.
# export PATH=$HOME/bin:/usr/local/bin:$PATH

export ZSH="$HOME/.oh-my-zsh"

ZSH_THEME="simple"

plugins=(
    git
    dnf
    zsh-autosuggestions
    zsh-syntax-highlighting
)

source $ZSH/oh-my-zsh.sh

# check the dnf plugins commands here
# https://github.com/ohmyzsh/ohmyzsh/tree/master/plugins/dnf


# Display Pokemon-colorscripts
# Project page: https://gitlab.com/phoneybadger/pokemon-colorscripts#on-other-distros-and-macos
#pokemon-colorscripts --no-title -s -r #without fastfetch
#pokemon-colorscripts --no-title -s -r | fastfetch -c $HOME/.config/fastfetch/config-pokemon.jsonc --logo-type file-raw --logo-height 10 --logo-width 5 --logo -

# fastfetch. Will be disabled if above colorscript was chosen to install
#fastfetch -c $HOME/.config/fastfetch/config-compact.jsonc

# Set-up FZF key bindings (CTRL R for fuzzy history finder)
source <(fzf --zsh)

HISTFILE=~/.zsh_history
HISTSIZE=10000
SAVEHIST=10000
setopt appendhistory

# Set-up icons for files/directories in terminal using lsd
alias ls='lsd'
alias l='ls -l'
alias la='ls -a'
alias lla='ls -la'
alias lt='ls --tree'

export PATH=$PATH:/home/k1en/.spicetify
export PATH="$HOME/.local/bin:$PATH"
export PATH="$HOME/.npm-global/bin:$PATH"
export PATH="$HOME/.local/bin:$PATH"
export PATH="$HOME/.local/bin:$PATH"
export SSH_ASKPASS=""


# DOCKER
alias start-sql="$HOME/docker/docker-scripts/start-sql.sh"
alias stop-sql="$HOME/docker/docker-scripts/stop-sql.sh"
alias status-sql="$HOME/docker/docker-scripts/status-sql.sh"

# DWM
alias cdwm="nvim ~/dwm/config.def.h; cd ~"
alias cdwmblock="nvim ~/.config/dwmblocks/blocks.def.h; cd ~"
alias mdwm="cd ~/dwm; rm config.h; sudo make install; cd -"
alias mdwmblock="cd ~/.config/dwmblocks; rm blocks.h; sudo make install; cd -"

# Power profile
alias gp="tuned-adm active"
alias ps="sudo tuned-adm profile powersave; tuned-adm active"
alias pb="sudo tuned-adm profile balanced; tuned-adm active"
alias pf="sudo tuned-adm profile throughput-performance; tuned-adm active"
