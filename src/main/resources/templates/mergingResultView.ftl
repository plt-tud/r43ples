<!DOCTYPE html>
	<!--[if IE 9]><html class="lt-ie10" lang="en" > <![endif]-->
<html class="no-js" lang="en" >

<head>
	  	<meta charset="utf-8">
	    <!-- If you delete this meta tag World War Z will become a reality -->
	  	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	    <title>Merge View</title>
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
    		
  	  
      <link rel="stylesheet" href="//cdn.jsdelivr.net/tipsy/1.0/stylesheets/tipsy.css">
      <!--<link rel="stylesheet" href="/static/css/version-merging-graph.css">-->
      <link rel="stylesheet" href="/static/css/process-graph.css">


     <!-- <link rel="stylesheet" href="https://cdn.datatables.net/1.10.6/css/jquery.dataTables.css"> -->
     <!--  <link rel="stylesheet" href="https://cdn.datatables.net/plug-ins/1.10.6/integration/foundation/dataTables.foundation.css">-->
      <link rel="stylesheet" href="/static/css/foundation.table.css">

      <!--jsTree-->
      <script src="/static/js/jstree.min.js"></script>
      <link rel="stylesheet" href="/static/css/style.min.css" />
        <!--Einbinden der externen Bibliotheken-->
    	<!--D3.js-->
    	<script src="//cdn.jsdelivr.net/d3js/3.5.5/d3.min.js"></script>
    	<!--dagre-d3-->
    	<script src="//cpettitt.github.io/project/dagre-d3/latest/dagre-d3.min.js"></script>

    	<!--tipsy-->
    	<script src="//cdn.jsdelivr.net/tipsy/1.0/javascripts/jquery.tipsy.js"></script>
    	<!--Version Graph-->
  
        


      <style>
      
    		.wsmall{
    		  -webkit-transform: scale(0.8,0.8); /* Safari and Chrome */
    		}
      </style>
	  

</head>

<body>  

      <script src="/static/js/vor-fast-forward-graph.js"></script>
      <script src="/static/js/fast-forward-graph.js"></script>   

      <script type="text/javascript">

        $(document).foundation();      
        $(document).ready(function(){  

          // Elemente in Variablen Speichern
          var toogleBranchesLeft = $('#toogle-branches-left');
          var toogleTagsLeft = $('#toogle-tags-left');
          //var dataSource = $('#datasource');
          
          // Graph mit der aktuellen Auswahl erstellen
          drawGraphOld("loadOldGraphProcess?graph=${graphName}&optradio=application/json",toogleBranchesLeft.prop("checked"),toogleTagsLeft.prop("checked"));
          
          
          // ChangeListener für die Tags
          toogleTagsLeft.change(function () {
              $(this).prop('checked')?showTags():hideTags();
          });
          
          // ChangeListener für die Branches
          toogleBranchesLeft.change(function () {
              $(this).prop('checked')?showBranches():hideBranches();
          });    
            


          // Elemente in Variablen Speichern
          var toogleBranchesRight = $('#toogle-branches-right');
          var toogleTagsRight = $('#toogle-tags-right');
          //var dataSource = $('#datasource');
          
          // Graph mit der aktuellen Auswahl erstellen
          drawGraph("mergingProcess?graph=${graphName}&optradio=application/json",toogleBranchesRight.prop("checked"),toogleTagsRight.prop("checked"));
          
          
          // ChangeListener für die Tags
          toogleTagsRight.change(function () {
              $(this).prop('checked')?showTags1():hideTags1();
          });
          
          // ChangeListener für die Branches
          toogleBranchesRight.change(function () {
              $(this).prop('checked')?showBranches1():hideBranches1();
          });

           // initial the size of svg
          var svgInitial = $("#right");
          $("#right").remove();
          svgInitial.width("100%");
          $("#visualisation-right").prepend(svgInitial);
        });
       
       
      </script>


        
      

 

  <#include "superNav.ftl">

  <!-- body content here -->
   
    <div class="row" style="margin-top:66px">  

      <div class="columns small-5 push-1" id= "leftContainer" >

        <fieldset style="padding-bottom:0px;" >
          <legend><strong>Graph before Merging</strong></legend>
          <div id="visualisation-left" class="live map">
            <svg id="left"  width="100%" height="468" style="border:1px solid #000000; margin:1px; overflow:hidden;"><g/></svg>
          </div> 
          <div class="form" style="margin:0px;padding-bottom:0px">
            <input type="checkbox" id="toogle-tags-left" class="checkbox-inline"><label for="toogle-tags" class="checkbox-inline">Tags</label>
            <input type="checkbox" id="toogle-branches-left" checked class="checkbox-inline"><label for="toogle-branches" class="checkbox-inline">Branches</label>
          </div> 
        </fieldset> 

      </div>

      <div class = "columns small-5 pull-1" id="rightContainer" >

        <fieldset style="padding-bottom:0px;" >
          <legend><strong>Graph after Merging</strong></legend>
          <div id="visualisation-right" class="live map">
            <svg id="right"  width="100%" height="468" style="border:1px solid #000000; margin:1px; overflow:hidden;"><g/></svg>
          </div> 
          <div class="form" style="margin:0px;padding-bottom:0px">
            <input type="checkbox" id="toogle-tags-right" class="checkbox-inline"><label for="toogle-tags" class="checkbox-inline">Tags</label>
            <input type="checkbox" id="toogle-branches-right" checked class="checkbox-inline"><label for="toogle-branches" class="checkbox-inline">Branches</label>
          </div> 
        </fieldset> 

      </div>
      
    </div>  
    <div class= "row ">
      <div class = "small-10 push-1 columns">
        <fieldset>
           <legend><strong>Merging Report</strong></legend>
           </br>
           <div class = "small-6 columns">
              <div class="row collapse prefix-radius" >
                <div class="small-2 columns" >
                  <span class="prefix"><strong>GRAPH:</strong></span>
                </div>
                <div class="small-10 columns" >
                  <input type="text" placeholder = ${commit.graphName} disabled ></input>
                </div>
              </div>
              <div class="row collapse prefix-radius" >
                <div class="small-2 columns" >
                  <span class="prefix"><strong>Strategie:</strong></span>
                </div>
                <div class="small-10 columns" >
                  <input type="text" placeholder= ${commit.strategy} disabled></input>
                </div>
              </div>
              <div class="row collapse prefix-radius" >
                  <div class="small-2 columns" >
                    <span class="prefix"><strong>SDD:</strong></span>
                  </div>
                  <div class="small-10 columns">          
                    <input type="text" placeholder= ${commit.sddName} disabled></input>
                  </div>
              </div>
              <div class="row collapse prefix-radius">
                <div class="small-2 columns">
                  <span class="prefix"><strong>MERGE:</strong></span>
                </div>
                <div class="small-3 columns">
                   <input type="text" placeholder= ${commit.branch1} disabled></input>        
                </div>
                <div class="small-2 small-offset-2 columns">
                  <span class="prefix"><strong>INTO:</strong></span>
                </div>
                <div class="small-3 columns">
                   <input type="text" placeholder=${commit.branch2} disabled></input>         
                </div>
              </div>
            </div>
            <div class = "small-6 columns">
              <div class="row collapse prefix-radius">
                  <div class="small-2 columns">
                    <span class="prefix"><strong>USER:</strong></span>
                  </div>
                  <div class="small-10 columns">
                    <input type="text" placeholder=${commit.user} id="user" name="user" disabled></input>
                  </div>
              </div>    
              <div class="row">
                   <label class="small-2 columns"><div align=center><strong>MESSAGE:</strong></div></label>
                   <div class="small-10 columns">
                    <textarea class="form-control" rows="2" id="message" name="message" placeholder=${commit.message} disabled></textarea>
                   </div>

              </div> 
              <hr/>
              <div class="row">
                   <a href="merging" ><button style="width:95%; margin-left:16px" type="button" class="button tiny expand radius" >New Merge</button></a>
              </div> 
              
          </div>

        </fieldset>
      </div>
    </div>
           
</body>
</html>       

  

