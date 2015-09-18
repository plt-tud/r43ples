<#include "superHeader.ftl">
	<div class="row" style="margin-top:-42%">
		<div id="main" class="small-6 columns">
			<div id="query_form">
				<h2>Query Form</h2>
				<form action="/r43ples/debug" method="get" role="form" class="form">
					<fieldset>
						<legend><label for="query" style="font-size:1.266rem">Query Text</label></legend>
						<textarea name="query" class="form-control" id="query" rows="16">
	SELECT * 
	WHERE { 
		GRAPH &lt;${revisionGraph}&gt; {
			?s ?p ?o 
		}
	}
						</textarea>
					</fieldset>
						<br/>
						<div class="button-group">
							<input type="submit" class="small button success" value="Run Query" />
							<input type="reset"	class="small button default" value=" Reset " />
						</div>
				</form>
			</div>
		</div>
		<div id="main" class="small-6 columns">
			<h2>Debug information</h2>
			<h3>Configuration</h3>
			<ul>
				<li>Revision Graph: <samp>${revisionGraph}</samp></li>
				<li>SDD Graph: <samp>${sdd_graph}</samp></li>
				<li>Triplestore: <samp>${triplestore}</samp></li>
			</ul>
			<h3>Existing graphs in triplestore</h3>
			<ul>
				
				<#list graphs as graph>  
				 <li><samp>${graph}</samp></li>
				</#list>
				</ul>
		</div>
	</div>
<#include "superFooter.ftl">

	