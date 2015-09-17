<#include "superHeader.ftl">

<script src="//cdn.jsdelivr.net/jquery/2.1.3/jquery.min.js"></script>
		
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

	h5 {
    font-size: 1.125rem;
    font-weight: bold;
	}

	.accordion-navigation .button {
		margin: 0rem;
	}
</style> 


<script type="text/javascript">
		
		$(document).ready(function(){
			$('.accordion-navigation :button').click(function(){
			var query = $(this).siblings('pre').text();

			$("#query").val(query);

				//alert(query);
			});
					    
		});
		
</script>

<div class="row">
	<div id="main" class="columns small-6">
		<div class="row">
					
			<div class="columns small-12">
				<h2>Query Form</h2>
				<form action="/r43ples/sparql" method="post" role="form">
					<div class="row">
						<div class="columns small-12">
							<label for="query" style= "font-weight: bold">Query Text</label><br />
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
	
	<div id="examples" class="columns small-6" style="margin-top: 1.2rem;">
		<h2>Example Queries</h2>
		<ul class="accordion" data-accordion style= "margin-top: 2rem;">
		  <li class="accordion-navigation">
				<a href="#panel1a"><h5>Create Graph under Version Control</h5></a>
				<div id="panel1a" class="content active">
					<pre>CREATE SILENT GRAPH &lt;http://test.com/r43ples-dataset-new&gt;</pre>
					<hr/>
					<button class='button default tiny expand'><span class='fa fa-reply'</span> copy to form</button>
				</div>
			</li>

			<li class="accordion-navigation">
				<a href="#panel2a"><h5>Select Query</h5></a>
				<div id="panel2a" class="content">
				<pre>SELECT * 
FROM &lt;http://test.com/r43ples-dataset-1&gt; REVISION "3"
WHERE {
 ?s ?p ?o. 
}</pre>
				<hr/>
					<button class='button default tiny expand'><span class='fa fa-reply'</span> copy to form</button>
				</div>
			</li>

			<li class="accordion-navigation">
				<a href="#panel3a"><h5>Select Query II</h5></a>
				<div id="panel3a" class="content">
				<pre>SELECT * 
WHERE {
  GRAPH &lt;http://test.com/r43ples-dataset-1&gt; REVISION "3" {
    ?s ?p ?o.
  } 
}</pre>
				<hr/>
				<button class='button default tiny expand'><span class='fa fa-reply'</span> copy to form</button>
				</div>
			</li>

			<li class="accordion-navigation">
				<a href="#panel4a"><h5>Select Query - Multiple Graphs</h5></a>
				<div id="panel4a" class="content">
				<pre>OPTION r43ples:SPARQL_JOIN
SELECT ?s ?p ?o 
FROM &lt;http://test.com/r43ples-dataset-1&gt; REVISION "master"
FROM &lt;http://test.com/r43ples-dataset-2&gt; REVISION "2"
WHERE {
  ?s ?p ?o. 
}</pre>
				<hr/>
					<button class='button default tiny expand'><span class='fa fa-reply'</span> copy to form</button>
				</div>
			</li>

			<li class="accordion-navigation">

				<a href="#panel5a"><h5>Update Query</h5></a>
				<div id="panel5a" class="content">
					<pre>USER "mgraube"
MESSAGE "test commit"
INSERT DATA {
	GRAPH &lt;http://test.com/r43ples-dataset-1&gt; REVISION "4" {
  			&lt;a&gt; &lt;b&gt; &lt;c&gt; .
	}
}</pre>
					<hr/>
					<button class='button default tiny expand'><span class='fa fa-reply'</span> copy to form</button>
				</div>
			</li>

			<li class="accordion-navigation">
				<a href="#panel6a"><h5>Branching</h5></a>
				<div id="panel6a" class="content">
				<pre>USER "mgraube"
MESSAGE "test branch commit"
BRANCH GRAPH &lt;http://test.com/r43ples-dataset-1&gt; REVISION "2" TO "unstable"</pre>
				<hr/>
					<button class='button default tiny expand'><span class='fa fa-reply'</span> copy to form</button>
				</div>
			</li>
			<li class="accordion-navigation">
				<a href="#panel7a"><h5>Tagging</h5></a>
				<div id="panel7a" class="content">
				<pre>USER "mgraube"
MESSAGE "test tag commit"
TAG GRAPH &lt;http://test.com/r43ples-dataset-1&gt; REVISION "2" TO "v0.3-alpha"</pre>
				<hr/>
					<button class='button default tiny expand'><span class='fa fa-reply'</span> copy to form</button>
				</div>
			</li>
		</ul>
	</div>

</div>

<#include "superFooter.ftl">
