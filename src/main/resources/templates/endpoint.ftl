<#include "superHeader.ftl">


<style>

	li {
		display: inherit;
		
	}

	.row .row {
    width: auto;
    margin-left: -0.9375rem;
    margin-right: -0.9375rem;
    margin-top: 1em;
    margin-bottom: 0;
    max-width: none;
	}

</style> 


<script type="text/javascript">
 //  $(function() {
	// $i=1;
	// $(".accordion > div").addClass("panel panel-default");
	// $(".accordion > div").html(function() {
 // 		  var text =	"<div class='panel-heading button tiny' data-toggle='collapse' data-parent='.accordion' href='#collapse"+$i+"'>"+
	// 	  					"<h3 class='panel-title'>" + $(this).children("h3").text() + "</h3>" +
	//   					"</div>" +
 //  						"<div id='collapse"+$i+"' class='panel-collapse collapse'>"+
 //  							"<div class='panel-body'>" +
 //  								"<button class='copy button tiny default'><span class='fa fa-reply'</span> copy to form</button>" +
 //  								"<pre>" + $(this).children("pre").html() + "</pre>" +
 //  								"</div>" +		
 //  							"</div>" +
	//   					"</div>";
	// 	  $i++;
	// 	  return text;
	// });
	// $(".accordion .panel-collapse").first().addClass("in");
 //  	$(".copy").click(function() {
	// 	$("#query").val($(this).next().text());
 //  	}); 
 //  });
  $(document).foundation();
  $(document).foundation('accordion', 'reflow');

</script>

<div class="row">
	<div id="main" class="columns small-6">
		<div class="row">
					
			<div class="columns small-12">
				<h2>Query Form</h2>
				<form action="/r43ples/sparql" method="post" role="form">
					<div class="row">
						<div class="columns small-12">
							<label for="query">Query Text</label><br />
							<textarea name="query" class="form-control" id="query" rows="16" placeholder="Insert R43ples/SPARQL query text or copy it from examples"></textarea>
						</div>
					</div>
					<div class="row">
						<label for="format" class="columns small-3">Results Format:</label>
						<div class="columns small-9">
							<select	name="format" class="form-control" id="name">
								<option value="text/html">HTML</option>
								<option value="text/plain">Text</option>
								<option value="application/sparql-results+xml">RDF/XML</option>
								<option value="text/turtle">Turtle</option>
								<option value="application/json">JSON</option>
							</select>
						</div>
					</div>
					<div class="row">
						<label for="join_option" class="columns small-3" >JOIN Option:</label>
						<div class="columns small-9">
							<input type="checkbox" name="join_option" value="true">
						</div>
					</div>
					<div class="row">
						<div class="columns push-3 small-9 button-group radius">
							<input type="submit" class="columns small-8 button tiny primary" value="Run Query" />
							<input type="reset"	class="columns small-4 button tiny default" value="Reset" />
						</div>
					</div>
				</form>
			</div>
		</div>
		<div class="row">
					
		<div class="columns small-12">
				<h2>Revision Information</h2>			
				<ul>
					<li>
						<form action="createSampleDataset" method="get">
							<div class="row">
								<div class="columns small-8">
									<select id="exampleGraph" name="dataset">
										<option value="all">All</option>
										<option value="1">http://test.com/r43ples-dataset-1</option>
										<option value="2">http://test.com/r43ples-dataset-2</option>
										<option value="merging">http://test.com/r43ples-dataset-merging</option>
										<option value="merging-classes">http://test.com/r43ples-dataset-merging-classes</option>
										<option value="renaming">http://test.com/r43ples-dataset-renaming</option>
										<option value="complex-structure">http://test.com/r43ples-dataset-complex-structure</option>
										<option value="rebase">http://test.com/r43ples-dataset-rebase</option>
										<option value="forcerebase">http://test.com/r43ples-dataset-force-rebase</option>
										<option value="fastforward">http://test.com/r43ples-dataset-fastforward</option>
									</select>
								</div>
								<div class="columns small-4">
        							<input type="submit" class="button tiny default radius" value="Create Samples" />
        						</div>
							</div>
						</form>
					</li>
					<li>
						<form action="revisiongraph" method="get">
							<div class="row">
								<div class="columns small-4">
									<select id="selectRevisedGraph" name="graph">
										<option value="">(All)</option>
										<#list graphList as graph>
										<option value="${graph}">${graph}</option>
										</#list>
									</select>
								</div>
								<div class="columns small-4">
									<select name="format">
										<option value="text/turtle">Turtle</option>
										<option value="batik">Graphical 1</option>
										<option value="d3">Graphical 2</option>
									</select>
								</div>
								<div class="columns small-4">
									<input type="submit" class="button tiny default radius" value="Get Revision Graph" />
								</div>
							</div>
						</form>
					</li>
					<li >
						<form action="sparql" method="get">
							<div class="row">
								<div class="columns small-8">
									<select id="dropRevisedGraph" name="query">
										<option value="">(None)</option>
										<#list graphList as graph>
										<option value="DROP GRAPH &lt;${graph}&gt;">${graph}</option>
										</#list>	
									</select>
								</div>
								<div class="columns small-4">
									<input type="submit" class="button tiny default radius" value="Drop Graph" />
								</div>
							</div>
						</form>
					</li>
				</ul>
				</div>
				</div>
	</div>
	
	<div id="examples" class="columns small-6">
		<h2>Example Queries</h2>
		<ul class="accordion" data-accordion>
		    <li class="accordion-navigation">
				<a href="#panel1a"><h3>Create Graph under Version Control</h3></a>
				<div id="panel1a" class="content active">
					<pre>CREATE SILENT GRAPH &lt;http://test.com/r43ples-dataset-new&gt;</pre>
				</div>
			</li>

			<li class="accordion-navigation">
				<a href="#panel2a"><h3>Select Query</h3></a>
				<div id="panel2a" class="content">
				<pre>SELECT * 
					FROM &lt;http://test.com/r43ples-dataset-1&gt; REVISION "3"
					WHERE {
						?s ?p ?o. 
					}</pre>
				</div>
			</li>

			<li class="accordion-navigation">
				<a href="#panel3a"><h3>Select Query II</h3></a>
				<div id="panel3a" class="content">
				<pre>SELECT * 
					WHERE {
					  GRAPH &lt;http://test.com/r43ples-dataset-1&gt; REVISION "3" {
					    ?s ?p ?o.
					  } 
					}</pre>
				</div>
			</li>

			<li class="accordion-navigation">
				<a href="#panel4a"><h3>Select Query - Multiple Graphs</h3></a>
				<div id="panel4a" class="content">
				<pre>OPTION r43ples:SPARQL_JOIN
					SELECT ?s ?p ?o 
					FROM &lt;http://test.com/r43ples-dataset-1&gt; REVISION "master"
					FROM &lt;http://test.com/r43ples-dataset-2&gt; REVISION "2"
					WHERE {
					  ?s ?p ?o. 
					}</pre>
				</div>
			</li>

			<li class="accordion-navigation">

				<a href="#panel5a"><h3>Update Query</h3></a>
				<div id="panel5a" class="content">
					<pre>USER "mgraube"
					MESSAGE "test commit"
					INSERT DATA {
					  GRAPH &lt;http://test.com/r43ples-dataset-1&gt; REVISION "4" {
					    &lt;a&gt; &lt;b&gt; &lt;c&gt; .
					  }
					}</pre>
				</div>
			</li>

			<li class="accordion-navigation">
				<a href="#panel6a"><h3>Branching</h3></a>
				<div id="panel6a" class="content">
				<pre>USER "mgraube"
					MESSAGE "test branch commit"
					BRANCH GRAPH &lt;http://test.com/r43ples-dataset-1&gt; REVISION "2" TO "unstable"
				</pre>
				</div>
			</li>
			<li class="accordion-navigation">
				<a href="#panel7a"><h3>Tagging</h3></a>
				<div id="panel7a" class="content">
				<pre>USER "mgraube"
					MESSAGE "test tag commit"
					TAG GRAPH &lt;http://test.com/r43ples-dataset-1&gt; REVISION "2" TO "v0.3-alpha"
				</pre>
				</div>
			</li>
		</ul>
	</div>

</div>

<#include "superFooter.ftl">
