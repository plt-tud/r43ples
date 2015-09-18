<#include "superHeader.ftl">
   

<div class="row">
	<div id="main" class="small-12 columns">
		<h1><img class="center-block" src="/static/images/r43ples-logo.v04.png" style="width:100%; max-width:800px;"alt="R43ples logo"></h1>
		R43ples (Revision for triples) is an open source Revision Management Tool for the Semantic Web.

		It provides different revisions of named graphs via a SPARQL interface. All information about revisions, changes, commits, branches and tags are stored in additional named graphs beside the original graph in an attached external triple store.
	</div>
</div>
<div class="row">
	<div class="small-6 columns stack button-group">
			<h2>Use R43ples</h2>
			<a class="button tiny" href="sparql"><i class="fa fa-edit"></i> Perform R43ples queries on Endpoint</a>
      <a class="button tiny" href="debug"><i class="fa fa-stethoscope"></i> Debug attached triplestore</a>
      <a class="button tiny" href="merging"><i class="fa fa-magic"></i> Merge branches on the web</a>
    </div>
    <div class="small-6 columns stack button-group">
    	<h2>More information</h2>           
      <a class="button tiny" href="http://plt-tud.github.io/r43ples/"><i class="fa fa-globe"></i> More information on the website</a>
	    <a class="button tiny" href="https://github.com/plt-tud/r43ples"><i class="fa fa-github"></i> Development on GitHub</a>
	    <br>
	    <em> <#if version??>Version: ${version!"  "}<#else>Git: ${gitCommit!" "} - ${gitBranch!" "}</#if> </em>
	</div>
</div>

<#include "superFooter.ftl">
