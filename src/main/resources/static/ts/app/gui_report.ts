namespace APP {

	export interface ReportReq {
		table: string;
		title?: string;
		type?: 'xls' | 'xlsx' | 'csv';
		fields?: string[];
		filter?: any;
		orderBy?: string;
		maxRows?: number;
		headers?: boolean;
		paging?: boolean;
		groupBy?: string[];
	}

	export interface ReportRes {
		title?: string;
		type?: string;
		rows?: number;
		content?: string;
	}

	export class GUIReport extends WUX.WComponent {
		// Components
		main: WUX.WContainer;
		brcr: Breadcrumb;
		form: WUX.WForm;
		table: WUX.WTable;
		btnFind: WUX.WButton;
		btnReset: WUX.WButton;

		constructor() {
			super('*', 'GUIReport');
		}

		override render() {
			this.brcr = new Breadcrumb();
			this.brcr.add('Report');

			let operazioni = ['', 'POST', 'PUT', 'DELETE'];

			this.form = new WUX.WForm(this.subId('form'));
			this.form
				.addRow()
					.addDateField('log_data__gte', 'Dalla data')
					.addDateField('log_data__lte', 'Alla data')
				.addRow()
					.addOptionsField('log_operazione__in0', 'Operazione', operazioni)
					.addOptionsField('log_operazione__in1', 'Operazione', operazioni)
				.addRow()
					.addBlankField()
				.addRow()
					.addTextField('log_funzione__x', 'Funzione', {"span": 2})
					.addTextField('__groupBy__', 'Raggruppa per')
					.addToggleField('__preview__', 'Anteprima')

			this.btnFind = new WUX.WButton(this.subId('btnFind'), 'Genera Report', 'fa-file-excel', 'btn-icon btn btn-primary', 'margin-right: 0.5rem;');
			this.btnFind.on('click', (e: PointerEvent) => {

				this.doFind();

			});
			this.btnReset = new WUX.WButton(this.subId('btnReset'), 'Annulla', 'fa-undo', 'btn-icon btn btn-secondary');
			this.btnReset.on('click', (e: PointerEvent) => {

				this.doReset();

			});

			this.table = new WUX.WTable(this.subId('tapp'), [''], ['0']);
			// Questo consente di forzare il refresh della tabella
			this.table.forceOnChange = true;

			this.main = new WUX.WContainer();
			this.main
				.before(this.brcr)
				.addRow()
					.addCol('col-md-12')
						.add(this.form)
				.addRow()
					.addCol('col-md-8')
						.addGroup({"classStyle": "form-row"}, this.btnFind, this.btnReset)
				.addRow()
					.addCol('col-md-12', 'padding-top: 1rem;')
						.add(this.table);

			return this.main;
		}

		clearTable() {
			this.table.header = [''];
			this.table.keys = ['0'];
			this.table.setState([]);
		}

		doFind() {
			if(this.form.isBlank()) {
				showWarning('Nessun criterio di ricerca specificato.');
				return;
			}
			let filter = this.form.getState();

			let report: ReportReq = {
				"table": 'APP_LOG',
				"title": 'Report Operazioni',
				"fields": ['CODICE_FISCALE', 'LOG_OPERAZIONE', 'LOG_FUNZIONE'],
				"filter": filter,
				"orderBy": 'LOG_DATA DESC',
				"maxRows": 50,
				"headers": true,
				"paging": false,
				"groupBy": []
			}

			if(filter && filter['__preview__']) {
				http.post('report/select', report, (data: [][]) => {
					console.log('Response:', data);
					if(data && data.length > 0) {
						// Remove first row (headers)
						let dh = data.shift();
						let ks = [];
						for(let i = 0; i < dh.length; i++) {
							ks.push('' + i);
						}
						this.table.header = dh;
						this.table.keys = ks;
						this.table.setState(data);
						showSuccess('Report generato con successo.');
					} else {
						this.clearTable()
						showError('Conenuto non disponibile o vuoto.');
					}
				});
			}
			else {
				http.post('report/export', report, (data: ReportRes) => {
					console.log('Response:', data);
					this.clearTable();
					if(data && data.content) {
						WUX.saveFile(data.content, 'report.xlsx');
						showSuccess('Report generato con successo.');
					} else {
						showError('Conenuto non disponibile o vuoto.');
					}
				});
			}
		}

		doReset() {
			this.clearTable();
			this.form.clear();
			this.form.focus();
		}
	}
}