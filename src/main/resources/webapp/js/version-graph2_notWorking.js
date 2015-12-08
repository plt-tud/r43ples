function d3_revGraph(
div_selector) {
        var commits = {};
    var revisions = {};
    var tags = {};
    var branches = {};
    var node=[], edge=[];
    /*Initialization and configuration*/
    var config = {
        "opacity_normal": "1.0",
        "opacity_highlight": "0.6",
        "colorRange": ["red", "yellow", "green", "blue"],
        "rotate": -55,
        "title": "",
        "title_class": "",
        "ytitle": "",
        "xtitle": "",
        "unit": "",
        "margin": { "top": 20, "right": 20, "bottom": 65, "left": 40}
    }
/*
    var dConfig = "";
    try {
        dConfig = JSON.parse(pConfig);
    } catch (e) {
        dConfig = {};
    }

    for (var attrname in dConfig) {
        config[attrname] = dConfig[attrname];
    }

    config.title = pTitle;
    config.ytitle = pYTitle;
    config.xtitle = pXTitle;*/
    var colors = d3.scale.category10();
   
    var margin = config.margin,
        width = getWidthwidth(),
        height = $(div_selector).height() - margin.top - margin.bottom;

    /*Define d3 axes and scales*/
    var x = d3.scale.ordinal()
        .rangeRoundPoints([0, width]);

    var y = d3.scale.ordinal()
        .rangeRoundPoints([height, 0]);

    /*var xAxis = d3.svg.axis()
        .scale(x)
        .orient("bottom");

    var yAxis = d3.svg.axis()
        .scale(y)
        .orient("left")
        .tickFormat(function (v) {
        return v + config.unit;
    })
        .ticks(Math.max(height / 50, 2)); //<-- dynamically adapt number of ticks to height
    /*create svg-element*/
    var contsvg = d3.select(div_selector).append("svg")
        .attr("width", getSVGwidth())
        .attr("height", getSVGheight())
        .style("margin", "0px auto")
        .style("display", "block");
    var svg = contsvg.append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    /*append chart-title and x-axis-title*/
   /* var title = contsvg.append("g");
    title.append("text")
        .attr("y", margin.top / 2)
        .attr("x", getSVGwidth() / 2)
        .attr("text-anchor", "middle")
        .attr("class", config.title_class)
        .text(config.title);
    /* x- axis- title*/
   /* title.append("text")
        .attr("y", getSVGheight() - 15)
        .attr("x", getSVGwidth() / 2)
        .attr("dy", ".35em")
        .style("text-anchor", "middle")
        .text(config.xtitle);
    /*create axes' elements*/
  /*  svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(xAxis)
        .selectAll("text")
        .style("text-anchor", "end")
        .attr("dx", "-.8em")
        .attr("dy", ".15em")
        .attr("transform", "rotate(" + config.rotate + ")");

    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis)
        .append("text")
        .attr("transform", "translate(" + (10 - margin.left) + ", " + height / 2 + ")rotate(-90)")
        .attr("dy", ".35em")
        .style("text-anchor", "middle")
        .text(config.ytitle);*/
    //div-container for tooltips
   /* d3.select("#d3_barcharttools_" + pRegionId).append("div")
        .attr("class", "xstooltip")
        .attr("id", "tooltip" + pRegionId);*/

    var data = [];
    var waitresize;

    //getData(refreshData);
refreshData(json_data);
    /* FUNCTION DEFINITIONS
-------------------------------------------------------------------------
*/
    function getSVGheight() {
        return height + margin.top + margin.bottom;
    }

    function getSVGwidth() {
        return width + margin.left + margin.right;
    }
	
	function getWidthwidth() {
		return $(div_selector).width() * .96 - margin.left - margin.right;
    }
    // Trim revision number if too long for node
function trimRevisionNumber(revisionNumber) {
	if (revisionNumber.length > 5) {
	    return revisionNumber.substr(0, 2) + ".." + revisionNumber.substr(revisionNumber.length - 2);
	} else {
	    return revisionNumber;
	}
}

function sortByDate (a,b){
    return a.date-b.date;
}
    function buildData(d3json) {
        var j = 1;
        $.each(d3json, function (key, value) {
            var types = value["http://www.w3.org/1999/02/22-rdf-syntax-ns#type"];
            j++;
            for (var i = 0; i < types.length; i++) {
                switch (types[i].value) {
                    // Falls Commit
                    case "http://eatld.et.tu-dresden.de/rmo#Commit":
                        commits[key] = {};
                        commits[key].title = value["http://purl.org/dc/terms/title"][0].value;
                        commits[key].wasAssociatedWith = value["http://www.w3.org/ns/prov#wasAssociatedWith"][0].value;
                        commits[key].generated = value["http://www.w3.org/ns/prov#generated"][0].value;
                        commits[key].used = [];
                        for (var k = 0; k < value["http://www.w3.org/ns/prov#used"].length; k++) {
                            commits[key].used.push(value["http://www.w3.org/ns/prov#used"][k].value);
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
                        revisions[key].deleteSet = (value["http://eatld.et.tu-dresden.de/rmo#deleteSet"])?value["http://eatld.et.tu-dresden.de/rmo#deleteSet"][0].value : null;
                        revisions[key].addSet = (value["http://eatld.et.tu-dresden.de/rmo#addSet"])?value["http://eatld.et.tu-dresden.de/rmo#addSet"][0].value : null;
                        revisions[key].revisionNumber = value["http://eatld.et.tu-dresden.de/rmo#revisionNumber"][0].value;
                        revisions[key].revisionOf = value["http://eatld.et.tu-dresden.de/rmo#revisionOf"][0].value;
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

        });
        console.log("rev", revisions);
        console.log("commits", commits);
        console.log("branches", branches);
        console.log("tags", tags);
        
         // create nodes for every revision
        Object.keys(revisions).forEach(function (revision) {
            var value = revisions[revision];
            value.label = trimRevisionNumber(revisions[revision].revisionNumber);
            value.height = 25;
            value.shape = "circle";
            value.style = "stroke:" + branches[revisions[revision].belongsTo].color + ";";
            //g.setNode(revision, value);
            node.push(value);
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
                /*g.setEdge(commits[commit].used[i], commits[commit].generated, {
                    style: "stroke:" + color + ";fill:none;",
                    arrowheadStyle: "fill:" + color + ";stroke:" + color + ";",
                    lineInterpolate: "basis"
                });*/
                var value =commits[commit];
                value.color=color;
                value.date= d3.time.format("%Y-%m-%dT%H:%M:%S").parse(commits[commit].time);
                edge.push(value)
            }
        });
        edge=edge.sort(sortByDate);
        console.log(node, edge);
    }

    function Pageload(link) {
        window.location = link;
    }

    function getData(f) {
        /*apex.server.plugin(
        pAjaxId, {}, {
            success: f,
            error: function (d) { /*NOTLÖSUNG: fehlende führende 0 bei Zahlenwerten führt im jQuery-JSONParser zu Fehlern*/
                //console.log("fail");console.log(d);console.log(d.responseText); 
          /*      var json = d.responseText.replace(/(\:\.|\:\,)/g, ":0.");
                f($.parseJSON(json));
            },
            dataType: "json"
        });*/
        // JSON-Daten mit jQuery laden und parsen
    $.getJSON(_JSON, function (data, status) {
        if (status != "success") {
            alert("Error getting JSON Data: Status " + status);
        }

        f(data);
    });
    }

    //wait some ms before reacting on the resize->decrease compute-weight
    $(window).on("resize", function () {
        clearTimeout(waitresize);
        waitresize = setTimeout(function () {
            wresize();
        }, 100);
    });

    function wresize() {
        /*height dependend from region-height -> hard coded in style
	-> get new width
	-> recompute all affected elements
	*/
        width = getWidthwidth();
        x.rangeRoundPoints([0, width]);
       /* yAxis.ticks(Math.max(height / 50, 2));

        svg.select("g.x.axis").transition()
            .call(xAxis)
            .selectAll("text")
            .style("text-anchor", "end")
            .attr("dx", "-.8em")
            .attr("dy", ".15em")
            .attr("transform", "rotate(" + config.rotate + ")");

        svg.selectAll("g.y.axis g.tick line").transition()
            .attr("x2", width);
        /*svg.select("g.y.axis")
		.call(yAxis);*/

        /*title.selectAll("text").transition()
            .attr("x", getSVGwidth() / 2);*/

        d3.select(div_selector + " svg")
            .attr("width", getSVGwidth())
            .attr("height", getSVGheight());

        /*var node = svg.selectAll(".tool");
        node.transition()
            .attr("transform", function (d) {
            return "translate(" + x(d.key) + ",0)";
        })
            .selectAll(".toolrect")
            .attr("width", x.rangeBand())
            .attr("y", function (d) {
            return y(d.y1);
        })
            .attr("height", function (d) {
            return y(d.y0) - y(d.y1);
        });

        node.selectAll("g rect").transition()
        //.attr("y", function (d) {return y(d3.sum(d.states, function (d){return d.value;}));})
        .attr("width", x.rangeBand())
        //.attr("height",function (d){return y(0)-y(d3.sum(d.states, function (d){return d.value;})); })
        ;*/

    }

    /*
-------------------------------------------------------------------------------------------------------
*/
    function refreshData(d3json) {
        /*No data found - section*/
        if (!d3json || d3json.length == 0) {
            contsvg.append("text").attr("class", "NoData")
                .attr("x", getSVGwidth() / 2)
                .attr("y", getSVGheight() / 2)
                .attr("dy", ".35em")
                .attr("text-anchor", "middle")
                .text("No Data");
            return;
        }
        contsvg.select("text.NoData").remove();

        buildData(d3json); //<-- get useable data-array out of json-object
        

        var edges = svg.selectAll(".edge")
            .data(edge);
			

        /*INSERT-section ->visualize new label*/
        var edgesEnter = egde.enter().append("g")
            .attr("class", "edge")
            .attr("transform", function (d) {
            return "translate(" + x(d.date) + ",0)";
        });

    /*UPDATE-section ->update existing labels*/
    /*DELETE-section -> remove no longer existing labels*/
    
}
}