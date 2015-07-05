<html class="no-js" lang= "en">
	<head>
	<title><h1>Report</h1></title> 
	<meta name="author" content="Markus Graube">
	<meta name="description" content="R43ples web application">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">	
	<link rel="icon" type="image/png" href="/static/images/r43ples-logo.v04.png">

	<!-- Foundation 5 core CSS -->
	<link href="/static/css/foundation.css" rel="stylesheet" />
	<link href="/static/css/normalize.css" rel="stylesheet" />
	<link rel="stylesheet" href="//cdn.jsdelivr.net/fontawesome/4.3.0/css/font-awesome.css">

	<!-- Custom styles for this template -->
	<link href="/static/css/r43ples.css" rel="stylesheet" /> 

	<!--Einbinden der externen Bibliotheken-->
    <script src="/static/js/jquery.js"></script>
    <script src="/static/js/foundation.js"></script>

	 <!--DataTable-->
    <link rel="stylesheet" href="https://cdn.datatables.net/1.10.6/css/jquery.dataTables.css">
    <link rel="stylesheet" href="https://cdn.datatables.net/plug-ins/1.10.6/integration/foundation/dataTables.foundation.css">
    <script src="https://cdn.datatables.net/1.10.6/js/jquery.dataTables.min.js"></script>
    <script src="https://cdn.datatables.net/plug-ins/1.10.6/integration/foundation/dataTables.foundation.js"></script>



    <script type="text/javascript">
	    $(document).ready(function () {
	        //DataTable
	        var table = $('#example').DataTable( {
	        "scrollY": "300px",
	        "paging": true
	         } );
	        var table = $('#example1').DataTable( {
	        "scrollY": "280.5px",
	        "paging": false
	        } );

	        $('a.toggle-vis').on( 'click', function (e) {
	        e.preventDefault();
	 
	        // Get the column API object
	        var column = table.column( $(this).attr('data-column') );
	 
	        // Toggle the visibility
	        column.visible( ! column.visible() );
	        } );

	        // Disable function
			jQuery.fn.extend({
			    disable: function(state) {
			        return this.each(function() {
			            this.disabled = state;
			        });
			    }
			});

	        var conflict = $('#haveConflict').text();
	        if(conflict == "1") {
	        	alert("Es gibt noch Konflikten , bitte zurueck zu MERGE Tabelle !" + conflict);
	        	$('#push').disable(true);
	        } else {
	        	alert("Alles ist in Ordnung" + conflict);
	        }



	    });
	</script>


	<style>
		.wsmall{
    	  -webkit-transform: scale(0.8,0.8); /* Safari and Chrome */
      	}

      	input[type="search"] {
      		height: 1.3125rem;
      	}

      	div.dataTables_info {
		    color: #999;
		    font-weight: normal;
		    display: none;
		}

		div.dataTables_paginate {
		    float: right;
		    margin: 0px;
		    display: none;
		}

	</style>	
	</head>
	<body style="background-color:WhiteSmoke;">

		<div class="container">
			<div class = "row">
				<div class = "small-12 columns">
					<div class="panel radius" style="background-color:white;">
	                  <fieldset>
	                    <legend><h4><strong>Merging Report</strong></h4></legend>
	                    <div class = "small-6 push-3 columns">
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
		                           	<textarea class="form-control" rows="6" id="message" name="message" placeholder=${commit.message} disabled></textarea>
		                           </div>
		                      </div>	
		                      </br>                      
	                    </div>
	                   
	                    <hr/>	                    
                        <table id="example" class="cell-border stripe" cellspacing="0" style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">
		                    <thead>
		                        <tr>
                                    <th style = "width:10%">Subject</th>
                                    <th style = "width:10%">Predicate</th>
                                    <th style = "width:10%">Object</th>
                                    <th style = "width:18%">State B1</th>
                                    <th style = "width:18%">State B2</th>
                                    <th style = "width:9%">Conflicting</th>
                                    <th style = "width:9%">Automatic Resolution State</th>
                                   	<th style = "width:8%">Resolution State</th>
                                    <th style = "width:8%">Approve</th>
                                </tr>
		                    </thead>
		        
		             
		                    <tbody>
		                    	<#list reportTableRowList as row>  
			                        <tr>
	                                    <th style = "width:10%">${row.subject} </th>
	                                    <th style = "width:10%">${row.predicate} </th>
	                                    <th style = "width:10%">${row.object} </th>
	                                    <th style = "width:18%">${row.stateA} (${row.revisionA!"  "})</th>
	                                    <th style = "width:18%">${row.stateB} (${row.revisionB!"  "})</th>

	                                    <#if row.conflicting == "1">
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Conflict.png"/></a></td>
                                      	<#else>
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Difference.png"/></a></td>
                                      	</#if>

                                      	<#if row.automaticResolutionState == "ADDED">
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Added.svg"/></a></td>
                                      	<#else>
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Deleted.svg"/></a></td>
                                      	</#if>

                                      	<#if row.resolutionState == "ADDED">
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Added.svg"/></a></td>
                                      	<#else>
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Deleted.svg"/></a></td>
                                      	</#if>

                                      	<#if row.approved == "RESOLVED">
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/Resolved.png"/></a></td>
                                      	<#else>
                                        <td style = "width:9%;text-align: center;"><a><img src="/static/images/NotApproved.svg"/></a></td>
                                      	</#if>

	                              	</tr>

	                            </#list>  	                        
		                    </tbody>
		                </table>

		                <hr/>
		                <div style = "display :none" id="haveConflict">${report}</div>
                      	<div class="small-3 push-1 columns">
                  			<!--check ob isRebase or three way merge-->
                      		<#if isRebase>
	                          <a href ="rebasePushProcessNew"><button id="push" type="button" class="button tiny expand default">Push</button></a>
	                        <#else>
	                          <a href ="pushProcessNew"><button id="push" type="button" class="button tiny expand default">Push</button></a>
	                        </#if>            	
                      	</div>
                      	<div class="small-3 pull-1 columns">
                        	<a href ="javascript:history.go(-1)"><button id="back" type="button" class="button tiny expand alert ">Back</button></a>
                      	</div>
	                    
	    
	                  </fieldset>
	                </div>
				</div>

			</div>
		</div>

	<!--	<h3>${report}!</h3>
		<a href ="pushProcessNew"><button>Push</button></a>
		<a href ="#"><button>Back</button></a> -->
	
	
	</body>
</html>


