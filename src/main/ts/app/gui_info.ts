namespace APP {

	export class GUIInfo extends WUX.WComponent {
		
		main: WUX.WContainer;
		
		brcr: Breadcrumb;
		
		constructor() {
			super();
		}
		
		override render() {
			this.brcr = new Breadcrumb();
			this.brcr.add('Informazioni');
			
			this.main = new WUX.WContainer();
			this.main
				.before(this.brcr)
				.addRow()
					.addCol('col-md-12')
						.add('<p>Informazioni di sistema</p>');
			
			return this.main;
		}
	}
	
}
