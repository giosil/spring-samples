namespace APP {
	
	export class Breadcrumb extends WUX.WComponent<string, string[]> {
		home: string;
		constructor(id?: string, classStyle?: string, style?: string | WUX.WStyle, attributes?: string | object) {
			super(id ? id : '*', 'Breadcrumb', '/', classStyle, style, attributes);
			this.rootTag = 'nav';
		}
		
		add(link: string): this {
			if(!this.state) this.state = [];
			if(link) this.state.push(link);
			return this;
		}
		
		render() {
			if(!this.home) this.home = '/index.html';
			if(!this._classStyle) this._classStyle = 'mb-5 breadcrumb-container';
			if(!this.props) this.props = '/';
			let s = this._style ? ' style="' + this._style + '"' : '';
			let a = this._attributes ? ' ' + this._attributes : '';
			let r = '<nav class="' + this._classStyle + '" aria-label="breadcrumb"' + s + a + '>';
			r += '<ol class="breadcrumb"><li class="breadcrumb-item"><a href="' + this.home + '">Homepage</a><span class="separator">/</span></li>';
			if(this.state) {
				for(let l of this.state) {
					if(!l) continue;
					if(l[0] == '<') {
						r += '<li class="breadcrumb-item">' + l + '</li>';
					}
					else {
						r += '<li class="breadcrumb-item"><a href="#">' + l + '</a></li>';
					}
				}
			}
			r += '</ol></nav>';
			return r;
		}
	}
	
}