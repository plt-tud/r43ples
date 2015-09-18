<#include "superHeader.ftl">


	<script>
		$(document)
				.ready(
						function() {
							var svg = document.getElementById("revisiongraph").innerHTML;
							var b64 = btoa(svg);
							$("#graphDownload").attr("href", "data:image/svg+xml;base64," + b64);
						});
	</script>
    
	<div class="row">
	    <h1>Revision Graph</h1>
	    <h3><samp>&lt;${graphName}&gt;</samp></h3>
		
		<div id="revisiongraph">
		<!-- {{{svg_content}}} -->
			${svg_content}
		</div>
		<br/>
		<a class="button small default" href="sparql">Back to Endpoint</a>
		<a class="button small default" id="graphDownload" href-lang='image/svg+xml' title='revisiongraph_${graphName}.svg' download='revisiongraph_${graphName}.svg'>Download</a><br/>
	</div>


<#include "superFooter.ftl">