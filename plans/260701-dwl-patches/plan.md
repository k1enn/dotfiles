---
title: Apply dwl patches — pertag + gaps + per-app-cast
status: pending
created: 2026-07-01
plan_dir: plans/260701-dwl-patches
tree: ~/.local/src/dwl  (dwl 0.8-dev, branch "dwl", vendored in dotfiles)
blockedBy: []
blocks: []
---

# Apply dwl patches: pertag + gaps + per-app-cast

## Context
Add per-tag layouts (pertag), small window padding (gaps), and per-window
screencast (per-app-cast) to the vendored dwl at `~/.local/src/dwl`
(dwl 0.8-dev, branch `dwl`, built vs **wlroots-0.19**, no `config.h` → uses
`config.def.h`; `bar.patch` already applied).

Two of the three are safe on the current tree. per-app-cast needs
**wlroots-0.20** (already installed: CachyOS `wlroots0.20` 0.20.1), and because
dwl here is vendored (not an upstream clone with a `wlroots-next` branch), it is
a **manual 0.19→0.20 port**, not a branch switch. It is therefore isolated in a
git worktree so a broken build never touches the live compositor.

Patches (from codeberg.org/dwl/dwl-patches), currently in session scratchpad —
**Step 0 persists them into the repo**:
`pertag.patch`, `gaps.patch`, `per-app-share-wlroots-next-d41ecb745c.patch`.

**Build constraint:** the agent shell cannot see the Arch system libs
(`pkg-config`/wlroots headers absent here). All `make`/run steps run on the
user's machine — invoke via `! <cmd>` in the session, or the user runs them.

---

## Phase 0 — Persist patches (both phases need them)
Copy the three patches from scratchpad into the tree so they survive the session:
```
cp <scratchpad>/patches/{pertag.patch,gaps.patch,per-app-share-wlroots-next-d41ecb745c.patch} \
   ~/.local/src/dwl/patches/
```
(`~/.local/src/dwl/patches/` already exists — holds `bar.patch`.)

**Verify:** `ls ~/.local/src/dwl/patches/` shows all 4 patches.

---

## Phase A — pertag + gaps (wlroots-0.19, ship first, low risk)

### A1. Apply pertag + gaps
```
cd ~/.local/src/dwl
git apply patches/pertag.patch
git apply patches/gaps.patch
```
Both touch `struct Monitor` (~L199) and `createmon()` with **additive inserts**
(pertag adds `Pertag *pertag;`, gaps adds `int gaps;`), and gaps rewrites
`tile()` — no same-line edits, so they stack.
**If the 2nd apply reports an offset failure:** `git apply --3way patches/<x>.patch`
or `patch -p1 --fuzz=3 < patches/<x>.patch`.

**Verify:** `git diff --stat` shows changes to `config.def.h` + `dwl.c` only;
no `.rej` files.

### A2. Tune knobs in `config.def.h`
gaps.patch adds (near top, `/* appearance */`):
```c
static const int smartgaps      = 0;   /* 1 = no outer gap when only one window */
static int       gaps           = 1;   /* gaps on by default */
static const unsigned int gappx = 10;  /* gap size in px */
```
- Set `gappx` to **6** (user wants *small* padding; 10 is upstream default).
- Optionally `smartgaps = 1` if you want zero gap when a tag has a single window.
- gaps.patch also binds **MOD+g → togglegaps**. Confirm no existing `XKB_KEY_g`
  binding in your `config.def.h` keys[] (grep `XKB_KEY_g`); rebind if it clashes.

pertag needs **no config edits** — it's transparent (per-tag layout/mfact/nmaster
kick in automatically). It relies on `TAGCOUNT`; the patch defines it. A build
error about `TAGCOUNT` here would mean the define didn't land — recheck the apply.

**Verify:** `grep -nE 'gappx|smartgaps|TAGCOUNT|Pertag' config.def.h dwl.c`
shows the expected symbols.

### A3. Build (user machine)
```
! cd ~/.local/src/dwl && make clean && make
```
Makefile copies `config.def.h` → `config.h` if absent, then compiles vs wlroots-0.19.
**Verify:** exit 0, `./dwl` binary produced, no warnings about the new symbols.

### A4. Test live
- Restart dwl (or run nested if supported).
- pertag: switch tags, set a different layout/mfact/nmaster on tag 2 vs tag 1,
  toggle between them → each tag keeps its own layout + master ratio + count.
- gaps: tiled windows show ~6px padding from edges and each other; **MOD+g**
  toggles gaps off/on.

### A5. Commit
```
cd ~/.local/dotfiles-or-repo-root
git add -A && git commit
```
Message (Conventional Commits):
`feat(dwl): add pertag (per-tag layouts) and gaps (window padding)`
Commit trailer per repo convention.

**Phase A is independently shippable.** If Phase B fails, this survives.

---

## Phase B — per-app-cast (wlroots-0.20 migration, isolated worktree)

> Do NOT start until Phase A is committed. All build steps run on the user machine.

### B1. Create isolated worktree from Phase A commit
```
cd <dotfiles repo root>
git worktree add ../dwl-020 dwl          # branch off current dwl (post-Phase-A)
```
Work happens in `../dwl-020/.local/src/dwl`. Live dwl at `~/.local/src/dwl`
stays untouched until B7.

### B2. Point build at wlroots-0.20
Edit `config.mk` in the worktree:
```
WLR_INCS = `$(PKG_CONFIG) --cflags wlroots-0.20`
WLR_LIBS = `$(PKG_CONFIG) --libs   wlroots-0.20`
```
Confirm the pkgconfig name first (CachyOS co-installable pkg):
```
! pkg-config --modversion wlroots-0.20
```
If that name differs (e.g. a versioned `.pc` path), set `PKG_CONFIG_PATH` or the
exact `.pc` accordingly. Also verify protocol deps still resolve
(`wayland-protocols`, `wlr-protocols`).

### B3. Fix 0.19→0.20 API breaks (iterate)
```
! cd <worktree>/.local/src/dwl && make clean && make 2>&1 | tee /tmp/dwl-020-build.log
```
Fix each compile error against the wlroots-0.20 headers. Expected small delta
(renamed/moved symbols). Reference: upstream dwl's own 0.19→0.20 migration diff
and wlroots 0.20 release notes. **Loop until 0 errors** (before applying per-app-cast).

### B4. Apply per-app-cast
```
git apply patches/per-app-share-wlroots-next-d41ecb745c.patch
```
It pulls wlroots-0.20 headers (`wlr_ext_image_capture_source_v1.h`,
`wlr_ext_image_copy_capture_v1.h`), adds a `foreign_toplevel` handle + capture
manager + a per-window offscreen scene. **If hunks reject** (the patch was cut
against `wlroots-next` base `d41ecb745c`, not your vendored tree), resolve the
`.rej` files by hand — the touched functions are `setup()`, `mapnotify()`,
`unmapnotify()`, `updatetitle()`, `cleanuplisteners()`, `gpureset()`, and the
`Client` struct.

### B5. Build green vs 0.20
```
! cd <worktree>/.local/src/dwl && make clean && make
```
**Verify:** exit 0, `./dwl` produced.

### B6. Runtime + portal verification
- Install/confirm `xdg-desktop-portal-wlr ≥ 0.8`.
- Configure the portal chooser: in `~/.config/xdg-desktop-portal-wlr/config`
  set `chooser_type = dmenu` (else capture is a silent no-op).
- Run the worktree's dwl. Start a screen-share in a client that uses the portal
  (e.g. OBS PipeWire capture, or a browser WebRTC share) → the picker should
  offer **per-window** (toplevel) sources, and sharing one window captures only
  that window (note: captured surface is re-rendered → small GPU cost).

### B7. Promote to live tree
Only after B5 + B6 pass:
```
cd <dotfiles repo root>
# fast-forward the dwl branch to the worktree's commits, then:
git worktree remove ../dwl-020
! cd ~/.local/src/dwl && make clean && make    # rebuild live tree
```
Restart dwl. Commit:
`feat(dwl): migrate to wlroots-0.20 + per-window screencast (per-app-cast)`

---

## Rollback
- Phase A: `git apply -R patches/gaps.patch patches/pertag.patch` (or `git checkout -- config.def.h dwl.c` before commit).
- Phase B: discard the worktree (`git worktree remove --force ../dwl-020`); live tree never changed until B7.

## Open risks
- **B3 port size unknown.** Worktree isolation + Phase A fallback contain it.
- **Portal not configured** → capture no-ops (B6 covers it).
- **MOD+g clash** in your custom keys[] (A2 covers it).
- **Build not verifiable from agent shell** → all `make`/run steps are `!`/user-run.
