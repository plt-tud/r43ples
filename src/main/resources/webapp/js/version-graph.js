// Create a new directed graph
/** Hauptfunktion, um kompletten Graphen zu erstellen.
 *
 *  _JSON: URL zu JSON-Daten oder Daten selbst

 */
function drawGraph(div_selector, _JSON, _showBranches, _showTags) {
	// Falls _showBranches nicht Ã¼bergeben wurde, standardmÃ¤ÃŸig auf false setzen
    _showBranches = _showBranches || false;
    // Falls _showTags nicht Ã¼bergeben wurden, standardmÃ¤ÃŸig auf false setzen
    _showTags = _showTags || false;
    
    var g;
    var svg, inner;
	var div_element = $(div_selector);
    // Farbpalette fÃ¼r die Branches aus D3-Funktion
    var colors = d3.scale.category10();
    // Datenvariablen initialisieren
    var commits = {};
    var revisions = {};
    var referenceCommits = {};
    var branches = {};
    
    
    
    
	div_element.html(
		"<div class='revisionGraphVisualisation'>" +
		"	<svg><g/></svg>" +
		"</div>" +
		"<div class='form'>" +
        "	<label class='checkbox-inline'><input type='checkbox' class='checkbox-inline toggle-tags'>Tags</label>" +
        "	<label class='checkbox-inline'><input type='checkbox' class='checkbox-inline toggle-branches'>Branches</label>" +
	  	"</div>");

	// ChangeListener for tags
	$('.toggle-tags').change(function () {
	    $(this).prop('checked')?showTags():hideTags();
	});

	//ChangeListener for branches
	$('.toggle-branches').change(function () {
	    $(this).prop('checked')?showBranches():hideBranches();
	});

	
	// http://stackoverflow.com/questions/16265123/resize-svg-when-window-is-resized-in-d3-js
	var svg_element = div_element.find('svg');
	function resizeSVG(){
	    svg_element.width(div_element.width());
	}
	window.onresize = resizeSVG;	 
	resizeSVG();

	// Loading Symbol in der Mitte des Graphen einfÃ¼gen
	SPINNER_WIDTH = SPINNER_HEIGHT = 60;
	var spinner_html = jQuery.parseHTML(
		"<div class='spinner' style='position: absolute; " +
					"top:"  + (svg_element.position().top  + (svg_element.height()-SPINNER_HEIGHT)/2) + "px;" +
					"left:" + (svg_element.position().left + (svg_element.width() -SPINNER_WIDTH )/2) + "px;'>" +
			"<i class='fa fa-spinner fa-pulse fa-3x'></i>" +
		"</div>");
	// Loading Symbol ins DOM einfÃ¼gen
	svg_element.after(spinner_html);
	var spinner = div_element.find('.spinner');

    // JSON-Daten mit jQuery laden und parsen
    $.getJSON(_JSON, function (data, status) {
        // PrÃ¼fen, ob die Daten erfolgreich geladen wurden
        if (status != "success") {
            alert("Error getting JSON Data: Status " + status);
        }

        // D3 Graph neu erstellen
        g = new dagreD3.graphlib.Graph();
        g.setGraph({});
        g.setDefaultEdgeLabel(function () {
            return {};
        });
        // Einstellungen fÃ¼r die AbstÃ¤nde zwischen den Knoten, Kanten und Level
        g.graph().ranksep = 10;
        g.graph().nodesep = 10;
        g.graph().edgesep = 10;


        // Hellblau als Farbe verbrauchen, die Farbe geht an die Tags
        colors(0);
		
		create_revision_model(data);


        // Revisionen aus dem Datenmodell als Knoten erstellen
        // Dazu Ã¼ber alle Revisionen iterieren
        Object.keys(revisions).forEach(function (revision) {
            // Alle Daten kÃ¶nnen im D3 Modell hinterlegt werden
            var value = revisions[revision];
            // Konfigurationsparameter setzen
            // Falls die Revisionsnummer fÃ¼r den Kreis zu lang ist, muss sie gekÃ¼rzt werden
            if (revisions[revision].revisionNumber.length > 5) {
                // Wenn gekÃ¼rzt werden muss, bleiben die ersten drei Zeichen und das letzte stehen, der Rest wird durch .. erstetzt
                value.label = revisions[revision].revisionNumber.substr(0, 3) + ".." + revisions[revision].revisionNumber.substr(revisions[revision].revisionNumber.length - 1);
            } else {
                // Sonst die ganze Revisionsnummer als Anzeigenamen setzen
                value.label = revisions[revision].revisionNumber;
            }
            // HÃ¶he setzen
            value.height = 30;
            // Revisionen werden als Kreise dargestellt
            value.shape = "circle";
            // Falls die Revision am Kopf des Branches steht
            if (branches[revisions[revision].revisionOfBranch].head == revision) {
                // Wird der Kreis komplett in der Branchfarbe gefÃ¼llt
                value.style = "fill:" + branches[revisions[revision].revisionOfBranch].color + ";stroke:none;";
            } else {
                // Sonst erhÃ¤lt er nur einen Rand in Branchfarbe
                value.style = "stroke:" + branches[revisions[revision].revisionOfBranch].color + ";";
            }
            // Knoten erzeugen
            g.setNode(revision, value);
        });

        // Commits aus dem Datenmodell als Kanten erstellen
        Object.keys(commits).forEach(function (commit) {
            // FÃ¼r jede Revision, aus der der Commit erzeugt wurde muss eine Kante erstellt werden
            for (var i = 0; i < commits[commit].used.length; i++) {
                // Farbe fÃ¼r die Kante
                var color;
                // Falls der Commit nur von einer Revision stammt
                if (commits[commit].used.length == 1) {
                    // Wird als Farbe fÃ¼r die Kante die Revision genommen, die der Commit erzeugt hat
                    color = branches[revisions[commits[commit].generated].revisionOfBranch].color;
                } else {
                    // Ansonsten die Farbe der Ursprungsrevision
                    color = branches[revisions[commits[commit].used[i]].revisionOfBranch].color;
                }
                // Kante von der Ursprungsrevision zur Revision, die der Commit erzeugt hat, erzeugen
                g.setEdge(commits[commit].used[i], commits[commit].generated, {
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
            createBranches();
        }
        // Falls erforderlich, Tags zum Anzeigen erzeugen
        if (_showTags) {
            createTags();
        }


        // Renderer erzeugen
        render = new dagreD3.render();

       
    	svg = d3.select(div_selector + " svg");
    	inner = svg.select("g");
    	
    	

        // Graphen rendern und ins DOM einfÃ¼gen
        render(inner, g);
        // Zoom aus D3 und Verschiebung des Graphen Ã¼ber D3 ermÃ¶glichen
        zoom = d3.behavior.zoom().on("zoom", function () {
        	inner.attr("transform", "translate(" + d3.event.translate + ")" +
            "scale(" + d3.event.scale + ")");
        });
    	
        // bind zoom to SVG
        svg.call(zoom);

        // tipsy-Funktion an Revisionsknoten binden
        // Dazu alle Knoten Ã¼ber D3 auswÃ¤hlen, Revisionen herausfinden. Titel setzen und tipsy binden
        inner.selectAll("g.node").filter(function (v) {
            return revisions[v] != null;
        })
            .attr("title", function (v) {
                return revTooltip(v, g.node(v))
            })
            // mit $.fn.tipsy.autoNS sorgt dafÃ¼r, dass Tooltips zu dicht am oberen oder unteren Rand passend dargestellt werden
            .each(function (v) {
                $(this).tipsy(tipsy_options);
            });
        
        // Bei Bedarf auch tipsy an die Tags binden
        if (_showTags) {
            bindTags();
        }
        // Graph skalieren und positionieren
        center();
        // Loader ausblenden
        spinner.hide();
    });

    function create_revision_model(data){
        // ZÃ¤hler initialisieren
        var j = 1;
        // Ãœber alle Eingangsdaten iterieren (Key-Value-Paare)
        $.each(data, function (key, value) {
            // Array der Datenobjekte mit den Typen laden
            var types = value["http://www.w3.org/1999/02/22-rdf-syntax-ns#type"];
            // ZÃ¤hler fÃ¼r die Farbzuweisung
            j++;
            // Ãœber alle verschiedenen Typen iterieren und je nach Typ behandeln
            for (var i = 0; i < types.length; i++) {
                switch (types[i].value) {
                    // Falls Commit
                    case "http://eatld.et.tu-dresden.de/rmo#Commit":
                        // Commit intialisieren
                        commits[key] = {};
                        // Nachricht setzen
                        commits[key].title = value["http://purl.org/dc/terms/title"][0].value;
                        // ID des Erstellers hinterlegen
                        commits[key].wasAssociatedWith = value["http://www.w3.org/ns/prov#wasAssociatedWith"][0].value;
                        // ID der Revision, die der Commit erstellt hat setzen
                        commits[key].generated = value["http://www.w3.org/ns/prov#generated"][0].value;
                        // Alle IDs der Revisionen, die in den Commit eingeflossen sind setzen (length > 1 --> Merge)
                        commits[key].used = [];
                        for (var k = 0; k < value["http://www.w3.org/ns/prov#used"].length; k++) {
                            commits[key].used.push(value["http://www.w3.org/ns/prov#used"][k].value);
                        }
                        // Zeitstempel, der fÃ¼r den Commit hinterlegt sind setzen
                        commits[key].time = value["http://www.w3.org/ns/prov#atTime"][0].value;
                        // Commit in der Revision hinterlegen, die aus dem Commit hervorging. Falls es die Revision im Datenmodell noch nicht gibt muss sie erstellt werden
                        if (revisions[commits[key].generated] == null) {
                            revisions[commits[key].generated] = {};
                        }
                        revisions[commits[key].generated].commit = key;
                        break;
                    // Falls Revision
                    case "http://eatld.et.tu-dresden.de/rmo#Revision":
                        // Falls die Revision noch nicht im Datenmodell existiert muss sie initialisiert werden
                        if (revisions[key] == null) {
                            revisions[key] = {};
                        }
                        // ID der entfernten Daten setzen
                        revisions[key].deltaRemoved = value["http://eatld.et.tu-dresden.de/rmo#deltaRemoved"][0].value;
                        // ID der hinzugefÃ¼gten Daten setzen
                        revisions[key].deltaAdded = value["http://eatld.et.tu-dresden.de/rmo#deltaAdded"][0].value;
                        // Revisionsnummer setzen
                        revisions[key].revisionNumber = value["http://eatld.et.tu-dresden.de/rmo#revisionNumber"][0].value;
                        // ID der eigentlichen Daten setzen
                        revisions[key].revisionOf = value["http://eatld.et.tu-dresden.de/rmo#revisionOf"][0].value;
                        // ID des Branches setzen, zu dem die Revision gehÃ¶rt
                        revisions[key].revisionOfBranch = value["http://eatld.et.tu-dresden.de/rmo#revisionOfBranch"][0].value;
                        break;
                    // Falls Branch
                    case "http://eatld.et.tu-dresden.de/rmo#Branch":
                        // Falls der Branch noch nicht im Datenmodell existiert muss er initialisiert werden
                        if (branches[key] == null) {
                            branches[key] = {};
                        }
                        // Name des Branch setzen
                        branches[key].label = value["http://www.w3.org/2000/01/rdf-schema#label"][0].value;
                        // ID der eigentlichen Daten setzen
                        branches[key].fullGraph = value["http://eatld.et.tu-dresden.de/rmo#fullGraph"][0].value;
                        // ID der Revision am Kopf des Branches setzen
                        branches[key].head = value["http://eatld.et.tu-dresden.de/rmo#references"][0].value;
                        // Falls noch keine Farbe fÃ¼r den Branch definiert ist, eine aus D3 laden
                        if (branches[key].color == null) {
                            branches[key].color = d3.rgb(colors(j)).brighter().toString();
                        }
                        break;
                    // Falls Tag
                    case "http://eatld.et.tu-dresden.de/rmo#ReferenceCommit":
                        // Tag initialisieren
                        referenceCommits[key] = {};
                        // Name des Tags setzen
                        referenceCommits[key].title = value["http://purl.org/dc/terms/title"][0].value;
                        // ID des Ersteller setzen
                        referenceCommits[key].wasAssociatedWith = value["http://www.w3.org/ns/prov#wasAssociatedWith"][0].value;
                        // Array fÃ¼r Zeitstempel initialisieren
                        referenceCommits[key].atTime = [];
                        // Array fÃ¼r Revisionen, die der Tag refereziert
                        referenceCommits[key].used = [];
                        // Alle Zeitstempel laden
                        for (var k = 0; k < value["http://www.w3.org/ns/prov#atTime"].length; k++) {
                            referenceCommits[key].atTime.push(value["http://www.w3.org/ns/prov#atTime"][k].value);
                        }
                        // Alle referenzierten Revisionen laden
                        for (var k = 0; k < value["http://www.w3.org/ns/prov#used"].length; k++) {
                            referenceCommits[key].used.push(value["http://www.w3.org/ns/prov#used"][k].value);
                        }
                        break;
                    // Falls Master
                    case "http://eatld.et.tu-dresden.de/rmo#Master":
                        // Falls der Masterbranch noch nicht als Branch vorliegt, muss er initialisiert werden
                        if (branches[key] == null) {
                            branches[key] = {};
                        }
                        // Alle Masterbranches sollen immer die gleiche Farbe haben
                        branches[key].color = "#5555ff";
                        break;
                }
            }

        });
    }
    
    var tipsy_options = {gravity: 'w', html: true, fade:true, trigger: 'focus', offset: 50, delayOut: 500};

	 // Center the graph
	 var center = function () {
	     // VerhÃ¤ltnis von Graph zur SVG GrÃ¶ÃŸe
	     var widthscale = svg_element.width() / (g.graph().width + 50);
	     var heightscale = svg_element.height() / (g.graph().height + 50);
	     // Kleineres VerhÃ¤ltnis zur Anpassung auswÃ¤hlen
	     var scale = widthscale < heightscale ? widthscale : heightscale;
	     // Graphen nicht grÃ¶ÃŸer als doppelt so groÃŸ anzeigen
	     scale = scale > 2 ? 2 : scale;
	     // Graph Ã¼ber D3 mittig positionieren und skalieren
	     zoom
	         .translate([(svg_element.width() - g.graph().width * scale) / 2, (svg_element.height() - g.graph().height * scale) / 2])
	         .scale(scale)
	         .event(svg_element);
	 };
	
	
	 function padStr(i) {
	     return (i < 10) ? "0" + i : "" + i;
	 }
	
	 // Funktion, die den Inhalt der Revisions-Tooltips erstellt
	 var revTooltip = function (name, node) {
	     // Tabelle mit den Detailinformationen der Revision erstellen, Verlinkte Informationen kÃ¶nnten ggfs. asynchron nachgeladen werden
	     var tooltip = "<h1>Revision " + node.revisionNumber + "</h1>" +
	         "<table class='properties'>" +
	         "<tr><td>Number:</td><td>" + node.revisionNumber + "</td><td></td></tr>" +
	         "<tr><td>Branch:</td><td>" + branches[node.revisionOfBranch].label + "</td><td><a href='" + node.revisionOfBranch + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
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
	     var date = new Date(commits[node.commit].time);
	     tooltip += "<h2>Creating Commits</h2>" +
	     "<table class='properties'>" +
	     "<tr><td>Title:</td><td colspan='2'>" + commits[node.commit].title + "</td></tr>" +
	     "<tr><td>Time:</td><td>" + padStr(date.getDate()) + "." + padStr((date.getMonth() + 1)) + "." + date.getFullYear() + " " + padStr(date.getHours()) + ":" + padStr(date.getMinutes()) + ":" + padStr(date.getSeconds()) + "</td><td></td></tr>" +
	     "<tr><td>User:</td><td>" + commits[node.commit].wasAssociatedWith + "</td><td><a href='" + commits[node.commit].wasAssociatedWith + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
	     "<tr><td>URL:</i></td><td colspan='2'>" + node.commit + "</td></tr>" + 
	     "</table>";
	     return tooltip;
	 };
	
	 // Funktion, die den Inhalt der Tag-Tooltips erstellt
	 var tagTooltip = function (node) {
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
	 var createTags = function () {
	     // Ãœber alle Tags iterieren
	     Object.keys(referenceCommits).forEach(function (referenceCommit) {
	         // Alle Daten kÃ¶nnen im D3 Modell hinterlegt werden
	         var value = referenceCommits[referenceCommit];
	         // Konfigurationsparameter setzen
	         // Anzeigename
	         value.label = referenceCommits[referenceCommit].title;
	         // HÃ¶he
	         value.height = 5;
	         // Hellblaue Rahmenfarbe
	         value.style = "stroke:#60b1fc;";
	         // SchriftgrÃ¶ÃŸe
	         value.labelStyle = "font-size:9px;";
	         // Knoten erzeugen
	         g.setNode(referenceCommit, value);
	         // FÃ¼r jede Revision, die der Tag referenziert, muss eine Kante erstellt werden
	         referenceCommits[referenceCommit].used.forEach(function (v) {
	             // Kante von der Revision zum Tag erstellen
	             g.setEdge(v, referenceCommit, {
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
	 var createBranches = function () {
	     // Ãœber alle Branches iterieren
	     Object.keys(branches).forEach(function (branch) {
	         // Alle Daten kÃ¶nnen im D3 Modell hinterlegt werden
	         var value = branches[branch];
	         // Konfigurationsparameter setzen
	         // Anzeigename
	         value.label = branches[branch].label;
	         // HÃ¶he
	         value.height = 5;
	         // In der Farbe des Branches fÃ¼llen
	         value.style = "fill:" + branches[branch].color + ";stroke:#fff;";
	         // TextgrÃ¶ÃŸe
	         value.labelStyle = "font-size:9px;";
	         // Node erstellen
	         g.setNode(branch, value);
	         // ZugehÃ¶rige Linie zur Revision erstellen
	         g.setEdge(branch, branches[branch].head, {
	             // Mindestabstand 1
	             minlen: 1,
	             // Linie ohne Pfeil
	             arrowhead: "undirected",
	             // Linie in der Farbe des Branches
	             style: "stroke:" + branches[branch].color + ";fill:none;",
	             arrowheadStyle: "fill:" + branches[branch].color + ";stroke:" + branches[branch].color + ";",
	             // Rundere Linien
	             lineInterpolate: "basis"
	         });
	     });
	 };
	
	 // tipsy-Funktion an Tag-Knoten binden
	 var bindTags = function () {
	     // Alle Knoten mit D3 erfassen, Tags herausfiltern. Titel setzen und tipsy binden
		 inner.selectAll("g.node").filter(function (v) {
	         return referenceCommits[v] != null;
	     })
	         .attr("title", function (v) {
	             return tagTooltip(g.node(v))
	         })
	         .each(function (v) {
	             $(this).tipsy(tipsy_options);
	         });
	 };
	
	 // Funktion um Tags einzublenden
	 var showTags = function () {
	     // Tag-Knoten und Kanten erstellen
	     createTags();
	     // Graph rendern und ins DOM einbinden
	     render(inner, g);
	     // tipsy-Funktion an die eingebundenen Knoten binden
	     bindTags();
	     // Graph neu skalieren und positionieren
	     center();
	 };
	
	 // Funktion um Tags auszublenden
	 var hideTags = function () {
	     // Alle Knoten mit D3 erfassen, Tags herausfiltern und lÃ¶schen
		 inner.selectAll("g.node").filter(function (v) {
	         return referenceCommits[v] != null;
	     })
	         .each(function (v) {
	             g.removeNode(v);
	         });
	     // Graph ohne Tags neu rendern
	     render(inner, g);
	     // Graph neu skalieren und positionieren
	     center(svg, g);
	 };
	
	 // Funktion um Branches einzubinden
	 var showBranches = function () {
	     // Branch-Knoten und Kanten erstellen
	     createBranches();
	     // Graph rendern und in DOM einbinden
	     render(inner, g);
	     // Graph neu skalieren und positionieren
	     center();
	 };
	
	 // Funktion um Branches auszublenden
	 function hideBranches() {
	     // Alle Knoten mit D3 erfassen, Branches herausfiltern und lÃ¶schen
		 inner.selectAll("g.node").filter(function (v) {
	         return branches[v] != null;
	     })
	         .each(function (v) {
	             g.removeNode(v);
	         });
	     // Graph ohne Branches neu rendern
	     render(inner, g);
	     // Graph neu skalieren und positionieren
	     center();
	 };
    
    
};
