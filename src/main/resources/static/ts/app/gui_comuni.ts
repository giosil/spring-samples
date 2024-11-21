namespace APP {

	import action = WUX.action;
	import getAction = WUX.getAction;
	import WUtil = WUX.WUtil;

	export class GUIComuni extends WUX.WComponent {
		
		main: WUX.WContainer;
		
		brcr: Breadcrumb;
		
		form: WUX.WFormPanel;
		btnFind: WUX.WButton;
		btnReset: WUX.WButton;
		table: WUX.WTable;
		// Dialogs
		dlg: DlgComune;
		
		constructor() {
			super();
			
			this.dlg = new DlgComune(this.subId('dlg'));
			this.dlg.onHiddenModal((e: JQueryEventObject) => {
				if (!this.dlg.ok) return;
				
				let a = this.dlg.getProps();
				let s = this.dlg.getState();
				if(!a || !s) return;
				console.log('dlg action,state', a, s);
			});
		}
		
		override render() {
			this.brcr = new Breadcrumb();
			this.brcr.add('Comuni');
			
			this.form = new WUX.WFormPanel(this.subId('form'));
			this.form
				.addRow()
					.addTextField('name', 'Denominazione', false, true);
			
			this.btnFind = new WUX.WButton(this.subId('btnFind'), 'Esegui ricerca', 'fa-search', 'btn-icon btn btn-primary', 'margin-right: 0.5rem;');
			this.btnFind.on('click', (e: PointerEvent) => {
				
				this.doFind();
				
			});
			
			let h = ['Identificativo', 'Denominazione', 'Cod. Fiscale', 'Provincia', 'Vedi', 'Modifica'];
			let k = ['idComune',       'descrizione',   'fiscale',      'provincia', '_v',   '_m'];
			this.table = new WUX.WTable(this.subId('tapp'), h, k);
			this.table.selectionMode = 'single';
			this.table.div = 'table-responsive';
			this.table.types = ['s', 's', 's', 's', 'w', 'w'];
			this.table.sortable = [1, 3];
			this.table.on('click', (e: PointerEvent) => {
				let a = getAction(e, this);
				console.log('click a=', a);
				if(!a || !a.ref) return;
				
				let s = this.table.getState();
				let x = WUtil.indexOf(s, 'idComune', a.ref);
				if(x < 0) return;
				
				this.dlg.setProps(a.name);
				this.dlg.setState(s[x]);
				this.dlg.show(this);
			});
			this.table.onDoubleClick((e: {element?: Element; rowElement?: Element; data?: Comune; rowIndex?: number; }) => {
				this.dlg.setProps('view');
				this.dlg.setState(e.data);
				this.dlg.show(this);
 			});
			
			this.btnReset = new WUX.WButton(this.subId('btnReset'), 'Annulla', 'fa-undo', 'btn-icon btn btn-secondary');
			this.btnReset.on('click', (e: PointerEvent) => {
				this.form.clear();
				this.table.setState([]);
				this.form.focus();
			});
			
			this.main = new WUX.WContainer();
			this.main
				.before(this.brcr)
				.addRow()
					.addCol('col-md-12')
						.add(this.form)
				.addRow('form-row justify-content-end')
					.add(this.btnFind)
					.add(this.btnReset)
				.addRow()
					.addCol('col-md-12')
						.add(this.table);
			
			return this.main;
		}
		
		doFind() {
			http.get('comuni', this.form.getState(), (data: Comune[]) => {
				if(!data || !data.length) {
					showWarning('Nessun elemento presente');
					return;
				}
				for(let record of data) {
					record["_v"] = action('view', record["idComune"], 'fa-search');
					record["_m"] = action('edit', record["idComune"], 'fa-edit');
				}
				this.table.setState(data);
			});
		}
	}
	
	export class DlgComune extends WUX.WDialog<string, Comune> {
		fp: WUX.WFormPanel;
		
		constructor(id: string) {
			super(id, 'DlgComune');
			
			this.title = 'Comune';
			
			this.fp = new WUX.WFormPanel(this.subId('fp'));
			this.fp.addRow();
			this.fp.addTextField('idComune', 'Identificativo', true);
			this.fp.addRow();
			this.fp.addTextField('fiscale', 'Codice Fiscale');
			this.fp.addRow();
			this.fp.addTextField('provincia', 'Provincia');
			this.fp.addRow();
			this.fp.addTextField('descrizione', 'Denominazione');
			this.fp.addRow();
			this.fp.addTextField('idRegione', 'Codice Regione');
			
			this.body
				.addRow()
				.addCol('col-md-12')
				.add(this.fp);
		}
		
		override updateState(nextState: Comune): void {
			this.state = nextState;
			if(this.fp) {
				this.fp.setState(this.state);
			}
		}
		
		override getState(): Comune {
			if(this.fp) {
				this.state = this.fp.getState();
			}
			return this.state;
		}
		
		override onClickOk(): boolean {
			return true;
		}
		
		protected onShown() {
			setTimeout(() => { this.fp.focusOn('fiscale'); });
		}
		
		clear() {
			if(this.fp) {
				this.fp.clear();
			}
			this.state = null;
		}
	}
}
