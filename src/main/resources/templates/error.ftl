<#include "superHeader.ftl">

<div class="row">
<div id="main" class="large-7 columns">
	<h2>Internal Error</h2>
	<h3>Message</h3>
	  ${error}
	<!-- {{ error.toString }} -->
	<h3>Stacktrace</h3>
	<!-- {{# error.stackTrace }}
	  {{ toString }}
	{{/ error.stackTrace }} -->
	<#list error.stackTrace as trace>							     
  	${trace}                      
  </#list>
	
</div>
</div>

<#include "superFooter.ftl">