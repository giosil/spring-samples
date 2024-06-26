$(document).ready(function() {
	_init();
});
function _init() {
	let t = document.getElementById('nav1-items');
	if(t) {
		let items = _item('Homepage',    'index.html');
		items += _item('Comuni',         'app-conf');
		items += _item('Documenti',      'app-docs');
		items += _item('Log di sistema', 'app-logs');
		items += _item('Informazioni',   'app-info');
		t.innerHTML = items;
	}
	var p = location.pathname;
	if(!p || p == '/' || p == '/home' || p.endsWith('/index.html')) {
		let a = document.getElementById('single-spa-application');
		if(a) {
			let cards = `<h2>Welcome</h2><br><ul>`;
			cards += _card('edit.png', 'Comuni',         'Consultazione comuni',     'app-conf');
			cards += _card('docs.png', 'Documenti',      'Documentazione tecnica',   'app-docs');
			cards += _card('cal.png',  'Log di sistema', 'Log operazioni',           'app-logs');
			cards += _card('card.png', 'Informazioni',   'Informazioni di sistema',  'app-info');
			cards += `</ul>`;
			a.innerHTML = cards;
		}
	}
}
function _item(title, href) {
	let p = location.pathname;
	if(!p || p == '/' || p == '/home') p = '/index.html';
	let s = p.endsWith(href) ? 'style="font-weight:bolder;"' : '';
	let x = href.endsWith('index.html') ? '' : '&nbsp; | &nbsp;';
	return `${x}<a href="${href}" title="${title}" ${s}>${title}</a>`;
}
function _card(icon, title, text, href) {
	return `<li><a href="${href}" title="${title}">${title}</a> - ${text}</li>`;
}