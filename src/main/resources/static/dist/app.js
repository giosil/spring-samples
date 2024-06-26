var APP;
(function (APP) {
    function showInfo(m, title) {
        if (!title)
            title = "Messaggio";
        window["BSIT"].notify({ "state": 'info', "title": title, "message": m });
    }
    APP.showInfo = showInfo;
    function showSuccess(m, title) {
        if (!title)
            title = "Messaggio";
        window["BSIT"].notify({ "state": 'success', "title": title, "message": m });
    }
    APP.showSuccess = showSuccess;
    function showWarning(m, title) {
        if (!title)
            title = "Messaggio";
        window["BSIT"].notify({ "state": 'warning', "title": title, "message": m });
    }
    APP.showWarning = showWarning;
    function showError(m, title) {
        if (!title)
            title = "Messaggio";
        window["BSIT"].notify({ "state": 'error', "title": title, "message": m });
    }
    APP.showError = showError;
    function dropdownBtn(id, t, items, cls) {
        if (cls === void 0) { cls = 'btn btn-default'; }
        var r = '<button id="' + id + '-b" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" class="dropdown-toggle ' + cls + '">' + t + ' <i class="fa fa-caret-down"></i></button>';
        var a = ' tabindex="-1"';
        var s = ' style="max-height:180px;overflow-y:auto;"';
        r += '<div id="' + id + '-m"' + a + ' role="menu" aria-hidden="true" class="dropdown-menu"' + s + '>';
        r += '<div class="link-list-wrapper">';
        r += '<ul id="' + id + '-l" class="link-list">';
        if (items)
            r += items;
        r += '</ul></div></div>';
        return r;
    }
    APP.dropdownBtn = dropdownBtn;
})(APP || (APP = {}));
var APP;
(function (APP) {
    var HttpClient = /** @class */ (function () {
        function HttpClient(url) {
            if (url) {
                this.url = url;
            }
            else {
                this.url = window.location.origin;
            }
        }
        HttpClient.prototype.before = function () {
            window['BSIT'].showLoader();
        };
        HttpClient.prototype.after = function () {
            window['BSIT'].hideLoader();
        };
        HttpClient.prototype.get = function (entity, params, success, failure) {
            this._get('GET', entity, params, success, failure);
        };
        HttpClient.prototype.delete = function (entity, params, success, failure) {
            this._get('DELETE', entity, params, success, failure);
        };
        HttpClient.prototype.post = function (entity, data, success, failure) {
            this._send('POST', entity, data, success, failure);
        };
        HttpClient.prototype.put = function (entity, data, success, failure) {
            this._send('PUT', entity, data, success, failure);
        };
        HttpClient.prototype.patch = function (entity, data, success, failure) {
            this._send('PATCH', entity, data, success, failure);
        };
        HttpClient.prototype._get = function (method, entity, params, success, failure) {
            var _this = this;
            if (!method)
                method = 'GET';
            var search = params ? new URLSearchParams(params).toString() : "";
            var requrl = search ? this.url + "/" + entity + "?" + search : this.url + entity;
            this.before();
            fetch(requrl, {
                "method": 'GET'
            })
                .then(function (response) {
                _this.after();
                if (!response.ok) {
                    console.error('[AppClient] ' + method + ' ' + entity + ': HTTP ' + response.status);
                    if (failure) {
                        failure(new Error("HTTP " + response.status));
                    }
                    else {
                        APP.showError("Errore servizio", "Si e' verificato un errore di servizio.");
                    }
                    return;
                }
                return response.json();
            })
                .then(function (data) {
                if (success)
                    success(data);
            })
                .catch(function (error) {
                console.error('[AppClient] ' + method + ' ' + entity + ':', error);
                _this.after();
                if (failure) {
                    failure(error);
                }
                else {
                    APP.showError("Errore servizio", "Si e' verificato un errore di servizio.");
                }
            });
        };
        HttpClient.prototype._send = function (method, entity, data, success, failure) {
            var _this = this;
            if (!method)
                method = 'POST';
            var requrl = this.url + "/" + entity;
            this.before();
            fetch(requrl, {
                "method": method,
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(data)
            })
                .then(function (response) {
                _this.after();
                if (!response.ok) {
                    console.error('[AppClient] ' + method + ' ' + entity + ': HTTP ' + response.status);
                    if (failure) {
                        failure(new Error("HTTP " + response.status));
                    }
                    else {
                        APP.showError("Errore servizio", "Si e' verificato un errore di servizio.");
                    }
                    return;
                }
                return response.json();
            })
                .then(function (data) {
                if (success)
                    success(data);
            })
                .catch(function (error) {
                console.error('[AppClient] ' + method + ' ' + entity + ':', error);
                _this.after();
                if (failure) {
                    failure(error);
                }
                else {
                    APP.showError("Errore servizio", "Si e' verificato un errore di servizio.");
                }
            });
        };
        return HttpClient;
    }());
    APP.HttpClient = HttpClient;
    APP.http = new HttpClient();
})(APP || (APP = {}));
var APP;
(function (APP) {
    var Breadcrumb = /** @class */ (function (_super) {
        __extends(Breadcrumb, _super);
        function Breadcrumb(id, classStyle, style, attributes) {
            var _this = _super.call(this, id ? id : '*', 'Breadcrumb', '/', classStyle, style, attributes) || this;
            _this.rootTag = 'nav';
            return _this;
        }
        Breadcrumb.prototype.add = function (link) {
            if (!this.state)
                this.state = [];
            if (link)
                this.state.push(link);
            return this;
        };
        Breadcrumb.prototype.render = function () {
            if (!this.home)
                this.home = '/index.html';
            if (!this._classStyle)
                this._classStyle = 'mb-5 breadcrumb-container';
            if (!this.props)
                this.props = '/';
            var s = this._style ? ' style="' + this._style + '"' : '';
            var a = this._attributes ? ' ' + this._attributes : '';
            var r = '<nav class="' + this._classStyle + '" aria-label="breadcrumb"' + s + a + '>';
            r += '<ol class="breadcrumb"><li class="breadcrumb-item"><a href="' + this.home + '">Homepage</a><span class="separator">/</span></li>';
            if (this.state) {
                for (var _i = 0, _a = this.state; _i < _a.length; _i++) {
                    var l = _a[_i];
                    if (!l)
                        continue;
                    if (l[0] == '<') {
                        r += '<li class="breadcrumb-item">' + l + '</li>';
                    }
                    else {
                        r += '<li class="breadcrumb-item"><a href="#">' + l + '</a></li>';
                    }
                }
            }
            r += '</ol></nav>';
            return r;
        };
        return Breadcrumb;
    }(WUX.WComponent));
    APP.Breadcrumb = Breadcrumb;
    // Props: Numero di pagine
    // State: Pagina corrente
    var ResPages = /** @class */ (function (_super) {
        __extends(ResPages, _super);
        function ResPages(id, classStyle, style, attributes) {
            var _this = _super.call(this, id ? id : '*', 'ResPages', 0, classStyle, style, attributes) || this;
            _this.rootTag = 'nav';
            _this.max = 5;
            return _this;
        }
        ResPages.prototype.refresh = function (rows, lim, tot, curr) {
            // rows non viene utilizzato, ma viene lasciato in caso lo si voglia mostrare 
            if (!tot)
                return this.clear();
            var pages = Math.floor(tot / lim);
            var rem = tot % lim;
            if (rem > 0)
                pages++;
            this.state = curr;
            this.props = pages;
            this.forceUpdate();
            return this;
        };
        ResPages.prototype.clear = function () {
            this.state = 0;
            this.props = 0;
            this.forceUpdate();
            return this;
        };
        ResPages.prototype.updateState = function (nextState) {
            if (this.state) {
                var ap = document.getElementById(this.id + '-' + this.state);
                if (ap) {
                    WUX.removeClassOf(ap.parentElement, 'active');
                }
            }
            if (!nextState)
                nextState = 1;
            _super.prototype.updateState.call(this, nextState);
            var an = document.getElementById(this.id + '-' + nextState);
            if (an) {
                WUX.addClassOf(an.parentElement, 'active');
            }
        };
        ResPages.prototype.componentDidMount = function () {
            var _this = this;
            if (this.props < 1) {
                this.root.innerHTML = '<nav id="' + this.id + '" class="pagination-wrapper" aria-label="Paginazione"></nav>';
                return;
            }
            var r = '<nav id="' + this.id + '" class="pagination-wrapper" aria-label="Paginazione">';
            r += '<ul class="pagination">';
            if (this.state == 1) {
                r += '<li class="page-item disabled">' + this.getBtnPrev() + '</li>';
            }
            else {
                r += '<li class="page-item">' + this.getBtnPrev() + '</li>';
            }
            if (this.max > 0 && this.props > this.max) {
                var b = this.state - (this.max - 2);
                if (b < 2) {
                    b = 1;
                }
                else {
                    var x = this.props - this.max + 1;
                    if (b > x)
                        b = x;
                }
                for (var i = b; i < b + this.max; i++) {
                    r += this.getPageItem(i, i == this.state);
                }
            }
            else {
                for (var i = 1; i <= this.props; i++) {
                    r += this.getPageItem(i, i == this.state);
                }
            }
            if (this.state == this.props) {
                r += '<li class="page-item disabled">' + this.getBtnNext() + '</li>';
            }
            else {
                r += '<li class="page-item">' + this.getBtnNext() + '</li>';
            }
            r += '</ul></nav>';
            this.root.innerHTML = r;
            var ap = document.getElementById(this.id + '-p');
            if (ap) {
                ap.addEventListener("click", function (e) {
                    var cs = _this.state;
                    if (cs > 1)
                        cs--;
                    _this.setState(cs);
                });
            }
            var an = document.getElementById(this.id + '-n');
            if (an) {
                an.addEventListener("click", function (e) {
                    var cs = _this.state;
                    if (cs < _this.props)
                        cs++;
                    _this.setState(cs);
                });
            }
            var _loop_1 = function (i) {
                var a = document.getElementById(this_1.id + '-' + i);
                if (!a)
                    return "continue";
                a.addEventListener("click", function (e) {
                    _this.setState(i);
                });
            };
            var this_1 = this;
            for (var i = 1; i <= this.props; i++) {
                _loop_1(i);
            }
        };
        ResPages.prototype.getBtnPrev = function () {
            return '<button id="' + this.id + '-p" class="page-link" aria-label="Precedente"><span aria-hidden="true"><i class="fa fa-angle-left fa-lg"></i></span><span class="sr-only">Precedente</span></button>';
        };
        ResPages.prototype.getBtnNext = function () {
            return '<button id="' + this.id + '-n" class="page-link" aria-label="Successiva"><span aria-hidden="true"><i class="fa fa-angle-right fa-lg"></i></span><span class="sr-only">Successiva</span></button>';
        };
        ResPages.prototype.getPageItem = function (i, a, t) {
            if (!t)
                t = '' + i;
            if (a)
                return '<li id="' + this.id + '-' + i + '" class="page-item" style="cursor:pointer;"><button aria-current="true" class="page-link" title="Pagina ' + i + '">' + t + '</button></i>';
            return '<li id="' + this.id + '-' + i + '" class="page-item" style="cursor:pointer;"><button class="page-link" title="Pagina ' + i + '">' + t + '</button></i>';
        };
        return ResPages;
    }(WUX.WComponent));
    APP.ResPages = ResPages;
    // Props: Numero di pagine
    // State: Pagina corrente
    var BtnPages = /** @class */ (function (_super) {
        __extends(BtnPages, _super);
        function BtnPages(id) {
            return _super.call(this, id ? id : '*', 'BtnPages') || this;
        }
        BtnPages.prototype.refresh = function (page, pages) {
            this.state = page;
            this.props = pages;
            this.forceUpdate();
            return;
        };
        BtnPages.prototype.updateState = function (n) {
            var p = document.getElementById(this.id + '-' + this.state);
            if (p)
                WUX.removeClassOf(p, 'active');
            _super.prototype.updateState.call(this, n);
            var a = document.getElementById(this.id + '-' + this.state);
            if (a)
                WUX.addClassOf(a, 'active');
        };
        BtnPages.prototype.render = function () {
            if (!this.props) {
                return '<div id="' + this.id + '"></div>';
            }
            if (!this.state)
                this.state = 1;
            var items = '';
            for (var i = 1; i <= this.props; i++) {
                items += '<li><a id="' + this.id + '-' + i + '" class="list-item" style="cursor:pointer;">Pagina ' + i + '</a></li>';
            }
            var r = '<div id="' + this.id + '" class="page-dropdown dropdown">';
            r += APP.dropdownBtn(this.id, 'Pagina ' + this.state + ' di ' + this.props, items);
            r += '</div>';
            return r;
        };
        BtnPages.prototype.componentDidMount = function () {
            var _this = this;
            var a = document.getElementById(this.id + '-' + this.state);
            if (a)
                WUX.addClassOf(a, 'active');
            var _loop_2 = function (i) {
                var a_1 = document.getElementById(this_2.id + '-' + i);
                if (!a_1)
                    return "continue";
                a_1.addEventListener("click", function (e) {
                    console.log('[BtnPages] click (i=' + i + ')', e);
                    _this.setState(i);
                });
            };
            var this_2 = this;
            for (var i = 1; i <= this.props; i++) {
                _loop_2(i);
            }
        };
        return BtnPages;
    }(WUX.WComponent));
    APP.BtnPages = BtnPages;
    // State: numero di elementi per pagina
    var BtnItems = /** @class */ (function (_super) {
        __extends(BtnItems, _super);
        function BtnItems(id) {
            var _this = _super.call(this, id ? id : '*', 'BtnItems') || this;
            _this.IPP = [5, 10, 20, 50, 100];
            _this.state = _this.IPP[0];
            return _this;
        }
        BtnItems.prototype.render = function () {
            if (!this.state)
                this.state = this.IPP[0];
            var items = '';
            for (var i = 0; i < this.IPP.length; i++) {
                var v = this.IPP[i];
                items += '<li><a id="' + this.id + '-' + v + '" class="list-item" style="cursor:pointer;">' + v + ' elementi</a></li>';
            }
            var r = '<div class="page-dropdown dropdown">';
            r += APP.dropdownBtn(this.id, 'Elementi per Pagina', items);
            r += '</div>';
            return r;
        };
        BtnItems.prototype.updateState = function (n) {
            var p = document.getElementById(this.id + '-' + this.state);
            if (p)
                WUX.removeClassOf(p, 'active');
            _super.prototype.updateState.call(this, n);
            var a = document.getElementById(this.id + '-' + this.state);
            if (a)
                WUX.addClassOf(a, 'active');
        };
        BtnItems.prototype.componentDidMount = function () {
            var _this = this;
            var a = document.getElementById(this.id + '-' + this.state);
            if (a)
                WUX.addClassOf(a, 'active');
            var _loop_3 = function (i) {
                var v = this_3.IPP[i];
                var a_2 = document.getElementById(this_3.id + '-' + v);
                if (!a_2)
                    return "continue";
                a_2.addEventListener("click", function (e) {
                    console.log('[BtnItems] click (v=' + v + ')', e);
                    _this.setState(v);
                });
            };
            var this_3 = this;
            for (var i = 0; i < this.IPP.length; i++) {
                _loop_3(i);
            }
        };
        return BtnItems;
    }(WUX.WComponent));
    APP.BtnItems = BtnItems;
})(APP || (APP = {}));
var APP;
(function (APP) {
    var action = WUX.action;
    var getAction = WUX.getAction;
    var WUtil = WUX.WUtil;
    var GUIComuni = /** @class */ (function (_super) {
        __extends(GUIComuni, _super);
        function GUIComuni() {
            var _this = _super.call(this) || this;
            _this.dlg = new DlgComune(_this.subId('dlg'));
            _this.dlg.onHiddenModal(function (e) {
                if (!_this.dlg.ok)
                    return;
                var a = _this.dlg.getProps();
                var s = _this.dlg.getState();
                if (!a || !s)
                    return;
                console.log('dlg action,state', a, s);
            });
            return _this;
        }
        GUIComuni.prototype.render = function () {
            var _this = this;
            this.brcr = new APP.Breadcrumb();
            this.brcr.add('Comuni');
            this.form = new WUX.WFormPanel(this.subId('form'));
            this.form
                .addRow()
                .addTextField('name', 'Denominazione', false, true);
            this.btnFind = new WUX.WButton(this.subId('btnFind'), 'Esegui ricerca', 'fa-search', 'btn-icon btn btn-primary', 'margin-right: 0.5rem;');
            this.btnFind.on('click', function (e) {
                _this.doFind();
            });
            var h = ['Identificativo', 'Denominazione', 'Cod. Fiscale', 'Provincia', 'Vedi', 'Modifica'];
            var k = ['idComune', 'descrizione', 'fiscale', 'provincia', '_v', '_m'];
            this.table = new WUX.WTable(this.subId('tapp'), h, k);
            this.table.selectionMode = 'single';
            this.table.div = 'table-responsive';
            this.table.types = ['s', 's', 's', 's', 'w', 'w'];
            this.table.sortable = [1, 3];
            this.table.on('click', function (e) {
                var a = getAction(e, _this);
                console.log('click a=', a);
                if (!a || !a.ref)
                    return;
                var s = _this.table.getState();
                var x = WUtil.indexOf(s, 'idComune', a.ref);
                if (x < 0)
                    return;
                _this.dlg.setProps(a.name);
                _this.dlg.setState(s[x]);
                _this.dlg.show(_this);
            });
            this.table.onDoubleClick(function (e) {
                _this.dlg.setProps('view');
                _this.dlg.setState(e.data);
                _this.dlg.show(_this);
            });
            this.btnReset = new WUX.WButton(this.subId('btnReset'), 'Annulla', 'fa-undo', 'btn-icon btn btn-secondary');
            this.btnReset.on('click', function (e) {
                _this.form.clear();
                _this.table.setState([]);
                _this.form.focus();
            });
            this.main = new WUX.WContainer();
            this.main
                .before(this.brcr)
                .addRow()
                .addCol('col-md-12')
                .add(this.form)
                .addRow('form-row justify-content-end')
                .add(this.btnFind)
                .add(this.btnReset)
                .addRow()
                .addCol('col-md-12')
                .add(this.table);
            return this.main;
        };
        GUIComuni.prototype.doFind = function () {
            var _this = this;
            APP.http.get('comuni', this.form.getState(), function (data) {
                if (!data || !data.length) {
                    APP.showWarning('Nessun elemento presente');
                    return;
                }
                for (var _i = 0, data_1 = data; _i < data_1.length; _i++) {
                    var record = data_1[_i];
                    record["_v"] = action('view', record["idComune"], 'fa-search');
                    record["_m"] = action('edit', record["idComune"], 'fa-edit');
                }
                _this.table.setState(data);
            });
        };
        return GUIComuni;
    }(WUX.WComponent));
    APP.GUIComuni = GUIComuni;
    var DlgComune = /** @class */ (function (_super) {
        __extends(DlgComune, _super);
        function DlgComune(id) {
            var _this = _super.call(this, id, 'DlgComune') || this;
            _this.title = 'Comune';
            _this.fp = new WUX.WFormPanel(_this.subId('fp'));
            _this.fp.addRow();
            _this.fp.addTextField('idComune', 'Identificativo', true);
            _this.fp.addRow();
            _this.fp.addTextField('fiscale', 'Codice Fiscale');
            _this.fp.addRow();
            _this.fp.addTextField('provincia', 'Provincia');
            _this.fp.addRow();
            _this.fp.addTextField('descrizione', 'Denominazione');
            _this.fp.addRow();
            _this.fp.addTextField('idRegione', 'Codice Regione');
            _this.body
                .addRow()
                .addCol('col-md-12')
                .add(_this.fp);
            return _this;
        }
        DlgComune.prototype.updateState = function (nextState) {
            this.state = nextState;
            if (this.fp) {
                this.fp.setState(this.state);
            }
        };
        DlgComune.prototype.getState = function () {
            if (this.fp) {
                this.state = this.fp.getState();
            }
            return this.state;
        };
        DlgComune.prototype.onClickOk = function () {
            return true;
        };
        DlgComune.prototype.onShown = function () {
            var _this = this;
            setTimeout(function () { _this.fp.focusOn('fiscale'); });
        };
        DlgComune.prototype.clear = function () {
            if (this.fp) {
                this.fp.clear();
            }
            this.state = null;
        };
        return DlgComune;
    }(WUX.WDialog));
    APP.DlgComune = DlgComune;
})(APP || (APP = {}));
var APP;
(function (APP) {
    var GUIDocumenti = /** @class */ (function (_super) {
        __extends(GUIDocumenti, _super);
        function GUIDocumenti() {
            return _super.call(this) || this;
        }
        GUIDocumenti.prototype.render = function () {
            this.brcr = new APP.Breadcrumb();
            this.brcr.add('Documenti');
            this.main = new WUX.WContainer();
            this.main
                .before(this.brcr)
                .addRow()
                .addCol('col-md-12')
                .add('<p>Documentazione tecnica</p>');
            return this.main;
        };
        return GUIDocumenti;
    }(WUX.WComponent));
    APP.GUIDocumenti = GUIDocumenti;
})(APP || (APP = {}));
var APP;
(function (APP) {
    var GUIInfo = /** @class */ (function (_super) {
        __extends(GUIInfo, _super);
        function GUIInfo() {
            return _super.call(this) || this;
        }
        GUIInfo.prototype.render = function () {
            this.brcr = new APP.Breadcrumb();
            this.brcr.add('Informazioni');
            this.main = new WUX.WContainer();
            this.main
                .before(this.brcr)
                .addRow()
                .addCol('col-md-12')
                .add('<p>Informazioni di sistema</p>');
            return this.main;
        };
        return GUIInfo;
    }(WUX.WComponent));
    APP.GUIInfo = GUIInfo;
})(APP || (APP = {}));
var APP;
(function (APP) {
    var GUILogs = /** @class */ (function (_super) {
        __extends(GUILogs, _super);
        function GUILogs() {
            return _super.call(this) || this;
        }
        GUILogs.prototype.render = function () {
            this.brcr = new APP.Breadcrumb();
            this.brcr.add('Log');
            this.main = new WUX.WContainer();
            this.main
                .before(this.brcr)
                .addRow()
                .addCol('col-md-12')
                .add('<p>Consultazione log operazioni</p>');
            return this.main;
        };
        return GUILogs;
    }(WUX.WComponent));
    APP.GUILogs = GUILogs;
})(APP || (APP = {}));
