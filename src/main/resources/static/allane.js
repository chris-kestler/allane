/**
 * Why is there so much Javascript?
 *
 * I was over ambitious and wanted one webpage to do everything and intregrate WebSockets to support multiple users or tabs
 *
 * I ran into issues and did not want to devote further time to the frontend when this is a backend position
 *
 * Although it should be noted this file mostly does not contain references contracts/customers/vehicles.
 * The only exception is the hacky PARAM and NAV() functions which were a last minute workaround to salvage my prior work
 * The original goal was to keep the code separated from the database model and business logic
 */


/**
 * This section is for loading page arguments, there are three total
 *
 * op - the operation this page will do: edit or browse
 * target - the target table, contracts, customers or vehicles
 * id - this argument is only on op=edit pages to edit a specific row in a database. This can be blank for creating a new row
 */
var PAGE_PARAMS = undefined; // holds params from the page
var PARSED_PARAMS = {};
export function PARAM(key){
    if(PAGE_PARAMS === undefined) {
        PAGE_PARAMS = new URL(window.location.href).searchParams;

        let op = PAGE_PARAMS.get("op");
        PARSED_PARAMS["op"] = op==null ? "browse" : op.toLowerCase()

        let target = PAGE_PARAMS.get("target")
        PARSED_PARAMS["target"] = target==null ? "contracts" : target.toLowerCase();

        let id = PAGE_PARAMS.get("id");
        if(id === undefined || id == null || (op === "browse" && target === "contracts"))
            id = null;
        else
            id = parseInt(id);
        PARSED_PARAMS["id"] = id;
    }
    if(PARSED_PARAMS[key] !== undefined)
        return PARSED_PARAMS[key];
    else
        return PAGE_PARAMS.get(key);
}

export function NAV(op, target, args ={}){
    if(op === "edit")
        args.op = op; // assume browse

    if(target !== "contracts" && target != null)
        args.target = target;

    let argList = [];
    for(let [key,value] of Object.entries(args))
        if(value != null)
            argList.push(key +"="+value);

    return "/?"+argList.join("&");
}


/**
 * Used to retrieve data from the server
 */
export function GET(url, onResponse, onError){
    fetch(url)
        .then(response => {
            // Check if the response status is OK (200)
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }

            return response.json();
        })
        .then(onResponse)
        .catch(onError);
}

export function POST(url, data, onResponse, onError) {

    let request = {
        method: 'POST',
        headers: new Headers({ 'Content-Type': 'application/json' }),
        body: JSON.stringify(data)
    };
    console.log("Sending post "+url +"\n "+JSON.stringify(request));

    fetch(url, request)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json(); // Parse the response as JSON
        })
        .then(onResponse)
        .catch(onError);
}

/**
 * Shorthand function for adding HTML
 *
 * @param tag tag of the element
 * @param parent parent node, can be null
 * @param css css classes, can be null
 * @param content will set innerHTML, can be null
 * @returns the element
 */
export function CREATE(tag, parent, css, content){
    let element = document.createElement(tag);
    if(parent)
        parent.appendChild(element);
    if(content)
        element.innerHTML = content;
    if(css)
        element.className = css;
    return element;
}

/**
 * This class is used to store data when setting up a contract
 */
const COOKIE_KEY = "allane_contract";
export class ContractCookie{
    static #loaded = undefined;

    static setValue(key, value){
        let cookie = this.getLoaded();
        cookie[key] = value;
        localStorage.setItem(COOKIE_KEY, JSON.stringify(this.#loaded));
    }

    static getValue(key){
        return this.getLoaded()[key];
    }

    static getLoaded(){
        if(this.#loaded === undefined){
            this.#loaded = localStorage.getItem(COOKIE_KEY);
            if( this.#loaded == null)
                this.#loaded = {};
            else
                this.#loaded = JSON.parse(this.#loaded);
        }
        return this.#loaded;
    }

    static remove(){
        this.#loaded = undefined;
        localStorage.removeItem(COOKIE_KEY);
    }
}


/**
 * A javascript class that wraps around an html element
 *
 * Originally more helper functions were in here but were eliminated
 */
class Widget{
    #element;

    constructor(element) {
        this.#element = element;
    }

    setHTML(attributes){
        for(let [key, value] of Object.entries(attributes))
            this.element.setAttribute(key, value);
    }

    get element(){
        return this.#element;
    }
}

/**
 *
 */
class FocusWidget extends Widget {
    #errorText;

    constructor(element) {
        super(element);
    }

    setError(error){
        if(this.#errorText)
            this.#errorText.innerText = error;
    }

    addControlRow(cancellable, ...elements){
        let buttonRow = CREATE("div", this.element, "container row form-row button_row");
        buttonRow.style.alignContent = "right";
        buttonRow.style.marginBottom =".5em";


        for(let element of elements){
            element.classList.add("col-1");
            buttonRow.appendChild(element);
        }

        if(cancellable){
            let cancel = CREATE("button",buttonRow,"col-1-auto btn btn-secondary", "Cancel");
            cancel.addEventListener('click', () => window.history.back());

            let home = CREATE("a",buttonRow,"col-auto btn btn-secondary", "Home");
            home.setAttribute("title","Cancel all changes and go to the main page");
            home.setAttribute("href", "/");
        }

        this.#errorText = CREATE("div", buttonRow, "col text-danger actually-align-middle prominent");
    }
}

class InputWidget extends Widget{
    transcriber; parser; validator;

    setValue(object){
        if(this.transcriber !== undefined)
            object = this.transcriber(object);
        else if(object === undefined)
            object = "";
        this.element.value = object;
    }

    getValue(){
        let value = this.element.value;
        if(this.parser !== undefined)
            value = this.parser(value);
        return value;
    }

    /** This was added incase a field needed custom clear code in addition to something in setValue; I did not end up utilizing this */
    clear(){
        this.element.value = "";
    }
}

/**
 *
 */
export class Form extends FocusWidget {
    #inputs = {}
    #divInputs;
    #model = {};
    #target;

    constructor(element, label, target, destination) {
        super(CREATE("form", element, "labelled_box bg-dark needs-validation"));

        label = (PARAM("id") === null ? "Create ":"Edit ") + label;

        this.#target = target;

        element.classList.add("needs-validation");
        element.setAttribute("novalidate", "");

        CREATE("div", this.element, "enclosed_label container prominent", label);

        this.#divInputs = CREATE("div", this.element, "container");

        let submit = CREATE("button", undefined, "col-auto btn btn-primary", "Save")
        submit.setAttribute("type", "submit");
        this.element.addEventListener('submit',  (event) =>{
             // PreventDefault should be called first! This prevents a page refresh!
             // An exception happening before this call will prevent this from running. Was hard to debug.
            event.preventDefault();
            this.element.classList.add('was-validated');

            if (!this.element.checkValidity()) {
                event.stopPropagation();
            } else {
                let result =  this.toJSON();
                POST("/data/"+this.#target+"/add", result, (data)=>{
                    if(data.id != null)
                        ContractCookie.setValue(this.#target, data.id);
                    window.location.assign(destination);
                },this.setError.bind(this));
            }
        }, false);

        this.addControlRow(true, submit);
    }

    #addRow(key, label, tag){
        if(!key)
            key = label.toLowerCase();
        let formKey = "input_"+key;

        let row = CREATE("div", this.#divInputs, "row form-row form-group");

        let labelEle = CREATE("label", row, "col col-2 form-label", label);
        labelEle.setAttribute("for",formKey);

        let input = CREATE(tag, CREATE("div", row, "col"), "form-control");
        input.id = formKey;
        input = new InputWidget(input);
        this.#inputs[key] = input;

        return input;
    }

    get model(){
        return this.#model;
    }

    addField(key, label, type, ...additionalProcess){
        let input = this.#addRow(key, label, "input");
        input.setHTML({"type": type, "required":""});

        if(type === "number")
            input.parser = value => value.toFixed(0);

        if(additionalProcess)
            for(let additional of additionalProcess)
                additional(input.element, input);

        return this;
    }


    /** Used to add the fields that load from another database */
    addForeignField(key, label, transcriber, rows = 1){
        let field = this.#addRow(key, label, "textarea");
        field.element.parentNode.classList.add("input-group");
        field.setHTML({"disabled":"", "rows":rows});
        field.element.style.resize = "none";
        field.transcriber = (value)=>{
            let edited = ContractCookie.getValue(key+"s");
            console.log("grabbing more data("+key+") " +edited+ " current value is "+value?.id);
            if(edited != null && edited != value?.id){
                GET("data/"+key+"s/get/"+edited, (data)=>{
                    field.element.value += " → " + transcriber(data);
                    this.#model[key] = data;
                    field.element.style.backgroundColor = "#b9b852";
                }, () => this.setError("Could not load previously set value"));
            }
            return transcriber(value);
        };

        let buttonParent = field.element.parentNode;

        field.parser = (data) => {
            let result = {};
            let id = this.model[key]?.id;
            if(id === undefined)
                id = null;
            result.id = id;
            console.log("foreign key "+key+" "+  JSON.stringify(result));
            return result;
        }

        //add post button
        let edit = CREATE("button", buttonParent, "btn btn-outline-secondary text-white", "⚙");
        edit.borderBottomLeftRadius = 0;
        edit.borderTopLeftRadius = 0;
        edit.addEventListener('click', ()=> {
            let args = {}
            let rowID = this.#model[key]?.id;
            if(rowID !== undefined)
                args.id = rowID;
            window.location.assign(NAV("edit", key+"s", args));
        });

        let del = CREATE("button", buttonParent, "btn btn-sm btn-outline-secondary text-white", "X");
        del.setAttribute("type","button");
        del.setAttribute("title","This removes this data from this contract; but does not delete the entry in the database");
        del.borderRadius = 0;
        del.addEventListener('click', () => {
            field.element.value = "The "+key+" will be removed from only this contract";
            this.#model[key] = {id: null};
            ContractCookie.setValue(key+"s", undefined);
            field.element.style.backgroundColor = "#b9b852";
        }, this.setError.bind(this));

        let find = CREATE("a", buttonParent, "btn btn-outline-secondary text-white", "🔍");
        find.setAttribute("href",  NAV("browse", key+"s"));
        find.borderBottomLeftRadius = 0;
        find.borderTopLeftRadius = 0;
        return this;
    }

    fill(object){
        this.#model = object;
        console.log("model filled " + JSON.stringify(object));
        for(let [key, input] of Object.entries(this.#inputs))
            input.setValue( this.#model[key]);
    }

    static addEuro(field, widget){
        let prepend = CREATE("div", undefined, "input-group-prepend input-group-text", "€");
        field.parentNode.insertBefore(prepend, field);
        field.parentNode.classList.add("input-group");

        widget.parser = parseFloat;
    }

    static setParser(parser){
        return (field, widget) => widget.parser = parser;
    }

    toJSON(){
        let result = {};
        result.id = this.#model?.id;
        for(let [key, input] of Object.entries(this.#inputs)){
            let value =  input.getValue();
            console.log("to input "+key+ ":"+value);
            result[key] = value;
        }
        return result;
    }
}

/** Used to make a sortable table of database data */
export class Table extends FocusWidget{
    #columnProcessors = []
    #rowProcessors = [];
    #target;
    #table;
    #dataTable;
    #body;
    #headerRow;
    #destination;
    constructor(parent, id, destination = undefined) {
        super(CREATE("div", parent));
        this.#destination = destination;

        id = id.toLowerCase();
        this.#target = id.toLowerCase();

        let createButton = CREATE("a", this.element, "btn btn-secondary", "Create");
        createButton.setAttribute("href", NAV("edit", PARAM("target")));

        this.addControlRow(destination !== undefined, createButton);

        this.#table = CREATE("table", this.element);
        this.#table.id = id.toLowerCase()+"_table";
        let header = CREATE("thead", this.#table);
        this.#headerRow = CREATE("tr", header);
        this.#body = CREATE("tbody", this.#table);
    }

    addColumn(label, processor){
        CREATE("th", this.#headerRow, "", label);

        if(processor === undefined)
            processor = label.toLowerCase();

        if(typeof processor === 'string'){
            this.#columnProcessors.push(function(row){
                return row[processor];
            });
        }else if(typeof processor === 'function'){
            this.#columnProcessors.push(processor);
        }else{
            console.log("Skipping processor for "+label +" "+typeof processor)
        }

        return this;
    }

    /** Populates the data by calling GET. This could be refactored for more code reuse */
    populate(){
        GET('data/'+this.#target+'/get',
                data => {
                console.log(JSON.stringify(data))
                for(let dataRow of data){
                    this.#addRow(dataRow);
                }
                this.#dataTable = new DataTable('#'+this.#table.id);

            },error => {
                console.error('Fetch error:', error);
            });

        // add a button for control
        CREATE("th", this.#headerRow);

        return this;
    }

    /** Used to add a row o the table */
    #addRow(dataRow){
        let row = CREATE("tr", this.#body);
        let ROW_ID = this.#target+ "_"+dataRow.id;
        row.id = ROW_ID;
        for(let processor of this.#columnProcessors){
            let content = processor != null ? processor(dataRow) : "";
            CREATE("td", row, "", content);
        }

        let controls =  CREATE("td", row);
        controls.style.width = "8em";

        let target = PARAM("target");
        let select;
        if(this.#destination !== undefined){
            select = CREATE("button", controls, "btn btn-sm btn-dark btn-outline-secondary", "☑");
            select.addEventListener('click',()=>{
                ContractCookie.setValue(target, dataRow.id);
                window.location.assign(this.#destination);
            });
        }

        let edit = CREATE("a", controls, "btn btn-sm btn-dark btn-outline-secondary", "⚙");
        edit.setAttribute("href",  NAV("edit", this.#target, {id: dataRow.id}));

        let del = CREATE("button", controls, "btn btn-sm btn-outline-secondary text-white", "🗑");
        del.setAttribute("type","button");
        del.borderRadius = 0;
        del.addEventListener('click', ()=>{
            POST("/data/"+this.#target+"/delete", dataRow.id, ()=> this.#dataTable.row('#'+ROW_ID).remove().draw(false), this.setError.bind(this));
        });

        for(let code of this.#rowProcessors)
            code(dataRow, row, select, edit, del);
    }

    /** Allows adding code that runs each time a row created */
    forEachRow(code){
        this.#rowProcessors.push(code);
        return this;
    }
}