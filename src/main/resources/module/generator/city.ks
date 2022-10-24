
# AnyTextEditor - Free Online Text Editor © 2022 AnyTextEditor
# Edit your texts for free online, improve them and create new ones

# https://anytexteditor.com

set names1 to array [ *a *e *i *o *u pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass ]
set names2 to array [ *b *c *d *f *g *h *j *k *l *m *n *p *q *r *s *t *v *w *x *y *z *br *cr *dr *fr *gr *kr *pr *qr *sr *tr *vr *wr *yr *zr *str *bl *cl *fl *gl *kl *pl *sl *vl *yl *zl *ch *kh *ph *sh *yh *zh ]
set names3 to array [ *a *e *i *o *u *a *e *i *o *u *a *e *i *o *u *a *e *i *o *u *a *e *i *o *u *ae *ai *au *aa *ee *ea *eu *ia *ie *oi *ou *ua *ue *ui *uo *uu *a *e *i *o *u *a *e *i *o *u *a *e *i *o *u *a *e *i *o *u *a *e *i *o *u ]
set names4 to array [ *b *c *d *f *g *h *j *k *l *m *n *p *q *r *s *t *v *w *x *y *z *br *cr *dr *fr *gr *kr *pr *tr *vr *wr *zr *st *bl *cl *fl *gl *kl *pl *sl *vl *zl *ch *kh *ph *sh *zh ]
set names5 to array [ *c *d *f *h *k *l *m *n *p *r *s *t *x *y pass pass pass pass pass pass pass pass pass pass pass pass pass pass pass ]
set names6 to array [ *aco *ada *adena *ago *agos *aka *ale *alo *am *anbu *ance *and *ando *ane *ans *anta *arc *ard *ares *ario *ark *aso *athe *eah *edo *ego *eigh *eim *eka *eles *eley *ence *ens *ento *erton *ery *esa *ester *ey *ia *ico *ido *ila *ille *in *inas *ine *ing *irie *ison *ita *ock *odon *oit *ok *olis *olk *oln *ona *oni *onio *ont *ora *ord *ore *oria *ork *osa *ose *ouis *ouver *ul *urg *urgh *ury ]
set names7 to array [ *bert *bridge *burg *burgh *burn *bury *bus *by *caster *cester *chester *dale *dence *diff *ding *don *fast *field *ford *gan *gas *gate *gend *ginia *gow *ham *hull *land *las *ledo *lens *ling *mery *mond *mont *more *mouth *nard *phia *phis *polis *pool *port *pus *ridge *rith *ron *rora *ross *rough *sa *sall *sas *sea *set *sey *shire *son *stead *stin *ta *tin *tol *ton *vale *ver *ville *vine *ving *well *wood ]

name case random 1 to 4 [
    when 1 -> join [ random &names1 random &names2 random &names3 random &names5 random &names7 ] by pass
    when 2 -> join [ random &names4 random &names3 random &names5 random &names7 ] by pass
    when 3 -> join [ random &names1 random &names2 random &names6 ] by pass
    else join [ random &names6 random &names7 ] by pass
]