<fieldset style="padding-bottom:0px;padding-top:0px;margin-bottom:6px">
	  <legend><strong>Resolution</strong></legend>
	  
	  <div class = "parentTbl">
	        <table  style="width:100%; table-layout:fixed; ">
	          <tr>
	            <td>
	              <div class = "childTbl">
	                <table  style="width:100%; table-layout:fixed;" class = "childTbl">
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
	              <div class="scrollData childTbl" style = "height:166px;">
	                <table style="width:100%;table-layout:fixed;">
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