namespace APP {

	export class GUILogs extends WUX.WComponent {
		
		main: WUX.WContainer;
		
		brcr: Breadcrumb;
		
		constructor() {
			super();
		}
		
		override render() {
			this.brcr = new Breadcrumb();
			this.brcr.add('Log');
			
			this.main = new WUX.WContainer();
			this.main
				.before(this.brcr)
				.addRow()
					.addCol('col-md-12')
						.add('<p>Consultazione log operazioni</p>');
			
			return this.main;
		}
	}
	
}
