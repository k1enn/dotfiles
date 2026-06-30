/* simple_scratchpad.c — dwm-style scratchpad for dwl.
 * Source: https://codeberg.org/dwl/dwl-patches patches/simple_scratchpad (v0.8)
 * Author: julmajustus <julmajustus@tutanota.com>
 * Fork-local: included from dwl.c (see "#include \"simple_scratchpad.c\"").
 * Re-apply / keep alongside the dwl.c hunks after an upstream pull. */

void
addscratchpad(const Arg *arg)
{
	Client *cc, *c = focustop(selmon);

	if (!c)
		return;
	/* Check if the added client is already a scratchpad client */
	for (int i = 0; i < SCRATCHPAD_COUNT; i++) {
		wl_list_for_each(cc, &scratchpad_clients[i], link_temp) {
			if (cc == c)
				return;
		}
	}
	if (!c->isfloating) {
		setfloating(c, 1);
	}
	wl_list_insert(&scratchpad_clients[scratchpad_sel], &c->link_temp);
}

void
togglescratchpad(const Arg *arg)
{
	Client *c;
	Monitor *m = selmon;

	scratchpad_visible[scratchpad_sel] = !scratchpad_visible[scratchpad_sel];
	if (scratchpad_visible[scratchpad_sel]) {
		wl_list_for_each(c, &scratchpad_clients[scratchpad_sel], link_temp) {
			c->mon = m;
			c->tags = m->tagset[m->seltags];
			arrange(m);
			focusclient(c, 1);
		}
	} else {
		wl_list_for_each(c, &scratchpad_clients[scratchpad_sel], link_temp) {
			c->tags = 0;
			focusclient(focustop(m), 1);
			arrange(m);
		}
	}
}

void
removescratchpad(const Arg *arg)
{
	Client *sc, *c = focustop(selmon);
	if (!c)
		return;
	for (int i = 0; i < SCRATCHPAD_COUNT; i++) {
		/* Check if c is in scratchpad_clients */
		wl_list_for_each(sc, &scratchpad_clients[i], link_temp) {
			if (sc == c) {
				wl_list_remove(&c->link_temp);
				return;
			}
		}
	}
}
