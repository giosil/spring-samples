declare namespace APP {
    function showInfo(m: string, title?: string): void;
    function showSuccess(m: string, title?: string): void;
    function showWarning(m: string, title?: string): void;
    function showError(m: string, title?: string): void;
    function dropdownBtn(id: string, t: string, items: string, cls?: string): string;
}
declare namespace APP {
    class HttpClient {
        url: string;
        mres: {
            [key: string]: any;
        };
        mock: boolean;
        auth: string;
        constructor(url?: string, auth?: string);
        before(): void;
        after(): void;
        sim(method: string, entity: string, params: any, success: (result: any) => void, failure?: (error: any) => void): void;
        get(entity: string, params: {
            [key: string]: any;
        }, success: (result: any) => void, failure?: (error: any) => void): void;
        delete(entity: string, params: {
            [key: string]: any;
        }, success: (result: any) => void, failure?: (error: any) => void): void;
        post(entity: string, data: object, success: (result: any) => void, failure?: (error: any) => void): void;
        put(entity: string, data: object, success: (result: any) => void, failure?: (error: any) => void): void;
        patch(entity: string, data: object, success: (result: any) => void, failure?: (error: any) => void): void;
        _get(method: string, entity: string, params: {
            [key: string]: any;
        }, success: (result: any) => void, failure?: (error: any) => void): void;
        _send(method: string, entity: string, data: object, success: (result: any) => void, failure?: (error: any) => void): void;
    }
    let http: HttpClient;
}
declare namespace APP {
    class Breadcrumb extends WUX.WComponent<string, string[]> {
        home: string;
        constructor(id?: string, classStyle?: string, style?: string | WUX.WStyle, attributes?: string | object);
        add(link: string): this;
        render(): string;
    }
    class ResPages extends WUX.WComponent<number, number> {
        max: number;
        constructor(id?: string, classStyle?: string, style?: string | WUX.WStyle, attributes?: string | object);
        refresh(rows: number, lim: number, tot: number, curr: number): this;
        clear(): this;
        protected updateState(nextState: number): void;
        protected componentDidMount(): void;
        getBtnPrev(): string;
        getBtnNext(): string;
        getPageItem(i: number, a?: boolean, t?: string): string;
    }
    class BtnPages extends WUX.WComponent<number, number> {
        constructor(id?: string);
        refresh(page: number, pages: number): void;
        protected updateState(n: number): void;
        render(): string;
        protected componentDidMount(): void;
    }
    class BtnItems extends WUX.WComponent<number, number> {
        IPP: number[];
        constructor(id?: string);
        render(): string;
        protected updateState(n: number): void;
        protected componentDidMount(): void;
    }
}
declare namespace APP {
    class GUIComuni extends WUX.WComponent {
        main: WUX.WContainer;
        brcr: Breadcrumb;
        form: WUX.WFormPanel;
        btnFind: WUX.WButton;
        btnReset: WUX.WButton;
        table: WUX.WTable;
        dlg: DlgComune;
        constructor();
        render(): WUX.WContainer;
        doFind(): void;
    }
    class DlgComune extends WUX.WDialog<string, Comune> {
        fp: WUX.WFormPanel;
        constructor(id: string);
        updateState(nextState: Comune): void;
        getState(): Comune;
        onClickOk(): boolean;
        protected onShown(): void;
        clear(): void;
    }
}
declare namespace APP {
    class GUIDocumenti extends WUX.WComponent {
        main: WUX.WContainer;
        brcr: Breadcrumb;
        constructor();
        render(): WUX.WContainer;
    }
}
declare namespace APP {
    class GUIInfo extends WUX.WComponent {
        main: WUX.WContainer;
        brcr: Breadcrumb;
        constructor();
        render(): WUX.WContainer;
    }
}
declare namespace APP {
    class GUILogs extends WUX.WComponent {
        main: WUX.WContainer;
        brcr: Breadcrumb;
        constructor();
        render(): WUX.WContainer;
    }
}
declare namespace APP {
    class Mock {
        dat: {
            [coll: string]: any[];
        };
        seq: {
            [coll: string]: number;
        };
        constructor();
        clean(coll: string): void;
        clear(): void;
        inc(coll: string): number;
        find(coll: string, filter?: any): any[];
        ins(coll: string, ent: any, key?: string): any;
        upd(coll: string, ent: any, key: string): any;
        del(coll: string, val: any, key: string): boolean;
        read(coll: string, key: string, val: any): any;
        protected match(rec: any, flt: any): boolean;
        protected norm(coll: string): string;
    }
}
declare namespace APP {
    interface Comune {
        idComune: string;
        fiscale?: string;
        provincia?: string;
        descrizione: string;
        idRegione: string;
    }
}
