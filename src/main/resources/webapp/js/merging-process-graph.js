
var drawProcess = function () {
	  var workers = {
			    "identifier": {
			      "consumers": 2,
			      "count": 20
			    },
			    "lost-and-found": {
			      "consumers": 1,
			      "count": 1,
			      "inputQueue": "identifier",
			      "inputThroughput": 50
			    },
			    "monitor": {
			      "consumers": 1,
			      "count": 0,
			      "inputQueue": "identifier",
			      "inputThroughput": 50
			    },
			    "meta-enricher": {
			      "consumers": 4,
			      "count": 9900,
			      "inputQueue": "identifier",
			      "inputThroughput": 50
			    },
			    "geo-enricher": {
			      "consumers": 2,
			      "count": 1,
			      "inputQueue": "meta-enricher",
			      "inputThroughput": 50
			    },
			    "elasticsearch-writer": {
			      "consumers": 0,
			      "count": 9900,
			      "inputQueue": "geo-enricher",
			      "inputThroughput": 50
			    }
			  };

			  // Set up zoom support
			  var svg = d3.select("#svg"),
			      inner = svg.select("g"),
			      zoom = d3.behavior.zoom().on("zoom", function() {
			        inner.attr("transform", "translate(" + d3.event.translate + ")" +
			                                    "scale(" + d3.event.scale + ")");
			      });
			  //注释掉call(zoom) 这图形不会任意变小或者变大了，也无法再拖拉了。
			  svg.call(zoom);

			  var render = new dagreD3.render();

			  // Left-to-right layout
			  var g = new dagreD3.graphlib.Graph();
			  g.setGraph({
			    nodesep: 70,
			    ranksep: 50,
			    rankdir: "LR",
			    marginx: 20,
			    marginy: 20
			  });

			  function draw(isUpdate) {
			    for (var id in workers) {
			      var worker = workers[id];
			      var className = worker.consumers ? "running" : "stopped";
			      if (worker.count > 10000) {
			        className += " warn";
			      }
			      var html = "<div>";
			      html += "<span class=status></span>";
			      html += "<span class=consumers>"+worker.consumers+"</span>";
			      html += "<span class=name>"+id+"</span>";
			      html += "<span class=queue><span class=counter>"+worker.count+"</span></span>";
			      html += "</div>";
			      g.setNode(id, {
			        labelType: "html",
			        label: html,
			        rx: 5,
			        ry: 5,
			        padding: 0,
			        class: className
			      });

			      if (worker.inputQueue) {
			        g.setEdge(worker.inputQueue, id, {
			          label: worker.inputThroughput + "/s",
			          width: 40
			        });
			      }
			    }

			    inner.call(render, g);

			    // Zoom and scale to fit:自动调整图形的大小，使得在svg里的始终是整个图形
			    var zoomScale = zoom.scale();
			    var graphWidth = g.graph().width + 80;
			    var graphHeight = g.graph().height + 40;
			    var width = parseInt(svg.style("width").replace(/px/, ""));
			    var height = parseInt(svg.style("height").replace(/px/, ""));
			    zoomScale = Math.min(width / graphWidth, height / graphHeight);
			    var translate = [(width/2) - ((graphWidth*zoomScale)/2), (height/2) - ((graphHeight*zoomScale)/2)];
			    zoom.translate(translate);
			    zoom.scale(zoomScale);
			    zoom.event(isUpdate ? svg.transition().duration(500) : d3.select("svg"));
			  }
			  draw();

}