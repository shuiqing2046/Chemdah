
# AnyTextEditor - Free Online Text Editor © 2022 AnyTextEditor
# Edit your texts for free online, improve them and create new ones

# https://anytexteditor.com

set nm1 to array [ pass pass pass pass *b *br *c *ch *d *fr *g *gr *j *k *m *n *p *q *r *t *x *z ]
set nm2 to array [ *u *u *u *u *u *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *a *e *i *o *y *ai *ay *ei *eo *ia *ie *oi ]
set nm3 to array [ *d *ddr *dr *g *gh *k *lb *ldr *lr *lzr *m *mb *mm *mr *n *nd *ndr *nn *r *rd *rg *rr *rs *rv *s *t *th *v *vr *z *zz ]
set nm4 to array [ *cr *d *n *nt *r *rr *sd ]
set nm5 to array [ pass pass *d *g *m *n *nth *r *rth *s *ss *t ]

name case random 1 to 2 [
    when 1 -> join [ random &nm1 random &nm2 random &nm3 random &nm2 random &nm5 ] by pass
    else join [ random &nm1 random &nm2 random &nm3 random &nm2 random &nm4 random &nm2 random &nm5 ] by pass
]