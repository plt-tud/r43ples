	    <script type="text/javascript">
	    	$(function(){
	    		$("#highLevelTable").find("tr").click(function() {
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
                            <th style = "width:20%">Subject</th>
                            <th style = "width:20%">Predicate</th>
                            <th style = "width:20%">Object</th>
                            <th style = "width:20%">Object will be renamed to</th>
                            <th style = "width:20%">Rename yes or no</th>
                          </tr>
                        </table>
                      </div>
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <div id = "highLevelTable" class="scrollData childTbl">
                        <table style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">

                          <#list highLevelRowList as row>
                            <tr>
                              <td style = "width:20%">${row.subject} </td>
                              <td style = "width:20%">${row.predicate}</td>
                              <td style = "width:20%">${row.objectAlt}</td>
                              <td style = "width:20%">${row.objectNew}</td>
                              <td style = "width:20% ;text-align: center;"><input type="checkbox" id ="opt" name="options" value=${row.tripleId}>
                              </td>
                            </tr>
                          </#list>   
                
                        </table>
                      </div>
                    </td>
                  </tr>
                </table>
              </div>                     
     	  </fieldset>