namespace APP {

	export function showInfo(m: string, title?: string) {
		if(!title) title = "Messaggio";
		window["BSIT"].notify({"state": 'info', "title" : title, "message": m});
	}

	export function showSuccess(m: string, title?: string) {
		if(!title) title = "Messaggio";
		window["BSIT"].notify({"state": 'success', "title" : title, "message": m});
	}

	export function showWarning(m: string, title?: string) {
		if(!title) title = "Messaggio";
		window["BSIT"].notify({"state": 'warning', "title" : title, "message": m});
	}

	export function showError(m: string, title?: string) {
		if(!title) title = "Messaggio";
		window["BSIT"].notify({"state": 'error', "title" : title, "message": m});
	}

}