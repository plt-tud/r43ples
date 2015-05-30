	    <script type="text/javascript">
	    	$(function(){
	    		$("#individualTripleTable").find("tr").click(function() {
	              if((this.style.background == "" || this.style.background =="white")) {
	                  $(this).css('background', 'green');	              	  
	              }
	              else {
	                  $(this).css('background', 'white');
	              }
	            });
	            
	    	});
	    </script>
       
       
        <fieldset style="padding-bottom:0px;padding-top:0px;margin-bottom:6px">
            <legend><strong>Resolution</strong></legend>
              <div class="button-bar" >
                <ul class="button-group right">
                  <li><a href="#" class="tiny button radius" style="margin-bottom:0px;">Approve selected</a></li>
                  <li><a href="#" class="tiny button radius" style="margin-bottom:0px;">select all</a></li>
                </ul>
              </div>
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
                            <th style = "width:20%">State B1</th>
                            <th style = "width:20%">State B2</th>
                            <th style = "width:12%">Conflicting</th>
                            <th style = "width:12%">Resolution State</th>
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
 
                              <td style = "width:20%">${row.stateA} (${row.revisionA!"  "})</td>
                              <td style = "width:20%">${row.stateB} (${row.revisionB!"  "})</td>
                              <#if row.conflicting == "1">
                                <td style = "width:12%;text-align: center;"><a><img src="/static/images/Conflict.png"/></a></td>
                              <#elseif row.conflicting == "0">
                                <td style = "width:12%;text-align: center;"><a><img src="/static/images/Difference.png"/></a></td>
                              <#else>
                              	<td style = "width:12%;text-align: center;">${row.conflicting}</td>
                              </#if>

                              <#if row.resolutionState == "ADDED">
                              	<td style = "width:12% ;text-align: center;"><input type="checkbox" id ="opt" name="options" checked value=${row.tripleId}>
                              	</td>
                              <#elseif row.resolutionState == "DELETED">
                              	<td style = "width:12% ;text-align: center;"><input type="checkbox" id ="opt" name="options" value=${row.tripleId}>
                              	</td>
                              <#else>
                              	<td style = "width:12%;text-align: center;">${row.resolutionState}</td>
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