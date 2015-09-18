<!DOCTYPE html>
    <!--[if IE 9]><html class="lt-ie10" lang="en" > <![endif]-->
<html class="no-js" lang="en" >

<head>
    <meta charset="utf-8">
    <!-- If you delete this meta tag World War Z will become a reality -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Graph View</title>
    <meta name="author" content="Markus Graube">
    <meta name="description" content="R43ples web application">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
          
      <!-- Foundation 5 core CSS -->
    <link href="/static/css/foundation.css" rel="stylesheet" />
    <link href="/static/css/normalize.css" rel="stylesheet" />
    <link rel="stylesheet" href="//cdn.jsdelivr.net/fontawesome/4.3.0/css/font-awesome.css">
    
    <!-- Custom styles for this template -->
    <link href="/static/css/r43ples.css" rel="stylesheet" />
    
    <script src="//cdn.jsdelivr.net/jquery/2.1.3/jquery.min.js"></script>
    
    <!--foundation.js-->
    <script src="/static/js/foundation.js"></script>


    <!--CSS Dateien für den Version Graph, tipsy und Font Awesome für Symbole -->
    <link rel="stylesheet" href="//cdn.jsdelivr.net/tipsy/1.0/stylesheets/tipsy.css">
    <link rel="stylesheet" href="/static/css/version-graph.css">
    
    <!--Einbinden der externen Bibliotheken-->
    <!--D3.js-->
    <script src="//cdn.jsdelivr.net/d3js/3.5.5/d3.min.js"></script>
    <!--dagre-d3-->
    <script src="//cpettitt.github.io/project/dagre-d3/latest/dagre-d3.min.js"></script>

    <!--tipsy-->
    <script src="//cdn.jsdelivr.net/tipsy/1.0/javascripts/jquery.tipsy.js"></script>

	<!--Version Graph-->
	<script src="/static/js/version-graph.js"></script>

	<script type="text/javascript">
    // Verknüpfung der UI Elemente mit dem Version Graph über jQuery
    //$(document).foundation();      
        $(document).foundation();
        $(document).ready(function () {
            // Elemente in Variablen Speichern
            var toogleBranches = $('#toogle-branches');
            var toogleTags = $('#toogle-tags');
            //var dataSource = $('#datasource');
            
            // Graph mit der aktuellen Auswahl erstellen
            drawGraph("revisiongraph?graph=${graphName}&format=application/json",toogleBranches.prop("checked"),toogleTags.prop("checked"));
            
            // ChangeListener für die Tags
            toogleTags.change(function () {
                $(this).prop('checked')?showTags():hideTags();
            });
            
            // ChangeListener für die Branches
            toogleBranches.change(function () {
                $(this).prop('checked')?showBranches():hideBranches();
            });
        });
    </script>

    <style>
        .wsmall{
             -webkit-transform: scale(0.8,0.8); /* Safari and Chrome */
         }
    </style>

</head>
<body> 
    <#include "superNav.ftl">
     
    <div class= "wsmall">  
        <div class="row">
            <div class= "large-10 large-offset-1 columns">
                <h1>Revision Graph</h1>
                <h2><samp>&lt;${graphName}&gt;</samp></h2>
            <!--SVG-Element für den Version Graph-->
            	<div id="visualisation">
                  <div class="row">
            		<svg width="100%" height="580" style="border:1px solid #000000; margin: 5px; overflow:hidden;"></svg>
                  </div>
                  </br>
            	  <div class="row">
                     <label ><em style="font-size:1.266rem">View:</em></label>
            		 <input type="checkbox" id="toogle-tags" /><label for="toogle-tags" >Tags</label> 
            		 <input type="checkbox" id="toogle-branches"/><label for="toogle-branches" >Branches</label> 
            	  </div>	
            	</div>
            </div>
        </div>
        <div class="row">
            <div class= "large-10 large-offset-1 columns">
            	<br/>
            	<a class="button small default" href="sparql">Back to Endpoint</a>
            </div>
        </div>
    </div>
</body>

</html>



