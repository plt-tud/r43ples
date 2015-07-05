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
    	<script src="/static/js/process-graph.js"></script>

    	
        <!--DataTable-->
        
      <script src="https://cdn.datatables.net/1.10.6/js/jquery.dataTables.min.js"></script>
      <script src="https://cdn.datatables.net/plug-ins/1.10.6/integration/foundation/dataTables.foundation.js"></script>

      <style>
        .parentTbl table {
          border-spacing: 0;
          border-collapse: collapse;
          border: 0;
          width: 690px;
        }
        .childTbl table {
          border-spacing: 0;
          border-collapse: collapse;
          border: 1px solid #d7d7d7;
          width: 665px;
        }
        
        .childTbl td {
          border: 1px solid #d7d7d7;
          text-align: center;
        }
        
        .childTbl th {
		  border: 2px solid black;
		  text-align: center;
		}
        .scrollData {
          width: 690;
          height: 366px;
          overflow-x: hidden;
        }


        .childTbl td:hover{
            cursor: pointer;
        }  

        .childTbl tr:hover{
            background-color: #ccc;
        }  

        #example tr {
            background-color: #eee;
            border-top: 1px solid #fff;
        }
        #example tr:hover {
            background-color: #ccc;
        }
        #example th {
            background-color: #fff;
        }
        #example th, #example td {
            padding: 3px 5px;
        }
        #example td:hover {
            cursor: pointer;
        } 
        
        table {
        	border-color: black;
        }    
        table tr td {
		  padding: 0rem;
		  font-size: 1rem;
		  margin-bottom: 0.5rem;
		}
		
		table tr th {
			padding: 0.3rem;
			font-size: 1rem;	
		}
			
		button, .button {	 
		  margin-top: 0.5rem;
		  margin-bottom: 0.5rem;
		}
		
		.wsmall{
		  -webkit-transform: scale(0.8,0.8); /* Safari and Chrome */
		}
      </style>
	  

 
</head>

<body>       
      <script type="text/javascript">
        $(document).foundation();      
        $(document).ready(function(){


            // von report seite go back , triple table bleibt
            $(window).load(function() {
              var checked = [];
              var properties;
              $("input[name='pFilter']:checked").each(function (){
                    checked.push($(this).val());
                });

              properties = checked.join(",");

              $.post("filterProcess",
                {
                  properties: properties
                },
                function(data, status){
                  $("#tripleTable").empty();
                  $("#tripleTable").prepend(data);
                  $("#tripleTable :button").each(function (){
                     if($(this).text() == "Approved") {
                        $(this).parent().prev().children().prop({"disabled":true});
                        $(this).parent().parent().css('background','green');
                     }else{
                        $(this).parent().prev().children().prop({"disabled":false});
                        $(this).parent().parent().css('background','white');
                     }
                  }); 

              }); 
            });
            
            // Elemente in Variablen Speichern
            var toogleBranches = $('#toogle-branches');
            var toogleTags = $('#toogle-tags');
            //var dataSource = $('#datasource');
            
            // Graph mit der aktuellen Auswahl erstellen
            drawGraph("mergingProcess?graph=${graphName}&optradio=application/json",toogleBranches.prop("checked"),toogleTags.prop("checked"));
            
            //test merging-process-graph
            //drawProcess();

            
            // ChangeListener für die Tags
            toogleTags.change(function () {
                $(this).prop('checked')?showTags():hideTags();
            });
            
            // ChangeListener für die Branches
            toogleBranches.change(function () {
                $(this).prop('checked')?showBranches():hideBranches();
            });
            
            //DataTable
            // var table = $('#example').DataTable( {
            // "scrollY": "366px",
            // "paging": true
            //  } );
            // var table = $('#example1').DataTable( {
            // "scrollY": "309.5px",
            // "paging": false
            // } );

            // $('a.toggle-vis').on( 'click', function (e) {
            // e.preventDefault();
     
            // // Get the column API object
            // var column = table.column( $(this).attr('data-column') );
     
            // // Toggle the visibility
            // column.visible( ! column.visible() );
            // } );

            //jsTree
            // get checkbox and delete the file icon
            $('#diffTree').jstree({ plugins : ["checkbox","sort","types","wholerow"], "core" : { "themes" : { "icons" : false } } });

            //get resolutionstate of Triples
            $("#push").click(function(){
              var checked = [];
              var options;
              $("input[name='options']:checked").each(function (){
                 // checked.push(parseInt($(this).val()));
                  checked.push($(this).val());
              });
              options = checked.join(",");
   //           $("#opt").val(options);

            //  $.post("pushProcess",{options: options});
              $.post("pushProcess",
              {
                  options: options
              },
              function(data, status){
                if (confirm("Push process : " + status + "Show push result ?") == true) {
                    window.location.assign("pushProcess");
                } else {
                    alert("stop!");
                }

              });

            });



            //change to individual view
            $("#individual").click(function(){
              $("#left").hide();
              $("#right").removeClass();
              $("#right").addClass("large-12 columns");
              $("#tripleView").hide();
              $("#highLevelView").hide();
              // $("#right").css("width", "100%");
              $("#individualView").load("individualView",function(){
                $("#individualView").show();
      //          window.location.href = "#";
              });
            });

            //change to triple view
            $("#triple").click(function(){
              $("#individualView").hide();
              $("#highLevelView").hide();
              $("#left").show();
              $("#right").removeClass();
              $("#right").addClass("large-9 columns");
              //updated tripleView
              $("#tripleView").children().remove();
              $("#tripleView").load("tripleView",function(){
                $("#tripleView").show();
                
                $("#tripleTable :button").each(function (){
                   if($(this).text() == "Approved") {
                      $(this).parent().prev().children().prop({"disabled":true});
                      $(this).parent().parent().css('background','green');
                   }else{
                      $(this).parent().prev().children().prop({"disabled":false});
                      $(this).parent().parent().css('background','white');
                   }
                });

              });

            //  $("#right").css("width", "");

             // debugger;
              // var graphDom = $("#right").children("fieldset:eq(0)");
              // $("#right").children("fieldset:eq(0)").remove();
              // graphDom.find("#visualisation #svg").width(500);
              // $("#right").prepend(graphDom);

              //each change view initial size of svg
              var graphDom = $("#svg");
              $("#svg").remove();
              graphDom.width("100%");
              $("#visualisation").prepend(graphDom);
  //            $("#example_wrapper").width("100%");

   //           table.columns.adjust().draw();
     //         window.location.href = "#";              
            });
            

            $("#highLevel").click(function(){
              $("#left").hide();
              $("#right").removeClass();
              $("#right").addClass("large-12 columns");
              $("#tripleView").hide();
              $("#individualView").hide();
              $("#highLevelView").load("highLevelView", function(){
                $("#highLevelView").show();
      //          window.location.href = "#";
                $("#highLevelTable :button").each(function (){
                   if($(this).text() == "Approved") {
                      $(this).parent().prev().children().prop({"disabled":true});
                      $(this).parent().parent().css('background','green');
                   }else{
                      $(this).parent().prev().children().prop({"disabled":false});
                      $(this).parent().parent().css('background','white');
                   }
                });

              });

            });

            // initial the size of svg
            var svgInitial = $("#svg");
            $("#svg").remove();
            svgInitial.width("100%");
            $("#visualisation").prepend(svgInitial);


            // filter table checked , filter TripleTable
            var property = $("#propertyBody").find("input");
            property.click(function(){
              var checked = [];
              var properties;
              $("input[name='pFilter']:checked").each(function (){
                    checked.push($(this).val());
                });
                properties = checked.join(",");
                alert(properties);
                $.post("filterProcess",
                  {
                    properties: properties
                  },
                  function(data, status){
                    $("#tripleTable").empty();
                    $("#tripleTable").prepend(data);
                    $("#tripleTable :button").each(function (){
                       if($(this).text() == "Approved") {
                          $(this).parent().prev().children().prop({"disabled":true});
                          $(this).parent().parent().css('background','green');
                       }else{
                          $(this).parent().prev().children().prop({"disabled":false});
                          $(this).parent().parent().css('background','white');
                       }
                    });
                            

                  }); 
                
            });

            //difference tree controle the inhalt von triple table 
            $('#diffTree').click(function(){
              var diffNode = $('#diffTree li .jstree-leaf');
              var checked = [];
              var triples = null;
              diffNode.each(function(index){
                if(($(this).attr('aria-selected')) == 'true'){
                  checked.push($(this).find('a').text());
                }
              });
              triples = checked.join(',');

              //post to server to select the triple table
              $.post("treeFilterProcess", 
                {
                  triples : triples
                },
                function(data, status){
                  $("#tripleTable").empty();
                  $("#tripleTable").prepend(data);
                  $("#tripleTable :button").each(function (){
                     if($(this).text() == "Approved") {
                        $(this).parent().prev().children().prop({"disabled":true});
                        $(this).parent().parent().css('background','green');
                     }else{
                        $(this).parent().prev().children().prop({"disabled":false});
                        $(this).parent().parent().css('background','white');
                     }
                  });

                });

            });


            //Table row clicked, color change
            // $("#tripleTable").find("tr").click(function() {
            //   if(this.style.background == "" || this.style.background =="white") {
            //       $(this).css('background', 'green');
            //   }
            //   else {
            //       $(this).css('background', 'white');
            //   }
            // });

            $("#tripleTable :button").click(function(){
              var box = $(this).parent().prev().children();
              var id = box.val();
              var isChecked;
              alert(box.is(':checked'));
              if(box.is(':checked')){
                //triple added
                isChecked = 1;
              }else{
                //triple deleted
                isChecked = 0;
              }

              if(box.attr("disabled")){
                box.prop({"disabled":false});
                $(this).parent().parent().css('background','white');
                $(this).text("Confirm");
              }else{
                box.prop({"disabled":true});
                $(this).parent().parent().css('background','green');
                $(this).text("Approved");
              }
              
              $.post("approveProcess",
                  {
                    id: id,
                    isChecked: isChecked
                  }
              );

            });

            $("#allSelect").click(function(){
              $("#tripleTable input[name='options']").each(function(){
                var id = $(this).val();
                var isChecked;

                if($(this).is(':checked')){
                  //triple added
                  isChecked = 1;
                }else{
                  //triple deleted
                  isChecked = 0;
                }

                if($(this).attr("disabled")){
                  // $(this).prop({"disabled":false});
                  // $(this).parent().parent().css('background','white');
                }else{
                  $(this).prop({"disabled":true});
                  $(this).parent().parent().css('background','green');
                  $(this).parent().next().children().text("Approved");
                  $.post("approveProcess",
                    {
                      id: id,
                      isChecked: isChecked
                    }
                  );
                }

              });

            });


        });
      </script>

  <#include "superNav.ftl">

  <!-- body content here -->
      
    <div class="container wsmall" style="margin-top:-66px;">  
        <div class="row panel radius" style="background-color:white;width:100%" >

          <!--Merging Client-->
             <fieldset style="padding-top:0px" >
             <legend><h3><strong>R43ples Merge</strong></h3></legend>

                    <!--Button grouo-->
             <div class="row" style="padding-left:16px;padding-right:16px">
                <div class="columns small-5" >
                      <ul class="button-group radius left" style="margin-top:16px">
                        <li><a href="merging" class="button tiny">New Merge</a></li>
                        <!--click push to push Triple and resolution state-->
                        <#if isRebase>
                          <li><a href ="rebaseReportProcess"><button id="Report" class="button tiny">Report</button></a></li>
                        <#else>
                          <li><a href ="reportProcess"><button id="Report" class="button tiny">Report</button></a></li>
                        </#if>                       
                    <!--    <li><a href="#" class="button tiny">Push</a></li> -->
                      </ul>
                </div>

                <div class="columns small-6">
                      <ul class="button-group radius right" style="margin-top:16px">
                        <li><a href="#" id="triple" class="button tiny">Triple view</a></li>
                        <li><a href="#" id="individual" class="button tiny">Individual</a></li>
                        <li><a href="#" id="highLevel" class="button tiny">High level</a></li>              
                      </ul>
                </div>
             </div> 

             
              
              <div id = "left" class="large-3 columns" style="margin:0px;padding:0px;">
                <fieldset style="padding-top:0px;padding-bottom:6px;height:519px;" >
                  <legend><strong>Differences</strong></legend>
                  <div id="diffTree" style="overflow:auto; height:486px; width:266px; padding:0px">
                    <#if conStatus=="1">
                      <ul>
                          <li data-jstree='{"opened" : true}'><a><img src="/static/images/Conflict.png"/> Conflict</a>
                            <ul>
                              <#list conList as node>
                                <li ><a><img src="/static/images/Conflict.png"/> ${node.differenceGroup}</a>
                                  <ul>
                                      <#assign triples = node.tripleList>
                                      <#list triples as triple>
                                          <li ><a><img src="/static/images/Conflict.png"/> ${triple}</a></li>              
                                      </#list>
                                  </ul>
                                </li>          
                              </#list>
                            </ul>
                           </li>
                           
                           <li data-jstree='{"opened" : true}'><a><img src="/static/images/Difference.png"/> Difference</a>
                            <ul>
                              <#list diffList as node>
                                <li ><a><img src="/static/images/Difference.png"/> ${node.differenceGroup}</a>
                                  <ul>
                                      <#assign triples = node.tripleList>
                                      <#list triples as triple>
                                          <li ><a><img src="/static/images/Difference.png"/> ${triple}</a></li>              
                                      </#list>
                                  </ul>
                                </li>          
                              </#list>
                            </ul>
                           </li>       
                       </ul> 
                      
                     <#else>
                        <ul>
                           <li data-jstree='{"opened" : true}'><a><img src="/static/images/Difference.png"/> Difference</a>
                            <ul>
                              <#list diffList as node>
                                <li ><a><img src="/static/images/Difference.png"/> ${node.differenceGroup}</a>
                                  <ul>
                                      <#assign triples = node.tripleList>
                                      <#list triples as triple>
                                          <li ><a><img src="/static/images/Difference.png"/> ${triple}</a></li>              
                                      </#list>
                                  </ul>
                                </li>          
                              </#list>
                            </ul>
                           </li>       
                        </ul> 
                      </#if>                 
                  </div>   

                </fieldset>

                <fieldset style="padding-top:0px">
                  <legend><strong>Filters</strong></legend> 

                  <div class = "parentTbl">
                        <table  style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">
                          <tr>
                            <td>
                              <div class = "childTbl">
                                <table  style="width:100%; table-layout:fixed;word-break: break-all; word-wrap: break-word;" class = "childTbl">
                                  <tr>
                                    <th style = "width:50%">Property</th>
                                    <th style = "width:50%">Select</th>
                                  </tr>
                                </table>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td>
                              <div class="scrollData childTbl" style = "height:345.5px;">
                                <table id = "propertyBody" style="width:100%;table-layout:fixed;word-break: break-all; word-wrap: break-word;">
									
                	 								<#if propertyList??>
                									   <#list propertyList as property>
	                                    <tr>
	                                        <td >${property!" "}</td>
	                                        <td><input type="checkbox" name="pFilter"  value=${property!" "}></td>
	                                    </tr>
	                                  </#list>     
									               <#else>
										                  <tr>
	                                        <td >null</td>
	                                        <td>no property</td>
	                                    </tr>
									               </#if>
                                  
                                </table>
                              </div>
                            </td>
                          </tr>
                        </table>
                  </div>



                  <!--Filter by Datatable.js-->
              <!--<div class="row" style="margin:0px;padding:0px;">
                    <table id="example1" class="stripe compact" cellspacing="0" width="100%">
                          <thead>
                              <tr>
                                  <th>Property</th>
                                  <th>Select</th>
                              </tr>
                          </thead>
                   
                          <tbody id = "propertyBody">
							<#if propertyList??>
							 <#list propertyList as property>
                                <tr>
                                    <td >${property!" "}</td>
                                    <td><input type="checkbox" name="pFilter" checked value=${property!" "}></td>
                                </tr>
                              </#list>     
							<#else>
								<tr>
                                    <td >null</td>
                                    <td>no property</td>
                                </tr>
							</#if>                	                      
                          </tbody>

                          <tfoot>
                              <tr>
                                  <th>Property</th>
                                  <th>Select</th>
                              </tr>
                          </tfoot>
                      </table>
                  </div> -->
              </fieldset>
              </div>

              <div id="right" class="large-9 columns" style="margin:0px;padding:0px;">
                <fieldset style="padding-bottom:0px;" >
                  <legend><strong>Revision graph</strong></legend>
                  <div id="visualisation" class="live map">
                   <svg id="svg"  width="100%" height="368" style="border:1px solid #000000; margin:1px; overflow:hidden;"><g/></svg>
                  </div> 
                  <div class="form columns small-5 " style="margin:0px;padding-bottom:0px">
                    <input type="checkbox" id="toogle-tags" class="checkbox-inline"><label for="toogle-tags" class="checkbox-inline">Tags</label>
                    <input type="checkbox" id="toogle-branches" class="checkbox-inline"><label for="toogle-branches" class="checkbox-inline">Branches</label>
                  </div> 
                </fieldset> 

                <div id="individualView"></div> 
                <div id="highLevelView"></div>  

                <div id="tripleView">
                <fieldset style="padding-bottom:0px;padding-top:0px;margin-bottom:6px">
                    <legend><strong>Resolution</strong></legend>
                      <div id="allSelect" class="columns large-2 push-10"><button type="button" class="button tiny radius">Approve All</button></div>

                      <hr style="margin:8px;"/>
                      <div class = "parentTbl">
                        <table  style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">
                          <tr>
                            <td>
                              <div class = "childTbl">
                                <table  style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;" class = "childTbl">
                                  <tr>
                                    <th style = "width:12%">Subject</th>
                                    <th style = "width:12%">Predicate</th>
                                    <th style = "width:12%">Object</th>
                                    <th style = "width:18%">State B1</th>
                                    <th style = "width:18%">State B2</th>
                                    <th style = "width:9%">Conflicting</th>
                                    <th style = "width:9%">Resolution State</th>
                                    <th style = "width:10%">Approve</th>
                                  </tr>
                                </table>
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td>
                              <div id = "tripleTable" class="scrollData childTbl">
                                <table style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">

                                  <#list tableRowList as row>               
                                    <tr>
                                      <td style = "width:12%">${row.subject} </td>
                                      <td style = "width:12%">${row.predicate}</td>
                                      <td style = "width:12%">${row.object}</td>
                                      <td style = "width:18%">${row.stateA} (${row.revisionA!"  "})</td>
                                      <td style = "width:18%">${row.stateB} (${row.revisionB!"  "})</td>
                                      <#if row.conflicting == "1">
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Conflict.png"/></a></td>
                                      <#else>
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Difference.png"/></a></td>
                                      </#if>

                                      <#if row.resolutionState == "ADDED">
                                      <td style = "width:9% ;text-align: center;"><input type="checkbox" id ="opt" name="options" checked value=${row.tripleId}>
                                      </td>
                                      <#else>
                                      <td style = "width:9% ;text-align: center;"><input type="checkbox" id ="opt" name="options" value=${row.tripleId}>
                                      </td>
                                      </#if>

                                      <#if row.state == "RESOLVED">
                                        <td><button type="button" class="button tiny expand radius" >Approved</button></td>
                                      <#else>
                                        <td><button type="button" class="button tiny expand radius" >Confirm</button></td>
                                      </#if>
                                    </tr>
                                  </#list>   
                        
                                </table>

                              </div>
                            </td>
                          </tr>
                        </table>
                      </div>

                      <!--data table by DataTable.js-->
                  <!--<table id="example"  style="width:100%; ">
                          <thead >
                              <tr >
                                  <th>Subject</th>
                                  <th>Predicate</th>
                                  <th>Object</th>
                                  <th>State B1</th>
                                  <th>State B2</th>
                                  <th>Conflicting</th>
                                  <th>Resolution State</th>
                              </tr>
                          </thead>
                   
                          <tfoot>
                              <tr>
                                  <th>Subject</th>
                                  <th>Predicate</th>
                                  <th>Object</th>
                                  <th>State B1</th>
                                  <th>State B2</th>
                                  <th>Conflicting</th>
                                  <th>Resolution State</th>
                              </tr>
                          </tfoot>
                   
                          <tbody  >                   
                              <#list tableRowList as row>
                                <tr>
                                  <td>${row.subject}</td>
                                  <td>${row.predicate}</td>
                                  <td>${row.object}</td>
                                  <td>${row.stateA} (${row.revisionA!"  "})</td>
                                  <td>${row.stateB} (${row.revisionB!"  "})</td>
                                  <#if row.conflicting == "1">
                                    <td><a><img src="/static/images/Conflict.png"/></a></td>
                                  <#else>
                                    <td><a><img src="/static/images/Difference.png"/></a></td>
                                  </#if>
                                  <td><input type="checkbox" id ="opt" name="options" value=${row.tripleId}></td>
                                </tr>
                              </#list>                          
                          </tbody>
                        </table> -->
              </fieldset>
              </div>

             </div>

            </fieldset>
        </div>       
  </div>    
           
</body>
</html>       

  

