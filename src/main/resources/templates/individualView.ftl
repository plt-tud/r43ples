    <script type="text/javascript">
      var graphName = "${graphName}";
      var clientName = "${clientName}";

      var tripleTableColor = "YellowGreen";
      var individualTabelColor = "LemonChiffon"
    	$(function(){

    		$("#individualBody").find("tr").click(function() {
              
              if((this.style.background == "" || this.style.background =="white")) {
              	  $("#individualBody").find("tr").css('background', 'white');
                  $(this).css('background', individualTabelColor);
                  var individualA = $(this).find('td:first').text();
              	  var individualB = $(this).find('td:last').text();
				  
              	  alert(individualA + individualB);
              	  $.post("individualFilter",
                  {
                    individualA: individualA,
                    individualB: individualB,
                    graph: graphName,
                    client: clientName
                  },
                  function(data, status){
				  	        $("#individualFilter").empty();
                    $("#individualFilter").prepend(data);

                    $("#individualTripleTable :button").each(function (){
                       if($(this).text() == "Approved") {
                          $(this).parent().prev().children().prop({"disabled":true});
                          $(this).parent().parent().css('background',tripleTableColor);
                       }else{
                          $(this).parent().prev().children().prop({"disabled":false});
                          $(this).parent().parent().css('background','white');
                       }
                    });                    


                  }); 
              	  
              }
            });
            
    	});
    </script>

	<fieldset style="padding-bottom:0px;padding-top:0px;margin-bottom:6px">
		  <legend><strong>Resolution</strong></legend>
		  
		  <div class = "parentTbl">
		        <table  style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">
		          <tr>
		            <td>
		              <div class = "childTbl">
		                <table  style="width:100%; table-layout:fixed;word-break: break-all; word-wrap: break-word;" class = "childTbl">
		                  <tr>
		                    <th style = "width:50%">Individuals of A</th>
		                    <th style = "width:50%">Individuals of B</th>
		                  </tr>
		                </table>
		              </div>
		            </td>
		          </tr>
		          <tr>
		            <td>
		              <div id = "individualBody" class="scrollData childTbl" style = "height:166px;">
		                <table style="width:100%;table-layout:fixed;word-break: break-all; word-wrap: break-word;">
			              <#list individualTableList as row> 
		              	  <tr>
		              	  	  <td>${row.rowData[0]}</td>
		              	  	  <td>${row.rowData[1]}</td>
		              	  </tr>
		              	  </#list> 	                  
		                </table>
		              </div>
		            </td>
		          </tr>
		        </table>
		  </div>
		      
	</fieldset>

	<div id = "individualFilter">
	  <fieldset style="padding-bottom:0px;padding-top:0px;margin-bottom:6px">
            <legend><strong>Resolution</strong></legend>
			  <div id="allSelectIndividual" class="columns large-2 push-10"><button type="button" class="button tiny radius" >Approve All</button></div>
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
                      <div class="scrollDataIndividual childTbl" style = "height:166px;">
                        <table style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">						 				
                
                        </table>
                      </div>
                    </td>
                  </tr>
                </table>
              </div>
            
      </fieldset>
	
	
	
	
	</div>
