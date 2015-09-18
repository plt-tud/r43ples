
<#include "superHeader.ftl">

<div class="row">
	<div id="main" class="large-10 large-offset-1 columns">
		<div id="results">
			<h2>Results</h2>
			<#escape x as x?html>
			<pre>${result}</pre>
			</#escape>
		</div>
		
	  	<div id="query">
	  		<h2>Query</h2>
	  		<pre>${query}</pre> 
		</div>
	</div>
</div>

<#include "superFooter.ftl">
