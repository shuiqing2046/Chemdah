
# AnyTextEditor - Free Online Text Editor © 2022 AnyTextEditor
# Edit your texts for free online, improve them and create new ones

# https://anytexteditor.com

set nm11 to array [ pass pass pass pass *b *c *ch *d *fr *g *m *n *p *q *r *s *t *v *x *z ]
set nm12 to array [ *u *u *u *u *a *e *i *o *y *ae *ai *ay *ei *eo *ia *ie *io *oa *oi ]
set nm13 to array [ *d *dh *dr *g *gh *k *l *ldr *ll *lr *m *mm *mr *n *nd *ndr *nn *p *ph *r *rl *rm *rr *rs *rv *s *ss *t *th *v *vn *vr *z *zz ]
set nm14 to array [ *d *l *n *nt *ph *r *rr *ss ]
set nm15 to array [ pass pass pass *d *g *l *lth *n *nth *r *rth *s *ss *t *th ]

name case random 1 to 2 [
    when 1 -> join [ random &nm11 random &nm12 random &nm13 random &nm12 random &nm15 ] by pass
    else join [ random &nm11 random &nm12 random &nm13 random &nm12 random &nm14 random &nm12 random &nm15 ] by pass
]