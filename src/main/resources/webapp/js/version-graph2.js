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
    var format = d3.time.format("%Y-%m-%dT%H:%M:%S");
    var commits = {};
    var revisions = {};
    var tags = {};
    var branches = {};
    var zoom;
	var changeSets = {};
	var rev_ar=[];
	var branch_ar=[];
	var branchPositions = {};
    
    d3.select(div_selector).append('div')
    .attr('class','revisionGraphVisualisation')
    .append('svg').append('g');
    
    d3.select(div_selector).append('div')
    .attr('class','checkbox')
    .html("<label><input type='checkbox' class='toggle-tags'>Show Tags</label>");
    
    d3.select(div_selector).append('div')
    .attr('id','infos')
    .style('display','none');
    d3.select('#infos').append('div')
    .attr('id','header')
    .html('<h2>Revision</h2>');
    d3.select('#infos').append('div')
    .html('<h2>Changeset </h2>')
    .append('div').attr('id', 'changesets');


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
		getChangeSets();
		console.log('revisions', revisions);
		console.log('commits', commits);
		create_revision_array();
    	sortBranches();

        // create nodes for every revision
        Object.keys(revisions).forEach(function (revision) {
            var value = revisions[revision];
            value.label = trimRevisionNumber(revisions[revision].revisionNumber);
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
        
        bindQTipToRevisionNodes();
        
        center();
        spinner.hide();
    });

    function create_revision_model(data){
        // counter for coloring
        var j = 1;
        $.each(data, function (key, value) {
            var types = value["http://www.w3.org/1999/02/22-rdf-syntax-ns#type"];
            //console.log('Typen: ',value["http://www.w3.org/1999/02/22-rdf-syntax-ns#type"], 'key ', key, 'value ', value);
            j++;
            for (var i = 0; i < types.length; i++) {
                switch (types[i].value) {
             // Falls Commit
                case "http://eatld.et.tu-dresden.de/rmo#RevisionCommit":
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
                    if (revisions[commits[key].generated] == null) {
                        revisions[commits[key].generated] = {};
                    }
                    revisions[commits[key].generated].commit = key;
                    break;
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
                    branches[key].derivedFrom = value["http://www.w3.org/ns/prov#wasDerivedFrom"]?value["http://www.w3.org/ns/prov#wasDerivedFrom"][0].value:null;
                    
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

        });
    }
    
    function getChangeSets() {
    	Object.keys(revisions).forEach(function (revision) {
    		var value = revisions[revision];
    		changeSets[revision] = {};
    		changeSets[revision].addSet = [];
    		changeSets[revision].deleteSet = [];
    		if (value.addSet!=null){
        		var j = 0;
        		$.ajax({
        			type: "GET",
        			url: "contentOfGraph?graph="+value.addSet+"&format=application/json",
        			async: false,
        			success: function(data) { 
						$.each(data, function (subject, predicates) {
							$.each(predicates, function (predicate, objects){
								for (var i = 0; i < objects.length; i++) {
					    			var object = objects[i].value;
									changeSets[revision].addSet[j] = subject + " - " + predicate + " - " + objects[i].value + "<br>";
									//console.log("added to revision " + revision + ": " + changeSets[revision].addSet[j]);
									j ++;
								}
							})
						})
        			}
	    		})
    		}
    		
    		if (value.deleteSet!=null){
        		var j = 0;
        		$.ajax({
        			type: "GET",
        			url: "contentOfGraph?graph="+value.deleteSet+"&format=application/json",
        			async: false,
        			success: function(data) { 
		    			$.each(data, function (subject, predicates) {
		    				$.each(predicates, function (predicate, objects){
		    					for (var i = 0; i < objects.length; i++) {
					    			var object = objects[i].value;
		    						changeSets[revision].deleteSet[j] = subject + " - " + predicate + " - " + objects[i].value + "<br>";
		    						//console.log("deleted from revision " + revision + ": " + changeSets[revision].deleteSet[j]);
		    						j ++;
		    					}
		    				})
		    			})
        			}
	    		})
    		}
    	})
    }
    
    function create_revision_array(){
    	Object.keys(revisions).forEach(function (i) {
    		rev_ar.push({
    			id: i, 
    			deleteSet: revisions[i].deleteSet,
    			addSet: revisions[i].addSet,
    			revNo: revisions[i].revisionNumber,
    			belongsTo: revisions[i].belongsTo
    			});
    	});
    	console.log('array', rev_ar);
    	
    	Object.keys(commits).forEach(function (i) {
    		var elementPos = rev_ar.map(function(x) {return x.id; }).indexOf(commits[i].generated);
    		rev_ar[elementPos].time=commits[i].time;
    		rev_ar[elementPos].d3time=format.parse(commits[i].time);	
    		rev_ar[elementPos].title=commits[i].title;
    		rev_ar[elementPos].used=commits[i].used;
    	});
    	console.log('with time', rev_ar);
    	
    	rev_ar.sort(function(a, b) { return b.d3time - a.d3time; });
    	console.log('sort?', rev_ar);
    	
    	console.log(branches);
    	Object.keys(branches).forEach(function (i) {
    		var elementPos = rev_ar.map(function(x) {return x.id; }).indexOf(branches[i].head);
    		var elementPos2 = rev_ar.map(function(x) {return x.id; }).indexOf(branches[i].derivedFrom);
    		var end_time = branches[i].derivedFrom? rev_ar[elementPos2].d3time: new Date()
    		branch_ar.push({
    			id: i,
    			starttime: rev_ar[elementPos].d3time,
    			endtime: end_time,
    			color: branches[i].color,
    			label:branches[i].label
    		});
    	});
    	console.log('brancharr', branch_ar);
    }
    
    /** erzeugt das Objekt branchPositions von dem die Position eines Branches über dessen ID
     * abgefragt werden kann mittels: branchPositions[branchID].pos
     * Positionen sind Integer von 0 - x, der Master-Branch hat immer Position 0 **/
    function sortBranches(){
    	var positions = [];
    	branch_ar.sort(function(a, b) { return b.endtime - a.endtime; });
    	for  (var i = 0; i < branch_ar.length; i++){
    		if (branch_ar[i].label != "master"){
    			for (var j = 0; j <= positions.length; j++){
    				if (branch_ar[i].starttime < positions[j] || positions[j] == null){
    					positions[j] = branch_ar[i].starttime;
    					branchPositions[branch_ar[i].id] = {};
    					branchPositions[branch_ar[i].id].pos = j + 1;
    					break;
    				}    			
    			}
    		}
    		else {
				branchPositions[branch_ar[i].id] = {};
				branchPositions[branch_ar[i].id].pos = 0;
    		}
    	}
    	console.log('branch positions', branchPositions);
    }
    
    var qtip_options = {gravity: 'w', html: true, fade:true, trigger: 'focus', offset: 100};

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
	     var tooltip = "<h1>Revision " + node.revisionNumber+"</h1>"
	     if (node.commit != null) { 
		     var date = new Date(commits[node.commit].time);
		     tooltip += "<table class='properties' style='width:100%'>"+
		    	"<tr><th style='padding-right:5px;'>" + commits[node.commit].title + "</th><td align='right' style='vertical-align:top;'>" + dateString(date) + "</td></tr>" + 
		    	"<table class='properties'>"+
		    	"<tr><th style='padding-right:5px;'>User:</th><td>" + commits[node.commit].wasAssociatedWith + "</td><td><a href='" + commits[node.commit].wasAssociatedWith + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
		    	"<tr><th style='padding-right:5px;'>URL:</th><td>" + node.commit + "</td><td></td></tr>";
		 }
	     tooltip+="</table>";
	     
	     return tooltip;
	 };

	// Funktion, die den Inhalt der Changeset-Anzeige im Detailfeld erstellt
	 var displayChangeset = function (name, node) {
	     var changesetText = //"<h2>Changeset </h2>"+
	     	"<h3>Add Set</h3><div><ul class='addSet'>";
	     for (var i = 0; i < changeSets[name].addSet.length; i++) {
	    	 changesetText+="<li class='addSet'>"+ changeSets[name].addSet[i] + "</li>";
	     }
	     changesetText += '</ul></div><h3>Delete Set</h3><div><ul class="deleteSet">';
	     for (var i = 0; i < changeSets[name].deleteSet.length; i++) {
	    	 changesetText+="<li class='deleteSet'>"+  changeSets[name].deleteSet[i] + "</li>";
	     }
	     changesetText += '</ul></div>';
	     
	     return changesetText;
	 };
	// Funktion, die den Inhalt der Info-Anzeige im Detailfeld erstellt
	 var displayHeader = function (name, node) {
	     var headerText = "<h1>Revision " + node.revisionNumber+"</h1>"+
	     	"<table class='properties' style='width:100%'>";
	     if (node.commit != null) { 
		     var date = new Date(commits[node.commit].time);
		     headerText += "<tr><th>"+ commits[node.commit].title + "</th><td align='right' style='vertical-align:top;'>"+ dateString(date) + "</td></tr></table>" +  
		     "<table class='properties' style='width:100%'><tr><th>User:</th><td>" + commits[node.commit].wasAssociatedWith + "</td><td><a href='" + commits[node.commit].wasAssociatedWith + "' target='_blank'><i class='fa fa-external-link'></i></a></td></tr>" +
		     "<tr><th>URL:</th><td>" + node.commit + "</td><td></td></tr></table>";
		 }
	     
	     return headerText;
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
	
    // bind qtip and detailtext to revision node
    function bindQTipToRevisionNodes(){
        inner.selectAll("g.node").filter(function (v) {
            return revisions[v] != null;
        })
        .attr("title", function (v) {
        	return revTooltip(v, g.node(v))
        })
        .each(function (v) {
        	//console.log(g.node(v));
        	var n = g.node;
        	//console.log(v);
        	//console.log(this);
        	$(this).on("click", function () {
            	//console.log("clicked");
        		$("#infos").css('display', '');
            	$("#header").html( displayHeader(v, g.node(v)) );
            	$("#changesets").html( displayChangeset(v, g.node(v)) );
            	$( "#changesets" ).accordion( "refresh" );
            	$( "#changesets" ).accordion( "option", "active", false );
            	$("#changesets").animate({
                    scrollTop: 0
                }, 0);
        	})
        	$(this).qtip({
        		//show: 'mouseover',
        		//hide: 'mouseout',
        		position: {
                    target: 'mouse', // Track the mouse as the positioning target
                    adjust: { x: 5, y: 5 } // Offset it slightly from under the mouse
                }
        	})
        });
        $( "#changesets" ).accordion({
  		  collapsible: true,
  		heightStyle: "content"
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
