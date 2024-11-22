namespace APP {
	
	export function getURLServices() {
		let h = window.location.hostname;
		let p = window.location.protocol;
		if(!p) p = "http:"
		if(!h || h.indexOf('localhost') >= 0) return "http://localhost:8081";
		let s = h.indexOf('.');
		if(s < 1) return "http://localhost:8081";
		return p + '//hcm-services' + h.substring(s);
	}
		
	export class HttpClient {
		url: string;
		sda: any;
		
		constructor(url?: string) {
			if(url) {
				this.url = url;
			}
			else {
				this.url = window.location.origin;
			}
		}
		
		before() {
			window['BSIT'].showLoader();
		}
		
		after() {
			window['BSIT'].hideLoader();
		}
		
		sim(entity: string, params: { [key: string]: any }, success: (result: any) => void, failure?: (error: any) => void) {
			console.log('sim(' + entity + ')', params);
			this.before();
			setTimeout(() => {
				this.after();
				if(success) success(this.sda);
			}, 1000);
		}
		
		get(entity: string, params: { [key: string]: any }, success: (result: any) => void, failure?: (error: any) => void) {
			this._get('GET', entity, params, success, failure);
		}
		
		delete(entity: string, params: { [key: string]: any }, success: (result: any) => void, failure?: (error: any) => void) {
			this._get('DELETE', entity, params, success, failure);
		}
		
		post(entity: string, data: object, success: (result: any) => void, failure?: (error: any) => void) {
			this._send('POST', entity, data, success, failure);
		}
		
		put(entity: string, data: object, success: (result: any) => void, failure?: (error: any) => void) {
			this._send('PUT', entity, data, success, failure);
		}
		
		patch(entity: string, data: object, success: (result: any) => void, failure?: (error: any) => void) {
			this._send('PATCH', entity, data, success, failure);
		}
		
		_get(method: string, entity: string, params: { [key: string]: any }, success: (result: any) => void, failure?: (error: any) => void) {
			if(!method) method = 'GET';
			let search = params ? new URLSearchParams(params).toString() : "";
			let requrl = search ? this.url + "/" + entity + "?" + search : this.url + entity;
			this.before();
			fetch(requrl, {
				"method" : method
			})
			.then(response => {
				this.after();
				if (!response.ok) {
					console.error('[AppClient] ' + method + ' ' + entity + ': HTTP ' + response.status);
					if(failure) {
						failure(new Error("HTTP " + response.status));
					}
					else {
						showError("Errore servizio", "Si e' verificato un errore di servizio.");
					}
					return;
				}
				return response.json();
			})
			.then(data => {
				if(success) success(data);
			})
			.catch(error => {
				console.error('[AppClient] ' + method + ' ' + entity + ':', error);
				this.after();
				if(failure) {
					failure(error);
				}
				else {
					showError("Errore servizio", "Si e' verificato un errore di servizio.");
				}
			});
		}
		
		_send(method: string, entity: string, data: object, success: (result: any) => void, failure?: (error: any) => void) {
			if(!method) method = 'POST';
			let requrl = this.url + "/" + entity;
			this.before();
			fetch(requrl, {
				"method" : method,
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(data)
			})
			.then(response => {
				this.after();
				if (!response.ok) {
					console.error('[AppClient] ' + method + ' ' + entity + ': HTTP ' + response.status);
					if(failure) {
						failure(new Error("HTTP " + response.status));
					}
					else {
						showError("Errore servizio", "Si e' verificato un errore di servizio.");
					}
					return;
				}
				return response.json();
			})
			.then(data => {
				if(success) success(data);
			})
			.catch(error => {
				console.error('[AppClient] ' + method + ' ' + entity + ':', error);
				this.after();
				if(failure) {
					failure(error);
				}
				else {
					showError("Errore servizio", "Si e' verificato un errore di servizio.");
				}
			});
		}
	}

	export let http = new HttpClient(getURLServices());
}