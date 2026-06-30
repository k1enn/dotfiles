---
title: dwl fixes — scratchpad, alwayscenter, float term, power/refresh menus, keyring
status: in-progress
created: 2026-06-30 10:54
note: P1-5 in-repo done (sandbox can't build/test); P6 root + build+test on user machine
mode: fast
effort: max
blockedBy: []
blocks: []
---

# dwl fixes

Approved design (brainstorm). 6 changes. dwl has **no shell IPC** → scratchpad
toggle stays C-side (tag trick). Editing `config.def.h`/`dwl.c` → `make` regens
`config.h` (`Makefile:49 config.h: config.def.h → cp`) → rebuild via existing flow.
Rationale: dwl issue #390 (no built-in scratchpad; alwayscenter patch recommended).

Keys confirmed free: `MOD+a`, `MOD+r`, `MOD+n`.

## Files
| File | Change | In repo? |
|---|---|---|
| `.local/src/dwl/config.def.h` | rules + cmds + keybinds | yes |
| `.local/src/dwl/dwl.c` | alwayscenter in `mapnotify` | yes |
| `.local/bin/dwl-autostart.sh` | spawn scratchpad (+ optional keyring daemon) | yes |
| `.local/bin/power-menu.sh` | NEW — asusctl + fuzzel | yes |
| `.local/bin/refresh-menu.sh` | NEW — wlr-randr + fuzzel | yes |
| `.local/src/dwl/README.md` | note: re-apply alwayscenter after dwl update | yes |
| `/etc/pam.d/login` | pam_gnome_keyring | **NO — root, user applies** |

---

## Phase 1 — alwayscenter patch (`dwl.c`)
Insert right after the `applyrules(c)` block in `mapnotify` (after **dwl.c:2036**,
before `drawbars();` at 2037). At this point `c->isfloating`, `c->mon`, and
`c->geom` (incl. border, set at 2020-2021) are all known.

```c
	} else {
		applyrules(c);
	}
	/* alwayscenter: center floating clients in their monitor work area (dwl#390) */
	if (c->isfloating && c->mon)
		resize(c, (struct wlr_box){
			.x = c->mon->w.x + (c->mon->w.width  - c->geom.width)  / 2,
			.y = c->mon->w.y + (c->mon->w.height - c->geom.height) / 2,
			.width = c->geom.width, .height = c->geom.height }, 0);
	drawbars();
```
- This is a local fork modification of upstream `dwl.c`. Add a one-line note in
  `README.md` (or `CHANGELOG.md`) so it's re-applied after any dwl upstream pull.
- Verify: `resize()` signature already used elsewhere (e.g. dwl.c:973) — `(Client*, struct wlr_box, int interact)`. `0` = non-interactive.

**Check:** floating window (scratchpad / floatterm) opens centered, not top-left.

## Phase 2 — scratchpad (autostart + tag toggle, `dwl-autostart.sh` + `config.def.h`)
No namedscratchpad patch. Existing rule `config.def.h:34`
`{ "scratchpad", NULL, 1<<8, 1, -1 }` stays (tag 9, floating).

1. `dwl-autostart.sh`: add near the other `&` spawns —
   ```sh
   # Scratchpad terminal: parked hidden on tag 9, toggle with MODKEY+grave
   pgrep -f 'foot.*app-id=scratchpad' >/dev/null || foot --app-id=scratchpad &
   ```
2. `config.def.h:143` keep `{ MODKEY, XKB_KEY_grave, toggleview, {.ui = 1 << 8} }`
   — now works on first press (window pre-spawned).
3. `config.def.h:144` repurpose `MOD+Shift+~` as respawn fallback (already spawns
   `scratchcmd`); update comment to say "respawn if killed".
4. Fix stale comment `config.def.h:33` → "spawned at autostart, hidden on tag 9,
   toggled with MODKEY+grave".

**Caveat (documented, not fixed):** single pad shares tag 9 — other tag-9 windows
toggle with it. Upgrade path: namedscratchpads patch.

**Check:** after login, `MOD+grave` shows centered scratchpad; again hides it.

## Phase 3 — floating centered terminal (`config.def.h`)
1. Rule: `{ "floatterm", NULL, 0, 1, -1 }` (current tags, floating).
2. Cmd: `static const char *floattermcmd[] = { "foot", "--app-id=floatterm", NULL };`
3. Keybind: `{ MODKEY, XKB_KEY_n, spawn, {.v = floattermcmd} },`

Centering handled by Phase 1.

**Check:** `MOD+n` opens a centered floating foot.

## Phase 4 — power-profile menu (`power-menu.sh` + `config.def.h`)
New `.local/bin/power-menu.sh` (mirror `monitor-menu.sh` skeleton: `set -eu`,
`have()` checks, `notify-send` on missing dep, exit 0 on empty pick):
```sh
#!/bin/sh
set -eu
have() { command -v "$1" >/dev/null 2>&1; }
have asusctl || { notify-send "power-menu" "asusctl not installed"; exit 1; }
have fuzzel  || { notify-send "power-menu" "fuzzel not installed";  exit 1; }
cur=$(asusctl profile -p 2>/dev/null | grep -oE 'Quiet|Balanced|Performance' | tail -1 || true)
choice=$(asusctl profile -l 2>/dev/null | grep -E '^(Quiet|Balanced|Performance)$' \
	| fuzzel --dmenu -p "Power [${cur:-?}]: ") || exit 0
[ -n "$choice" ] && asusctl profile -P "$choice" && notify-send "Power profile" "$choice"
```
- ⚠️ Verify exact asusctl flags on the machine — older/newer asusctl differ
  (`profile list` vs `profile -l`, `profile set X` vs `profile -P X`, `-p` for current).
  Diagnose: `asusctl profile -h`. Adjust the three asusctl calls to match.
- `chmod +x`. Cmd `powercmd[] = {"power-menu.sh", NULL}`; keybind `MOD+a`.

**Check:** `MOD+a` → fuzzel lists 3 profiles → pick → `asusctl profile -p` reflects it.

## Phase 5 — refresh-rate menu (`refresh-menu.sh` + `config.def.h`)
**wlr-randr, NOT xrandr** (xrandr only sees XWayland's virtual screen on Wayland).
New `.local/bin/refresh-menu.sh` — auto-detect internal output (reuse monitor-menu
detect), list its modes, set chosen:
```sh
#!/bin/sh
set -eu
have() { command -v "$1" >/dev/null 2>&1; }
have wlr-randr || { notify-send "refresh-menu" "wlr-randr not installed"; exit 1; }
have fuzzel    || { notify-send "refresh-menu" "fuzzel not installed";    exit 1; }
out=$(wlr-randr | grep -E '^[A-Za-z]' | awk '{print $1}' | grep -iE '^eDP|^LVDS' | head -1)
[ -n "$out" ] || out=$(wlr-randr | grep -E '^[A-Za-z]' | awk '{print $1}' | head -1)
# mode lines look like:  1536x864 px, 143.880 Hz  (current)
mode=$(wlr-randr | awk -v o="$out" '
		$1==o {f=1; next}
		f && /^[A-Za-z]/ {exit}
		f && /px,/ { gsub(/,/,""); printf "%s@%sHz\n", $1, $3 }' \
	| fuzzel --dmenu -p "Refresh ($out): ") || exit 0
[ -n "$mode" ] && wlr-randr --output "$out" --mode "$mode" \
	&& notify-send "Display" "$out -> $mode"
```
- ⚠️ Verify wlr-randr mode-line format on the machine (`wlr-randr` output) and the
  exact `--mode WxH@RHz` string it accepts. Adjust the awk parse / mode string to match.
- `chmod +x`. Cmd `refreshcmd[] = {"refresh-menu.sh", NULL}`; keybind `MOD+r`.

**Check:** `MOD+r` → fuzzel lists eDP-2 modes → pick → `wlr-randr` shows new current.

## Phase 6 — keyring PAM auto-unlock (`/etc/pam.d/login`, root)
Root cause: greeter-less fish-autostart of dwl on tty1 (commit `0b4fad82`) never
runs `pam_gnome_keyring`, so the login keyring stays locked → Thunar/gvfs first
secret-service call prompts.

**Not in dotfiles repo** — user applies as root. Provide exact steps:
1. Verify deps: `pacman -Q gnome-keyring gcr` (or distro equiv). Install if missing.
2. Edit `/etc/pam.d/login` (back it up first):
   - after the `auth` stack, add: `auth     optional  pam_gnome_keyring.so`
   - after the `session` stack, add: `session  optional  pam_gnome_keyring.so auto_start`
3. (Optional, if daemon not already up) ensure secrets component runs — pam
   `auto_start` handles this for the tty1 login session. If you also log in elsewhere,
   add to `dwl-autostart.sh`:
   `pgrep -x gnome-keyring-d >/dev/null || eval "$(gnome-keyring-daemon --start --components=secrets,ssh)"; export SSH_AUTH_SOCK`
4. **One-time:** if the existing login keyring password ≠ login password, delete
   `~/.local/share/keyrings/login.keyring` (loses stored secrets) OR change its
   password to match the login password via Seahorse, so PAM can unlock it.

**Check:** reboot → log in on tty1 → open Thunar → **no** unlock prompt;
`pgrep -af gnome-keyring` shows daemon, `echo $SSH_AUTH_SOCK` set.

---

## Build & verify (after Phases 1-5)
```sh
cd ~/.local/src/dwl && make        # regens config.h from config.def.h, compiles
# install per existing flow, then re-login to dwl / restart compositor
```
- Smoke: scratchpad toggle, MOD+n float term (both centered), MOD+a power, MOD+r refresh.
- Phase 6 verified separately after reboot.

## Skipped (YAGNI — add when needed)
- namedscratchpads patch (multi/named pads) — current single-pad tag trick suffices.
- per-rule geometry in Rule struct — alwayscenter covers the centering need.
- multi-monitor scratchpad discipline (#390 caveat) — single output now.
- blank-keyring fallback — PAM unlock chosen (stays encrypted).
