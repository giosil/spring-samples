namespace APP {

	export class GUIDocumenti extends WUX.WComponent {
		
		main: WUX.WContainer;
		
		brcr: Breadcrumb;
		
		constructor() {
			super();
		}
		
		override render() {
			this.brcr = new Breadcrumb();
			this.brcr.add('Documenti');
			
			this.main = new WUX.WContainer();
			this.main
				.before(this.brcr)
				.addRow()
					.addCol('col-md-12')
						.add('<p>Documentazione tecnica</p>');
			
			return this.main;
		}
	}
	
}
