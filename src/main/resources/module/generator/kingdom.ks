
# AnyTextEditor - Free Online Text Editor © 2022 AnyTextEditor
# Edit your texts for free online, improve them and create new ones

# https://anytexteditor.com

set names1 to array [ *ae *ea *ai *au *ou *a *e *i *o *u *a *e *i *o *u *a *e *i *o *u pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass ]
set names2 to array [ *ae *eo *ea *ai *ui *ou *a *e *i *o *u *a *e *i *o *u *a *e *i *o *u ]
set names3 to array [ *b *c *d *g *h *k *l *m *n *p *q *r *s *t *v *w *x *y *z *br *cr *dr *gr *kr *pr *tr *vr *wr *st *sl *ch *sh *ph *kh *th ]
set names4 to array [ *b *c *d *g *k *l *m *n *p *q *r *s *t *v *w *x *y *z *b *c *d *g *k *l *m *n *p *q *r *s *t *v *w *x *y *z *b *c *d *f *g *k *l *m *n *p *q *r *s *t *v *w *x *y *z *bb *cc *dd *ff *gg *kk *ll *mm *nn *pp *rr *ss *tt *zz *br *cr *dr *gr *kr *pr *sr *tr *zr *st *sl *ch *sh *ph *kh *th ]
set names5 to array [ *ba *bet *bia *borg *burg *ca *caea *can *cia *curia *dal *del *dia *dian *do *dor *dora *dour *galla *gary *gia *gon *han *kar *kha *kya *les *lia *lon *lan *lum *lux *lyra *mid *mor *more *nad *nait *nao *nate *nada *neian *nem *nia *nid *niel *ning *ntis *nyth *pan *phate *pia *pis *ra *ral *rean *rene *renth *ria *rian *rid *rin *ris *rith *rus *ryn *sal *san *sea *seon *sha *sian *site *sta *ston *teron *terra *tha *thage *then *thia *tia *tis *tish *ton *topia *tor *tus *valon *setia *vell *ven *via *viel *wen *weth *wyth *ya *zar *zia ]

name case random 1 to 2 [
    when 1 -> join [ random &names1 random &names3 random &names2 random &names5 ] by pass
    else join [ random &names1 random &names3 random &names2 random &names4 random &names2 random &names5 ] by pass
]