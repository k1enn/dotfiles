# **dotfiles**
Backup my dotfiles in case Fedora system expoded.

## **System informations**
- **Linux distro**: Fedora Workstation 42.
- **DE**: GNOME 48.
- **DRM**: Wayland.
- **Kernel**: ASUS ROG.
## **How to install**
### **Install Prerequisites**

**Ubuntu / Debian**
```
bash
sudo apt update
sudo apt install git stow
```

**Arch Linux**
```
bash
sudo pacman -S git stow
```

**Fedora**
```
bash
sudo dnf install git stow
```

### **Installation**

1. **Clone the repository**

   ```bash
   git clone https://github.com/k1enn/dotfiles.git ~/.dotfiles
   cd ~/.dotfiles
   ```

2. **Run the setup script**
⚠️**WARNING**
Running this script will override your current dotfiles to create a symlink. See files structure below to know what you should backup.

   ```bash
   ./install.sh
   ```

   This will use GNU Stow to symlink configuration files from each package (e.g. `zsh`, `p10k`, `ideavim`, `git`) into your home directory.

   > Alternatively, you can stow individual packages manually:
   >
   > ```bash
   > stow zsh
   > stow p10k
   > stow ideavim
   > stow git
   > ```

---

## **Directory Structure**

Your `~/.dotfiles` directory should look like this:

```
~/.dotfiles/
├── zsh/
│   └── .zshrc
├── p10k/
│   └── .p10k.zsh
├── ideavim/
│   └── .ideavimrc
├── git/
│   └── .gitconfig
├── config/
│   └──.config/
│       ├── nvim/
│       │   └── doc/
│       │   └── init.lua
│       │   └── LICENSE.md
│       │   └── lua/
│       │   └── README.md
├── tmux/
│   └── plugins/
├── install.sh
└── README.md
```

Each folder contains the config files for that tool. Stow will symlink them into your `$HOME` directory.
