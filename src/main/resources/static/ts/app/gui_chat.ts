namespace APP {

	import WUtil = WUX.WUtil;

	export class GUIChat extends WUX.WComponent {
		// Components
		main: WUX.WContainer;
		brcr: Breadcrumb;
		form: WUX.WForm;
		btnSend: WUX.WButton;
		btnReset: WUX.WButton;
		idOut: string;
		defModel: string = 'ai/llama3.2:1B-Q4_0';
		defMaxTokens: number = 1000;
		defTemperature: number = 0.7;

		constructor() {
			super('*', 'GUIChat');
		}

		override render() {
			this.brcr = new Breadcrumb();
			this.brcr.add('Chat');

			let models = ['', 'ai/llama3.2:1B-Q4_0']

			this.form = new WUX.WForm(this.subId('form'));
			this.form
				.addRow()
					.addOptionsField('model', 'Modello', models)
					.addTextField('maxTokens', 'Max Tokens')
					.addTextField('temperature', 'Temperatura (0.0-2.0)')
				.addRow()
					.addTextField('topP', 'Top P (0.0-1.0)')
					.addTextField('freqPenalty', 'Frequency penalty (0.0-2.0)')
					.addTextField('presPenalty', 'Presence penalty (0.0-2.0)')
				.addRow()
					.addTextField('sysMessage', 'Contesto (role=system)')
					.addTextField('assMessage', 'Risposta attesa (role=assistant)')
				.addRow()
					.addNoteField('usrMessage', 'Prompt (role=user)', 3);

			this.form.setValue('model', this.defModel);
			this.form.setValue('maxTokens', this.defMaxTokens);
			this.form.setValue('temperature', this.defTemperature);

			this.btnSend = new WUX.WButton(this.subId('btnSend'), 'Invia', 'fa-arrow-right', 'btn-icon btn btn-primary btn-block', 'margin-bottom:0.5rem;min-width:8rem;');
			this.btnSend.on('click', (e: PointerEvent) => {

				this.doSend();

			});
			this.btnReset = new WUX.WButton(this.subId('btnReset'), 'Annulla', 'fa-undo', 'btn-icon btn btn-secondary btn-block', 'margin-bottom:0.5rem;min-width:8rem;');
			this.btnReset.on('click', (e: PointerEvent) => {

				this.doReset();

			});
	
			this.idOut = this.subId('out');

			this.main = new WUX.WContainer();
			this.main
				.before(this.brcr)
				.addRow()
					.addCol('col-md-10')
						.add(this.form)
					.addCol('col-md-2')
						.add(this.btnSend)
						.add(this.btnReset)
				.addRow()
					.addCol('col-md-12')
						.add("<div id='" + this.idOut + "' style='white-space:pre-wrap;font-weight:bolder;'></div>");

			return this.main;
		}

		doSend() {
			let values = this.form.getState();

			let model = values['model'];
			let maxT  = values['maxTokens'];
			let temp  = values['temperature'];
			let topP  = values['topP'];
			let freq  = values['freqPenalty'];
			let pres  = values['presPenalty'];

			if (maxT != null) maxT = parseInt(maxT);
			if (temp != null) temp = parseFloat(temp);
			if (topP != null) topP = parseFloat(topP);
			if (freq != null) freq = parseFloat(freq);
			if (pres != null) pres = parseFloat(pres);

			let usrMessage = WUtil.getString(values, 'usrMessage');
			if(!usrMessage) {
				showWarning("Scrivere un prompt da inviare");
				return;
			}
			let heOut = document.getElementById(this.idOut);
			if (!heOut) {
				showWarning("Elemento con id '" + this.idOut + "' non trovato.");
				return;
			}
			heOut.textContent = '';

			let sysMessage = WUtil.getString(values, 'sysMessage');
			let assMessage = WUtil.getString(values, 'assMessage');
			let messages = [];
			if (sysMessage) {
				messages.push({ role: "system", content: sysMessage });
			}
			if (usrMessage) {
				messages.push({ role: "user", content: usrMessage });
			}
			if (assMessage) {
				messages.push({ role: "assistant", content: assMessage });
			}

			window['BSIT'].showLoader();
			fetch('http://localhost:8081/chat/completions', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({
					model: model,
					messages: messages,
					max_tokens: maxT,
					temperature: temp,
					top_p: topP,
					frequency_penalty: freq,
					presence_penalty: pres,
					stream: true
				})
			})
			.then(res => {
				window['BSIT'].hideLoader();
				if (!res.ok) {
					console.error('HTTP error', res.status);
					showError('Errore HTTP ' + res.status, 'Errore chiamata');
					return;
				}
				const tdec = new TextDecoder("utf-8");
				const reader = res.body.getReader();
				function read() {
					reader.read().then(({ done, value }) => {
						if (done) return;
						const chunk = tdec.decode(value, { stream: true });
						heOut.textContent += chunk;
						read();
					});
				}
				read();
			})
			.catch(error => {
				window['BSIT'].hideLoader();
				console.log('Fetch error:', error);
				showError('Servizio non disponibile.', 'Errore chiamata');
			});
		}

		doReset() {
			let heOut = document.getElementById(this.idOut);
			if (heOut) heOut.textContent = '';
			this.form.clear();
			this.form.setValue('model', this.defModel);
			this.form.setValue('maxTokens', this.defMaxTokens);
			this.form.setValue('temperature', this.defTemperature);
			this.form.focus();
		}
	}
}