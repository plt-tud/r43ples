function addSpinner(element) {
	SPINNER_WIDTH = SPINNER_HEIGHT = 60;
	var spinner_html = jQuery.parseHTML(
		"<div class='spinner' style='position: absolute; " +
					"top:"  + (element.position().top  + (element.height()-SPINNER_HEIGHT)/2) + "px;" +
					"left:" + (element.position().left + (element.width() -SPINNER_WIDTH )/2) + "px;'>" +
			"<i class='fa fa-spinner fa-pulse fa-3x'></i>" +
		"</div>");
	element.after(spinner_html);
	return element.next();
}

// Trim revision number if too long for node
function trimRevisionNumber(revisionNumber) {
	if (revisionNumber.length > 5) {
	    return revisionNumber.substr(0, 2) + ".." + revisionNumber.substr(revisionNumber.length - 2);
	} else {
	    return revisionNumber;
	}
}


// Create a new directed graph
/** Hauptfunktion, um kompletten Graphen zu erstellen.
 *
 *  _JSON: URL zu JSON-Daten oder Daten selbst

 */
function drawGraph(div_selector, _JSON, _showTags) {
    // _showTags default is false
    _showTags = _showTags || false;
    
    var g;		//d3 graph
    var svg, inner;
	var div_element = $(div_selector);
    var colors = d3.scale.category10();
    var commits = {};
    var revisions = {};
    var tags = {};
    var branches = {};
    var zoom;
    
    
	div_element.html(
		"<div class='revisionGraphVisualisation'>" +
		"	<svg><g/></svg>" +
		"</div>" +
		"<div class='checkbox'>" +
        "	<label><input type='checkbox' class='toggle-tags'>Show Tags</label>" +
	  	"</div>");

	// ChangeListener for tags
	div_element.find('.toggle-tags').change(function () {
	    $(this).prop('checked')?showTags():hideTags();
	});


	
	// http://stackoverflow.com/questions/16265123/resize-svg-when-window-is-resized-in-d3-js
	function resizeSVG(){
	    $('.revisionGraphVisualisation svg').each(function() {
	    	$(this).width($(this).parent().width());
	    });
	}
	$(window).resize( resizeSVG );	 
	resizeSVG();

	var svg_element = div_element.find('svg');
	var spinner = addSpinner(svg_element);

    // JSON-Daten mit jQuery laden und parsen
    $.getJSON(_JSON, function (data, status) {
        if (status != "success") {
            alert("Error getting JSON Data: Status " + status);
        }

        // create  D3 graph
        g = new dagreD3.graphlib.Graph();
        g.setGraph({
          rankdir: "LR",
        });


        // light blue color for tags
        colors(0);
		
		create_revision_model(data);

        // create nodes for every revision
        Object.keys(revisions).forEach(function (revision) {
            var value = revisions[revision];
            value.label = revisions[revision].revisionNumber;
            value.height = 25;
            value.shape = "circle";
            value.style = "stroke:" + branches[revisions[revision].belongsTo].color + ";";
            g.setNode(revision, value);
        });

        // create edge for every commit
        Object.keys(commits).forEach(function (commit) {
            for (var i = 0; i < commits[commit].used.length; i++) {
                var color;
                // Falls der Commit nur von einer Revision stammt
                if (commits[commit].used.length == 1) {
                    // Wird als Farbe fÃ¼r die Kante die Revision genommen, die der Commit erzeugt hat
                    color = branches[revisions[commits[commit].generated].belongsTo].color;
                } else {
                    // Ansonsten die Farbe der Ursprungsrevision
                    color = branches[revisions[commits[commit].used[i]].belongsTo].color;
                }
                g.setEdge(commits[commit].used[i], commits[commit].generated, {
                    style: "stroke:" + color + ";fill:none;",
                    arrowheadStyle: "fill:" + color + ";stroke:" + color + ";",
                    lineInterpolate: "basis"
                });
            }
        });

        createBranches();
        if (_showTags) {
            createTags();
        }
       
    	svg = d3.select(div_selector + " svg");
    	inner = svg.select("g");
    	
        // render graph and add to DOM
        render = new dagreD3.render();
        render(inner, g);
        
        // enable zooming and panning of graph
        zoom = d3.behavior.zoom().on("zoom", function () {
        	inner.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
        });
    	
        // bind zoom to SVG
        svg.call(zoom);

		bindTipsyToRevisionNodes();

        center();
        spinner.hide();
    });

    function create_revision_model(data){
        // counter for coloring
        var j = 1;
        $.each(data, function (key, value) {
            j++;
            $.each(value["http://www.w3.org/1999/02/22-rdf-syntax-ns#type"], function(k, type) {
                switch (type.value) {
                    // Falls Revision
                    case "http://eatld.et.tu-dresden.de/rmo#Revision":
                        if (revisions[key] == null) {
                            revisions[key] = {};
                        }
                        if (value["http://eatld.et.tu-dresden.de/rmo#deleteSet"]!=null){
                        	revisions[key].deleteSet = value["http://eatld.et.tu-dresden.de/rmo#deleteSet"][0].value;
                        }
                        if (value["http://eatld.et.tu-dresden.de/rmo#addSet"]){
                        	revisions[key].addSet = value["http://eatld.et.tu-dresden.de/rmo#addSet"][0].value;
                        }
                        revisions[key].revisionNumber = value["http://eatld.et.tu-dresden.de/rmo#revisionNumber"][0].value;
                        revisions[key].belongsTo = value["http://eatld.et.tu-dresden.de/rmo#belongsTo"][0].value;
                        break;
                    // Falls Branch
                    case "http://eatld.et.tu-dresden.de/rmo#Branch":
                        if (branches[key] == null) {
                            branches[key] = {};
                        }
                        branches[key].label = value["http://www.w3.org/2000/01/rdf-schema#label"][0].value;
                        branches[key].fullGraph = value["http://eatld.et.tu-dresden.de/rmo#fullGraph"][0].value;
                        branches[key].head = value["http://eatld.et.tu-dresden.de/rmo#references"][0].value;
                        if (branches[key].color == null) {
                            branches[key].color = d3.rgb(colors(j)).brighter().toString();
                        }
                        break;
                    // Falls Tag
                    case "http://eatld.et.tu-dresden.de/rmo#Tag":
                        tags[key] = {};
                        tags[key].label = value["http://www.w3.org/2000/01/rdf-schema#label"][0].value;
                        tags[key].head = value["http://eatld.et.tu-dresden.de/rmo#references"][0].value;
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
            )

        });
        
        // Adding Commits
        $.each(data, function (key, value) {
            $.each(value["http://www.w3.org/1999/02/22-rdf-syntax-ns#type"], function(k, type) {
               if (type.value== "http://eatld.et.tu-dresden.de/rmo#RevisionCommit"){
                        commits[key] = {};
                        commits[key].title = value["http://purl.org/dc/terms/title"][0].value;
                        commits[key].wasAssociatedWith = value["http://www.w3.org/ns/prov#wasAssociatedWith"][0].value;
                        commits[key].generated = value["http://www.w3.org/ns/prov#generated"][0].value;
                        commits[key].used = [];
                        if (value["http://www.w3.org/ns/prov#used"]){
                            for (var k = 0; k < value["http://www.w3.org/ns/prov#used"].length; k++) {
                                commits[key].used.push(value["http://www.w3.org/ns/prov#used"][k].value);
                            }
                        }
                        commits[key].time = value["http://www.w3.org/ns/prov#atTime"][0].value;
                        if (revisions[commits[key].generated]){
                        	revisions[commits[key].generated].commit = key;
                        	}
                       }
                        });
                        });
                        
    }
    
    var tipsy_options = {gravity: 'w', html: true, fade:true, trigger: 'focus', offset: 100};

	 // Center the graph
	 var center = function () {
	     // relationf of graph to svg size
	     var widthscale = svg_element.width() / (g.graph().width + 50);
	     var heightscale = svg_element.height() / (g.graph().height + 50);
	     // use smaller scale for adaptation
	     var scale = widthscale < heightscale ? widthscale : heightscale;
	     // not more than twice the size
	     scale = scale > 2 ? 2 : scale;
	     zoom
	         .translate([(svg_element.width() - g.graph().width * scale) / 2, (svg_element.height() - g.graph().height * scale) / 2])
	         .scale(scale)
	         .event(svg_element);
	 };
	
	
	 function padStr(i) {
	     return (i < 10) ? "0" + i : "" + i;
	 }
	 
	 function dateString(date) {
	 	return padStr(date.getDate()) + "." + padStr((date.getMonth() + 1)) + "." + date.getFullYear() + " " + padStr(date.getHours()) + ":" + padStr(date.getMinutes()) + ":" + padStr(date.getSeconds());
	 }
	
	 // Funktion, die den Inhalt der Revisions-Tooltips erstellt
	 var revTooltip = function (name, node) {
	     var tooltip = "<h1>Revision " + node.revisionNumber + "</h1>" +
	         "<table class='properties'>" +
	         "<tr><td>Number:</td><td>" + node.revisionNumber + "</td><td></td></tr>" +
	         "<tr><td>Branch:</td><td>" + branches[node.belongsTo].label + "</td><td><a href='" + node.belongsTo + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
	         "<tr><td>URL:</td><td>" + name + "</td><td></td></tr>" +
	         "<tr><td>Add Set:</td><td>" + node.addSet + "</td><td><a href='" + node.addSet + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
	         "<tr><td>Delete Set:</td><td>" + node.deleteSet + "</td><td><a href='" + node.deleteSet + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
	         "</table></p>";
	        
	     if (node.commit != null) { 
		     var date = new Date(commits[node.commit].time);
		     tooltip += "<h2>Commit</h2>" +
		     "<table class='properties'>" +
		     "<tr><td>Title:</td><td colspan='2'>" + commits[node.commit].title + "</td></tr>" +
		     "<tr><td>Time:</td><td colspan='2'>" + dateString(date) + "</td></tr>" +
		     "<tr><td>User:</td><td>" + commits[node.commit].wasAssociatedWith + "</td><td><a href='" + commits[node.commit].wasAssociatedWith + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
		     "<tr><td>URL:</i></td><td colspan='2'>" + node.commit + "</td></tr>" + 
		     "</table>";
		 }
	     return tooltip;
	 };
	

	
	 // Funktion, die die Tags als Knoten mit Kanten erstellt
	 var createTags = function () {
	     Object.keys(tags).forEach(function (referenceCommit) {
	         var value = tags[referenceCommit];
	         value.label = tags[referenceCommit].label;
	         value.height = 5;
	         value.style = "stroke:#60b1fc;";
	         value.labelStyle = "font-size:9px;";
	         g.setNode(referenceCommit, value);
             g.setEdge(tags[referenceCommit].head, referenceCommit, {
                 style: "stroke:#60b1fc;fill:none;stroke-dasharray: 5, 5;",
                 arrowhead: "undirected",
                 weight: 2,
             });
	     });
	 };
	
	 // Funktion, die die Branches als Knoten mit Kanten erstellt
	 var createBranches = function () {
	     Object.keys(branches).forEach(function (branch) {
	         var value = branches[branch];
	         value.label = branches[branch].label;
	         value.height = 5;
	         value.style = "fill:" + branches[branch].color + ";stroke:#fff;";
	         value.labelStyle = "font-size:9px;";
	         g.setNode(branch, value);
	         g.setEdge(branches[branch].head, branch, {
	             style: "stroke:" + branches[branch].color + ";fill:none;",
	             arrowhead: "undirected",
	             lineInterpolate: "basis"
	         });
	     });
	 };
	
    // bind tipsy to revision node
    function bindTipsyToRevisionNodes(){
        inner.selectAll("g.node").filter(function (v) {
            return revisions[v] != null;
        })
            .attr("title", function (v) {
                return revTooltip(v, g.node(v))
            })
            .each(function() {
                $(this).tipsy(tipsy_options);
            });
     }
            
	
	 // Funktion um Tags einzublenden
	 var showTags = function () {
	     createTags();
	     render(inner, g);
	 };
	
	 // Funktion um Tags auszublenden
	 var hideTags = function () {
	     // remove tag nodes from d3
		 inner.selectAll("g.node").filter(function (v) {
	         return tags[v] != null;
	     })
	         .each(function (v) {
	             g.removeNode(v);
	         });
	     render(inner, g);
	 };    
    
};
