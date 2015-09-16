<#include "superHeader.ftl">


<div class= "row" style="margin-top:66px">
 Following example data sets have been generating including a revision graph:

<ul>
 	<#list graphs as graphName>
	<li>${graphName}</li>
	</#list>
</ul>

<a class="button small default" href="sparql">Back to Endpoint</a>
</div>

<#include "superFooter.ftl">
