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
		btnExport: WUX.WButton;

		constructor() {
			super('*', 'GUIReport');
		}

		override render() {
			this.brcr = new Breadcrumb();
			this.brcr.add('Report');

			this.form = new WUX.WForm(this.subId('form'));
			this.form
				.addRow()
					.addDateField('id_comune', 'Id comune')
					.addDateField('fiscale', 'Fiscale')
				.addRow()
					.addTextField('descrizione__x', 'Denominazione', {"span": 2});

			this.btnExport = new WUX.WButton(this.subId('btnFind'), 'Genera Report', 'fa-file-excel', 'btn-icon btn btn-primary', 'margin-right: 0.5rem;');
			this.btnExport.on('click', (e: PointerEvent) => {
				let report: ReportReq = {
					"table": 'ana_comuni',
					"fields": ['id_comune', 'fiscale', 'provincia', 'descrizione'],
					"filter": this.form.getState(),
					"orderBy": 'descrizione',
					"maxRows": 50,
					"headers": true,
					"paging": false
				}

				http.post('report/export', report, (data: ReportRes) => {
					console.log('Response:', data);
					if(data && data.content) {
						WUX.saveFile(data.content, 'log.xlsx');
						showSuccess('Report generato con successo.');
					} else {
						showError('Conenuto non disponibile o vuoto.');
					}
				});
			});

			this.main = new WUX.WContainer();
			this.main
				.before(this.brcr)
				.addRow()
					.addCol('col-md-12')
						.add(this.form)
				.addRow()
					.addCol('col-md-8')
						.addGroup({"classStyle": "form-row"}, this.btnExport);

			return this.main;
		}
	}
}