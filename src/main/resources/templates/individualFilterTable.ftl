	  <script type="text/javascript">
	    	// $(function(){
	    	// 	$("#individualTripleTable").find("tr").click(function() {
	     //          if((this.style.background == "" || this.style.background =="white")) {
	     //              $(this).css('background', 'green');	              	  
	     //          }
	     //          else {
	     //              $(this).css('background', 'white');
	     //          }
	     //     });
	            
	    	// });
            //approve triples in server and update difference model
      $("#individualTripleTable :button").click(function(){
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

      $("#allSelectIndividual").click(function(){
        $("#individualTripleTable input[name='options']").each(function(){
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


	    </script>
       
       
        <fieldset style="padding-bottom:0px;padding-top:0px;margin-bottom:6px">
            <legend><strong>Resolution</strong></legend>
              <div id="allSelectIndividual" class="columns large-2 push-10"><button type="button" class="button tiny expand radius" >Approve All</button></div>
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
                      <div id = "individualTripleTable" class="scrollData childTbl">
                        <table style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">
						  
						  
                          <#list updatedTripleRowList as row>
                            <tr>
                            
                              <td style = "width:12%">${row.subject} </td>
                              <td style = "width:12%">${row.predicate}</td>
                              <td style = "width:12%">${row.object}</td>
 
                              <td style = "width:18%">${row.stateA} (${row.revisionA!"  "})</td>
                              <td style = "width:18%">${row.stateB} (${row.revisionB!"  "})</td>
                              <#if row.conflicting == "1">
                                <td style = "width:9%;text-align: center;"><a><img src="/static/images/Conflict.png"/></a></td>
                              <#elseif row.conflicting == "0">
                                <td style = "width:9%;text-align: center;"><a><img src="/static/images/Difference.png"/></a></td>
                              <#else>
                              	<td style = "width:9%;text-align: center;">${row.conflicting}</td>
                              </#if>

                              <#if row.resolutionState == "ADDED">
                              	<td style = "width:9% ;text-align: center;"><input type="checkbox" id ="opt" name="options" checked value=${row.tripleId}>
                              	</td>
                              <#elseif row.resolutionState == "DELETED">
                              	<td style = "width:9% ;text-align: center;"><input type="checkbox" id ="opt" name="options" value=${row.tripleId}>
                              	</td>
                              <#else>
                              	<td style = "width:9%;text-align: center;">${row.resolutionState}</td>
                              </#if>

                              <#if row.state == "RESOLVED">
                                <td><button type="button" class="button tiny expand radius" >Approved</button></td>
                              <#elseif row.state == "--">
                                <td><button type="button" class="button tiny expand radius" disabled>${row.state}</button></td>
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
            
      </fieldset>