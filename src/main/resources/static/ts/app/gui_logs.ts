namespace APP {

	import WUtil = WUX.WUtil;

	export class GUILogs extends WUX.WComponent {
		main: WUX.WContainer;

		brcr: Breadcrumb;

		// Left
		cntL: WUX.WContainer;
		formL: WUX.WForm;
		btnLFind: WUX.WButton;
		btnLReset: WUX.WButton;
		tabL: WUX.WTable;

		// Right
		cntR: WUX.WContainer;
		formR: WUX.WForm;
		btnRFind: WUX.WButton;
		btnRReset: WUX.WButton;
		tabR: WUX.WTable;

		constructor(id?: string) {
			super(id ? id : '*', 'GUILogs');
		}

		protected render() {
			this.brcr = new Breadcrumb();
			this.brcr.add('Log');

			this.formL = new WUX.WForm(this.subId('forml'));
			this.formL.addRow();
			this.formL.addTextField('folder', 'Cartella');
			
			this.formL.onEnter(() => {
				this.btnLFind.trigger('click');
			});

			this.btnLFind = new WUX.WButton(this.subId('btnlf'), 'Lista', 'fa-search', 'btn-icon btn btn-primary', 'margin-right: 0.5rem;');
			this.btnLFind.on('click', (e: PointerEvent) => {
				let filter = this.formL.getState();
				http.get('log/list', filter, (data: string[]) => {
					if(!data) data = [];
					let l = data.length;
					if(!l) {
						showWarning('Nessun elemento trovato.');
					}
					// string[] -> string[][]
					let s = [];
					for(let r of data) {
						s.push([r]);
					}
					this.tabL.setState(s);
				});
			});
			this.btnLReset = new WUX.WButton(this.subId('btnlr'), 'Annulla', 'fa-undo', 'btn-icon btn btn-secondary');
			this.btnLReset.on('click', (e: PointerEvent) => {
				this.formL.clear();
				this.tabL.setState([]);
			});

			let lc = [
				['File', '0', 's']
			];
			this.tabL = new WUX.WTable(this.subId('tabl'), WUtil.col(lc, 0), WUtil.col(lc, 1));
			this.tabL.selectionMode = 'single';
			this.tabL.types = WUtil.col(lc, 2);
			this.tabL.css({ h: 480 });
			this.tabL.onDoubleClick(() => {
				let srd = this.tabL.getSelectedRowsData();
				if (!srd || !srd.length) return;
				this.btnRFind.trigger('click');
			});

			this.formR = new WUX.WForm(this.subId('formr'));
			this.formR.addRow();
			this.formR.addNumberField('first', 'Prime righe');
			this.formR.addNumberField('last', 'Ultime righe');
			this.formR.addTextField('search', 'Testo contenuto');

			this.formR.onEnter(() => {
				this.btnRFind.trigger('click');
			});

			this.btnRFind = new WUX.WButton(this.subId('btnrf'), 'Vedi file', 'fa-file', 'btn-icon btn btn-primary', 'margin-right: 0.5rem;');
			this.btnRFind.on('click', (e: JQueryEventObject) => {
				let srd = this.tabL.getSelectedRowsData();
				if(!srd || !srd.length) {
					showWarning('Selezionare prima il file da visualizzare');
				}
				let filter = this.formR.getState();
				if(!filter) filter = {};
				filter["name"] = srd[0][0];
				http.get('log/read', filter, (data: string[]) => {
					if(!data) data = [];
					let l = data.length;
					if(!l) {
						showWarning('Contenuto non presente.');
					}
					// string[] -> string[][]
					let s = [];
					for(let r of data) {
						s.push([r]);
					}
					this.tabR.setState(s);
				});
			});
			this.btnRReset = new WUX.WButton(this.subId('btnrr'), 'Annulla', 'fa-undo', 'btn-icon btn btn-secondary');
			this.btnRReset.on('click', (e: JQueryEventObject) => {
				this.formR.clear();
				this.tabR.setState([]);
				this.formR.focus();
			});

			let rc = [
				['Testo', '0', 's'],
			];
			this.tabR = new WUX.WTable(this.subId('tabr'), WUtil.col(rc, 0), WUtil.col(rc, 1));
			this.tabR.types = WUtil.col(rc, 2);
			this.tabR.css({ h: 480 });
			this.tabR.onRowPrepared((e: { element?: Element, rowElement?: Element, data?: any, rowIndex?: number }) => {
				if (!e.data) return;
				let r = e.data[0]
				if(r && r.indexOf('WARN') >= 0) {
					WUX.setCss(e.rowElement, WUX.CSS.WARNING);
				}
				else if(r && r.indexOf('ERROR') >= 0) {
					WUX.setCss(e.rowElement, WUX.CSS.DANGER);
				}
			});
			
			this.cntL = new WUX.WContainer(this.subId('cntl'));
			this.cntL
				.addRow()
					.addCol('col-md-12')
						.add(this.formL)
				.addRow()
					.addCol('col-md-12')
						.addGroup({"classStyle": "form-row"}, this.btnLFind, this.btnLReset)
				.addRow()
					.addCol('12')
						.add(this.tabL);

			this.cntR = new WUX.WContainer(this.subId('cntr'));
			this.cntR
				.addRow()
					.addCol('col-md-12')
						.add(this.formR)
				.addRow()
					.addCol('col-md-12')
						.addGroup({"classStyle": "form-row"}, this.btnRFind, this.btnRReset)
				.addRow()
					.addCol('12')
						.add(this.tabR);

			this.main = new WUX.WContainer();
			this.main
				.before(this.brcr)
				.addRow()
					.addCol('4')
						.add(this.cntL)
					.addCol('8')
						.add(this.cntR);

			return this.main;
		}

		protected componentDidMount(): void {
			setTimeout(() => {
				this.btnLFind.trigger('click');
			}, 0);
		}
	}


}
