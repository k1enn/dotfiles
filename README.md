<h1 align="center">
  .dotfiles
</h1>
<div align="center">
  <h3>Follow me: </h3>
</div>

<div align="center">
 <p>
    <img src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/github.png" alt="GitHub Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://github.com/k1enn" target="_blank">GitHub</a></strong>
    <img style="padding-left: 10px; " src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/codeforces.png" alt="Codeforces Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://codeforces.com/profile/dinhtrungkien" target="_blank">Codeforces</a></strong>
    <img style="padding-left: 10px;" src="https://github.com/k1enn/software-engineer-notes/blob/main/subjects/web-programming/Buoi1/Bai01/images/linkedin.png" alt="LinkedIn Logo" width="20" height="20" />
    <strong><a style="text-decoration:none;" href="https://www.linkedin.com/in/k1enn/" target="_blank">LinkedIn</a></strong>
  </p>
      <small>September, 2025</small>
</div>

Backup my dotfiles in case Fedora system explode.

## **System informations**

- **Linux distro**: Fedora Workstation 42.
- **WM**: dwl (suckless, Wayland)
- **Display server**: Wayland (wlroots)
- **Notifications**: dunst — plays `/usr/share/sounds/freedesktop/stereo/bell.oga` on every notification via `~/.config/dunst/play-sound.sh` (needs `paplay` from pipewire-pulse/pulseaudio).

---

## Installation
```
sudo dnf install stow 
git clone git@github.com:k1enn/dotfiles.git ~/.dotfiles 
cd ~/.dotfiles
stow .

```

## dwl keybindings

`MOD` = Super (Win) key. Config: `.local/src/dwl/config.def.h`.

### Apps & actions
| Key | Action |
|-----|--------|
| `MOD+Return` | Terminal (foot) |
| `MOD+Space` | App launcher (fuzzel) |
| `MOD+Shift+~` | Scratchpad terminal |
| `MOD+grave` | Toggle scratchpad tag (9) |
| `MOD+O` | Monitor menu |
| `MOD+Shift+V` | Clipboard history picker |
| `MOD+Shift+S` | Region screenshot → clipboard |
| `MOD+Shift+X` | Lock screen (swaylock) |

### Window & focus
| Key | Action |
|-----|--------|
| `MOD+J` / `MOD+K` | Focus next / previous |
| `MOD+H` / `MOD+L` | Shrink / grow master area |
| `MOD+I` / `MOD+P` | Increase / decrease master count |
| `MOD+Shift+Return` | Zoom (promote to master) |
| `MOD+Tab` | View last tag |
| `MOD+Q` | Kill window |
| `MOD+Shift+Space` | Toggle floating |
| `MOD+E` | Toggle fullscreen |

### Layout
| Key | Action |
|-----|--------|
| `MOD+T` / `MOD+F` / `MOD+M` | Tile / floating / monocle |
| `MOD+B` | Toggle bar |

### Tags
| Key | Action |
|-----|--------|
| `MOD+1..9` | View tag |
| `MOD+Ctrl+1..9` | Add / remove tag from view |
| `MOD+Shift+1..9` | Move window to tag |
| `MOD+Ctrl+Shift+1..9` | Toggle tag on window |
| `MOD+0` | View all tags |
| `MOD+Shift+)` | Window on all tags |

### Monitors
| Key | Action |
|-----|--------|
| `MOD+,` / `MOD+.` | Focus monitor left / right |
| `MOD+Shift+<` / `MOD+Shift+>` | Move window to monitor left / right |

### Media & brightness
Function keys (no modifier): volume ±5 / mute, mic mute, brightness ±5%.

### Session
| Key | Action |
|-----|--------|
| `MOD+Shift+Q` | Quit dwl |
| `Ctrl+Alt+Backspace` | Quit dwl |
| `Ctrl+Alt+F1..F12` | Switch virtual terminal |

### Mouse
- **Tag**: Left = view · Right = toggle · `MOD`+Left = move window here · `MOD`+Right = toggle tag
- **Layout symbol**: Left = tile · Right = monocle
- **Title**: Middle = zoom
- **Status**: Middle = open terminal
- **Window**: `MOD`+Left = move · `MOD`+Right = resize · `MOD`+Middle = toggle floating
