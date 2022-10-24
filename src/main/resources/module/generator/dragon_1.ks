
# AnyTextEditor - Free Online Text Editor © 2022 AnyTextEditor
# Edit your texts for free online, improve them and create new ones

# https://anytexteditor.com

set nm6 to array [ pass pass pass pass pass pass *b *c *ch *d *f *fr *g *h *l *m *n *p *q *r *s *sh *t *v *z ]
set nm7 to array [ *u *u *u *u *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *ae *ai *ay *ei *eo *ie *io *oa ]
set nm8 to array [ *d *dh *g *gh *k *ldr *ll *m *mm *mr *n *nd *ndr *nn *p *ph *r *rg *rl *rm *rr *rs *rv *s *ss *t *th *v *vn *z *zz ]
set nm9 to array [ *d *l *n *nt *ph *r *rr *ss ]
set nm10 to array [ pass pass pass pass pass *l *lth *n *nth *r *rth *s *ss *t *th ]

name case random 1 to 2 [
    when 1 -> join [ random &nm6 random &nm7 random &nm8 random &nm7 random &nm10 ] by pass
    else join [ random &nm6 random &nm7 random &nm8 random &nm7 random &nm9 random &nm7 random &nm10 ] by pass
]