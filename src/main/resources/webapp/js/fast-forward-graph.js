// Create a new directed graph
// Globale Variablen für das SVG-Element, den D3-Graph, das Graph-Element, den Zoom und den Renderer
var svg1, g1, inner1, zoom1, render1;
// Variablen für das Datenmodell
var commits1, revisions1, referenceCommits1, branches1;

var tipsy_options = {gravity: 'w', html: true, fade:true, trigger: 'focus', offset: 50, delayOut: 500};

// http://stackoverflow.com/questions/16265123/resize-svg-when-window-is-resized-in-d3-js
var w;
function updateWindow(){
    _svg.attr("width", w.clientWidth);
}
window.onresize = updateWindow;

// jQuery-SVG-Objekt um den Loader zu postionieren
var _svg, spinner1;

$(document).ready(function () {
	_svg = $("#right");
	 w = $("#visualisation-right")[0];
	 updateWindow();
	// Loading Symbol in der Mitte des Graphen einfügen
	spinner1 = jQuery.parseHTML("<div style='position: absolute; top:" + (_svg.position().top + (_svg.attr('height') / 2) - 34) + "px;left:" + (_svg.position().left + (_svg.attr('width') / 2) - 34) + "px;'><i class='fa fa-spinner fa-pulse fa-3x'></i></div>");
	// Loading Symbol ins DOM einfügen
	_svg.after(spinner1);
});




// Center the graph
var center1 = function () {
    // Verhältnis von Graph zur SVG Größe
    var widthscale = svg1.attr("width") / (g1.graph().width + 50);
    var heightscale = svg1.attr("height") / (g1.graph().height + 50);
    // Kleineres Verhältnis zur Anpassung auswählen
    var scale = widthscale < heightscale ? widthscale : heightscale;
    // Graphen nicht größer als doppelt so groß anzeigen
    scale = scale > 2 ? 2 : scale;
    // Graph über D3 mittig positionieren und skalieren
    zoom1
        .translate([(svg1.attr("width") - g1.graph().width * scale) / 2, (svg1.attr("height") - g1.graph().height * scale) / 2])
        .scale(scale)
        .event(svg1);
    // Loader ausblenden
    $(spinner1).hide();
};


function padStr(i) {
    return (i < 10) ? "0" + i : "" + i;
}

// Funktion, die den Inhalt der Revisions-Tooltips erstellt
var revTooltip1 = function (name, node) {
    // Tabelle mit den Detailinformationen der Revision erstellen, Verlinkte Informationen könnten ggfs. asynchron nachgeladen werden
    var tooltip = "<h1>Revision " + node.revisionNumber + "</h1>" +
        "<table class='properties'>" +
        "<tr><td>Number:</td><td>" + node.revisionNumber + "</td><td></td></tr>" +
        "<tr><td>Branch:</td><td>" + branches1[node.revisionOfBranch].label + "</td><td><a href='" + node.revisionOfBranch + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
        "<tr><td>Revised graph:</td><td>" + node.revisionOf + "</td><td><a href='" + node.revisionOf + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
        "<tr><td>URL:</td><td>" + name + "</td><td></td></tr>" +
        "<tr><td>Add Set:</td><td>" + node.deltaAdded + "</td><td><a href='" + node.deltaAdded + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
        "<tr><td>Delete Set:</td><td>" + node.deltaRemoved + "</td><td><a href='" + node.deltaRemoved + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
        "</table></p>";
       
    // Falls die Revision Version 0 ist, kann keine Information zum Commit angegeben werden
    if (node.commit == null) {
        return tooltip;
    }
    // Tabelle mit den Detailinformationen des Commits, der die Revision erstellt hat
    var date = new Date(commits1[node.commit].time);
    tooltip += "<h2>Creating Commits</h2>" +
    "<table class='properties'>" +
    "<tr><td>Title:</td><td colspan='2'>" + commits1[node.commit].title + "</td></tr>" +
    "<tr><td>Time:</td><td>" + padStr(date.getDate()) + "." + padStr((date.getMonth() + 1)) + "." + date.getFullYear() + " " + padStr(date.getHours()) + ":" + padStr(date.getMinutes()) + ":" + padStr(date.getSeconds()) + "</td><td></td></tr>" +
    "<tr><td>User:</td><td>" + commits1[node.commit].wasAssociatedWith + "</td><td><a href='" + commits1[node.commit].wasAssociatedWith + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
    "<tr><td>URL:</i></td><td colspan='2'>" + node.commit + "</td></tr>" + 
    "</table>";
    return tooltip;
};

// Funktion, die den Inhalt der Tag-Tooltips erstellt
var tagTooltip1 = function (node) {
	var date = new Date(node.atTime);
	tooltip = "<h1>Tag " + node.title +"</h1>" +
    "<table class='properties'>" +
    "<tr><td>Title:</td><td colspan='2'>" + node.title + "</td></tr>" +
    "<tr><td>Time:</td><td>" + padStr(date.getDate()) + "." + padStr((date.getMonth() + 1)) + "." + date.getFullYear() + " " + padStr(date.getHours()) + ":" + padStr(date.getMinutes()) + ":" + padStr(date.getSeconds()) + "</td><td></td></tr>" +
    "<tr><td>User:</td><td>" + node.wasAssociatedWith + "</td><td><a href='" + node.wasAssociatedWith + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
    "</table>";
    return tooltip;
};

// Funktion, die die Tags als Knoten mit Kanten erstellt
var createTags1 = function () {
    // Über alle Tags iterieren
    Object.keys(referenceCommits1).forEach(function (referenceCommit) {
        // Alle Daten können im D3 Modell hinterlegt werden
        var value = referenceCommits1[referenceCommit];
        // Konfigurationsparameter setzen
        // Anzeigename
        value.label = referenceCommits1[referenceCommit].title;
        // Höhe
        value.height = 5;
        // Hellblaue Rahmenfarbe
        value.style = "stroke:#60b1fc;";
        // Schriftgröße
        value.labelStyle = "font-size:9px;";
        // Knoten erzeugen
        g1.setNode(referenceCommit, value);
        // Für jede Revision, die der Tag referenziert, muss eine Kante erstellt werden
        referenceCommits1[referenceCommit].used.forEach(function (v) {
            // Kante von der Revision zum Tag erstellen
            g1.setEdge(v, referenceCommit, {
                //Hellblaue, gestrichelte Linie definieren
                style: "stroke:#60b1fc;fill:none;stroke-dasharray: 5, 5;",
                // Hellblauen Pfeil definieren
                arrowheadStyle: "fill:#60b1fc;stroke:#60b1fc;",
                // Rundere Linien
                lineInterpolate: "basis",
                // Mindestabstand 2
                minlen: 2
            });
        });
    });
};

// Funktion, die die Branches als Knoten mit Kanten erstellt
var createBranches1 = function () {
    // Über alle Branches iterieren
    Object.keys(branches1).forEach(function (branch) {
        // Alle Daten können im D3 Modell hinterlegt werden
        var value = branches1[branch];
        // Konfigurationsparameter setzen
        // Anzeigename
        value.label = branches1[branch].label;
        // Höhe
        value.height = 5;
        // In der Farbe des Branches füllen
        value.style = "fill:" + branches1[branch].color + ";stroke:#fff;";
        // Textgröße
        value.labelStyle = "font-size:9px;";
        // Node erstellen
        g1.setNode(branch, value);
        // Zugehörige Linie zur Revision erstellen
        g1.setEdge(branch, branches1[branch].head, {
            // Mindestabstand 1
            minlen: 1,
            // Linie ohne Pfeil
            arrowhead: "undirected",
            // Linie in der Farbe des Branches
            style: "stroke:" + branches1[branch].color + ";fill:none;",
            arrowheadStyle: "fill:" + branches1[branch].color + ";stroke:" + branches1[branch].color + ";",
            // Rundere Linien
            lineInterpolate: "basis"
        });
    });
};

// tipsy-Funktion an Tag-Knoten binden
var bindTags1 = function () {
    // Alle Knoten mit D3 erfassen, Tags herausfiltern. Titel setzen und tipsy binden
    inner1.selectAll("g.node").filter(function (v) {
        return referenceCommits1[v] != null;
    })
        .attr("title", function (v) {
            return tagTooltip1(g1.node(v))
        })
        .each(function (v) {
            $(this).tipsy(tipsy_options);
        });
};

// Funktion um Tags einzublenden
var showTags1 = function () {
    // Loader einblenden
    $(spinner1).show();
    // Tag-Knoten und Kanten erstellen
    createTags1();
    // Graph rendern und ins DOM einbinden
    render1(inner1, g1);
    // tipsy-Funktion an die eingebundenen Knoten binden
    bindTags1();
    // Graph neu skalieren und positionieren
    center1();
};

// Funktion um Tags auszublenden
var hideTags1 = function () {
    // Loader einblenden
    $(spinner1).show();
    // Alle Knoten mit D3 erfassen, Tags herausfiltern und löschen
    inner1.selectAll("g.node").filter(function (v) {
        return referenceCommits1[v] != null;
    })
        .each(function (v) {
            g1.removeNode(v);
        });
    // Graph ohne Tags neu rendern
    render1(inner1, g1);
    // Graph neu skalieren und positionieren
    center1();
};

// Funktion um Branches einzubinden
var showBranches1 = function () {
    // Loader einbinden
    $(spinner1).show();
    // Branch-Knoten und Kanten erstellen
    createBranches1();
    // Graph rendern und in DOM einbinden
    render1(inner1, g1);
    // Graph neu skalieren und positionieren
    center1();
};

// Funktion um Branches auszublenden
var hideBranches1 = function () {
    // Loader einblenden
    $(spinner1).show();
    // Alle Knoten mit D3 erfassen, Branches herausfiltern und löschen
    inner1.selectAll("g.node").filter(function (v) {
        return branches1[v] != null;
    })
        .each(function (v) {
            g1.removeNode(v);
        });
    // Graph ohne Branches neu rendern
    render1(inner1, g1);
    // Graph neu skalieren und positionieren
    center1();
    
};

/** Hauptfunktion, um kompletten Graphen zu erstellen.
 *
 *  _JSON: URL zu JSON-Daten oder Daten selbst
 *  _showBranches: true, wenn Branches mit angezeigt werden sollen (Optional)
 *  _showTags: true, wenn Tags mit angezeigt werden sollen (Optional)
 */
var drawGraph = function (_JSON, _showBranches, _showTags) {
    // Falls _showBranches nicht übergeben wurde, standardmäßig auf false setzen
    _showBranches = _showBranches || false;
    // Falls _showTags nicht übergeben wurden, standardmäßig auf false setzen
    _showTags = _showTags || false;
    // Loader einblenden
    $(spinner1).show();
    // JSON-Daten mit jQuery laden und parsen
    $.getJSON(_JSON, function (data, status) {
        // Prüfen, ob die Daten erfolgreich geladen wurden
        if (status != "success") {
            alert("Error getting JSON Data: Status " + status);
        }
        // Farbpalette für die Branches aus D3-Funktion
        var colors = d3.scale.category10();
        // Zähler initialisieren
        var j = 1, k = 0;
        // D3 Graph neu erstellen
        g1 = new dagreD3.graphlib.Graph();
        g1.setGraph({});
        g1.setDefaultEdgeLabel(function () {
            return {};
        });
        // Falls es bereits einen Graph gab, diesen löschen
        if (svg1 != null) {
           _svg.children("g").remove();
        } else {
            // Sonst über D3 das SVG-Element zuweisen
            svg1 = d3.select("#right");
        }
        // Das Graph-Element in das SVG-Element einfügen
        inner1 = svg1.append("g");
        // Datenvariablen initialisieren
        commits1 = {};
        revisions1 = {};
        referenceCommits1 = {};
        branches1 = {};
        // Hellblau als Farbe verbrauchen, die Farbe geht an die Tags
        colors(0);
        // Über alle Eingangsdaten iterieren (Key-Value-Paare)
        $.each(data, function (key, value) {
            // Array der Datenobjekte mit den Typen laden
            var types = value["http://www.w3.org/1999/02/22-rdf-syntax-ns#type"];
            // Zähler für die Farbzuweisung
            j++;
            // Über alle verschiedenen Typen iterieren und je nach Typ behandeln
            for (var i = 0; i < types.length; i++) {
                switch (types[i].value) {
                    // Falls Commit
                    case "http://eatld.et.tu-dresden.de/rmo#Commit":
                        // Commit intialisieren
                        commits1[key] = {};
                        // Nachricht setzen
                        commits1[key].title = value["http://purl.org/dc/terms/title"][0].value;
                        // ID des Erstellers hinterlegen
                        commits1[key].wasAssociatedWith = value["http://www.w3.org/ns/prov#wasAssociatedWith"][0].value;
                        // ID der Revision, die der Commit erstellt hat setzen
                        commits1[key].generated = value["http://www.w3.org/ns/prov#generated"][0].value;
                        // Alle IDs der Revisionen, die in den Commit eingeflossen sind setzen (length > 1 --> Merge)
                        commits1[key].used = [];
                        for (k = 0; k < value["http://www.w3.org/ns/prov#used"].length; k++) {
                            commits1[key].used.push(value["http://www.w3.org/ns/prov#used"][k].value);
                        }
                        // Zeitstempel, der für den Commit hinterlegt sind setzen
                        commits1[key].time = value["http://www.w3.org/ns/prov#atTime"][0].value;
                        // Commit in der Revision hinterlegen, die aus dem Commit hervorging. Falls es die Revision im Datenmodell noch nicht gibt muss sie erstellt werden
                        if (revisions1[commits1[key].generated] == null) {
                            revisions1[commits1[key].generated] = {};
                        }
                        revisions1[commits1[key].generated].commit = key;
                        break;
                    // Falls Revision
                    case "http://eatld.et.tu-dresden.de/rmo#Revision":
                        // Falls die Revision noch nicht im Datenmodell existiert muss sie initialisiert werden
                        if (revisions1[key] == null) {
                            revisions1[key] = {};
                        }
                        // ID der entfernten Daten setzen
                        revisions1[key].deltaRemoved = value["http://eatld.et.tu-dresden.de/rmo#deltaRemoved"][0].value;
                        // ID der hinzugefügten Daten setzen
                        revisions1[key].deltaAdded = value["http://eatld.et.tu-dresden.de/rmo#deltaAdded"][0].value;
                        // Revisionsnummer setzen
                        revisions1[key].revisionNumber = value["http://eatld.et.tu-dresden.de/rmo#revisionNumber"][0].value;
                        // ID der eigentlichen Daten setzen
                        revisions1[key].revisionOf = value["http://eatld.et.tu-dresden.de/rmo#revisionOf"][0].value;
                        // ID des Branches setzen, zu dem die Revision gehört
                        revisions1[key].revisionOfBranch = value["http://eatld.et.tu-dresden.de/rmo#revisionOfBranch"][0].value;
                        break;
                    // Falls Branch
                    case "http://eatld.et.tu-dresden.de/rmo#Branch":
                        // Falls der Branch noch nicht im Datenmodell existiert muss er initialisiert werden
                        if (branches1[key] == null) {
                            branches1[key] = {};
                        }
                        // Name des Branch setzen
                        branches1[key].label = value["http://www.w3.org/2000/01/rdf-schema#label"][0].value;
                        // ID der eigentlichen Daten setzen
                        branches1[key].fullGraph = value["http://eatld.et.tu-dresden.de/rmo#fullGraph"][0].value;
                        // ID der Revision am Kopf des Branches setzen
                        branches1[key].head = value["http://eatld.et.tu-dresden.de/rmo#references"][0].value;
                        // Falls noch keine Farbe für den Branch definiert ist, eine aus D3 laden
                        if (branches1[key].color == null) {
                            branches1[key].color = d3.rgb(colors(j)).brighter().toString();
                        }
                        break;
                    // Falls Tag
                    case "http://eatld.et.tu-dresden.de/rmo#ReferenceCommit":
                        // Tag initialisieren
                        referenceCommits1[key] = {};
                        // Name des Tags setzen
                        referenceCommits1[key].title = value["http://purl.org/dc/terms/title"][0].value;
                        // ID des Ersteller setzen
                        referenceCommits1[key].wasAssociatedWith = value["http://www.w3.org/ns/prov#wasAssociatedWith"][0].value;
                        // Array für Zeitstempel initialisieren
                        referenceCommits1[key].atTime = [];
                        // Array für Revisionen, die der Tag refereziert
                        referenceCommits1[key].used = [];
                        // Alle Zeitstempel laden
                        for (k = 0; k < value["http://www.w3.org/ns/prov#atTime"].length; k++) {
                            referenceCommits1[key].atTime.push(value["http://www.w3.org/ns/prov#atTime"][k].value);
                        }
                        // Alle referenzierten Revisionen laden
                        for (k = 0; k < value["http://www.w3.org/ns/prov#used"].length; k++) {
                            referenceCommits1[key].used.push(value["http://www.w3.org/ns/prov#used"][k].value);
                        }
                        break;
                    // Falls Master
                    case "http://eatld.et.tu-dresden.de/rmo#Master":
                        // Falls der Masterbranch noch nicht als Branch vorliegt, muss er initialisiert werden
                        if (branches1[key] == null) {
                            branches1[key] = {};
                        }
                        // Alle Masterbranches sollen immer die gleiche Farbe haben
                        branches1[key].color = "#5555ff";
                        break;

                }
            }

        });
        // Einstellungen für die Abstände zwischen den Knoten, Kanten und Level
        g1.graph().ranksep = 10;
        g1.graph().nodesep = 10;
        g1.graph().edgesep = 10;
        g1.graph().rankdir = "BT";
        // Revisionen aus dem Datenmodell als Knoten erstellen
        // Dazu über alle Revisionen iterieren
        Object.keys(revisions1).forEach(function (revision) {
            // Alle Daten können im D3 Modell hinterlegt werden
            var value = revisions1[revision];
            // Konfigurationsparameter setzen
            // Falls die Revisionsnummer für den Kreis zu lang ist, muss sie gekürzt werden
            if (revisions1[revision].revisionNumber.length > 5) {
                // Wenn gekürzt werden muss, bleiben die ersten drei Zeichen und das letzte stehen, der Rest wird durch .. erstetzt
                value.label = revisions1[revision].revisionNumber.substr(0, 3) + ".." + revisions1[revision].revisionNumber.substr(revisions1[revision].revisionNumber.length - 1);
            } else {
                // Sonst die ganze Revisionsnummer als Anzeigenamen setzen
                value.label = revisions1[revision].revisionNumber;
            }
            // Höhe setzen
            value.height = 30;
            // Revisionen werden als Kreise dargestellt
            value.shape = "circle";
            // Falls die Revision am Kopf des Branches steht
            if (branches1[revisions1[revision].revisionOfBranch].head == revision) {
                // Wird der Kreis komplett in der Branchfarbe gefüllt
                value.style = "fill:" + branches1[revisions1[revision].revisionOfBranch].color + ";stroke:none;";
            } else {
                // Sonst erhält er nur einen Rand in Branchfarbe
                value.style = "stroke:" + branches1[revisions1[revision].revisionOfBranch].color + ";";
            }
            // Knoten erzeugen
            g1.setNode(revision, value);
        });
        // Commits aus dem Datenmodell als Kanten erstellen
        Object.keys(commits1).forEach(function (commit) {
            // Für jede Revision, aus der der Commit erzeugt wurde muss eine Kante erstellt werden
            for (var i = 0; i < commits1[commit].used.length; i++) {
                // Farbe für die Kante
                var color;
                // Falls der Commit nur von einer Revision stammt
                if (commits1[commit].used.length == 1) {
                    // Wird als Farbe für die Kante die Revision genommen, die der Commit erzeugt hat
                    color = branches1[revisions1[commits1[commit].generated].revisionOfBranch].color;
                } else {
                    // Ansonsten die Farbe der Ursprungsrevision
                    color = branches1[revisions1[commits1[commit].used[i]].revisionOfBranch].color;
                }
                // Kante von der Ursprungsrevision zur Revision, die der Commit erzeugt hat, erzeugen
                g1.setEdge( commits1[commit].generated, commits1[commit].used[i], {
                    // Mindestabstand 4
                    minlen: 4,
                    // Farben setzen
                    style: "stroke:" + color + ";fill:none;",
                    arrowheadStyle: "fill:" + color + ";stroke:" + color + ";",
                    // Rundere Linien
                    lineInterpolate: "basis"
                });
            }
        });
        // Falls erforderlich, Branches zum Anzeigen erzeugen
        if (_showBranches) {
            createBranches1();
        }
        // Falls erforderlich, Tags zum Anzeigen erzeugen
        if (_showTags) {
            createTags1();
        }

        // Renderer erzeugen
        render1 = new dagreD3.render();

        // Zoom aus D3 und Verschiebung des Graphen über D3 ermöglichen
        zoom1 = d3.behavior.zoom().on("zoom", function () {
            inner1.attr("transform", "translate(" + d3.event.translate + ")" +
            "scale(" + d3.event.scale + ")");
        });

        // Zoom an dei SVG binden
        svg1.call(zoom1);

        // Graphen rendern und ins DOM einfügen
        render1(inner1, g1);
        // tipsy-Funktion an Revisionsknoten binden
        // Dazu alle Knoten über D3 auswählen, Revisionen herausfinden. Titel setzen und tipsy binden
        inner1.selectAll("g.node").filter(function (v) {
            return revisions1[v] != null;
        })
            .attr("title", function (v) {
                return revTooltip1(v, g1.node(v))
            })
            // mit $.fn.tipsy.autoNS sorgt dafür, dass Tooltips zu dicht am oberen oder unteren Rand passend dargestellt werden
            .each(function (v) {
                $(this).tipsy(tipsy_options);
            });
        // Bei Bedarf auch tipsy an die Tags binden
        if (_showTags) {
            bindTags1();
        }
        // Graph skalieren und positionieren
        center1();
    });
      
};

