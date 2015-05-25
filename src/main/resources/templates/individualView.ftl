<fieldset style="padding-bottom:0px;padding-top:0px;margin-bottom:6px">
	  <legend><strong>Resolution</strong></legend>
	  <div class="row" style="margin:0px;padding:0px;">
        <table id="example1" class="stripe compact" cellspacing="0" width="100%">
              <thead>
                  <tr>
                      <th>Property</th>
                      <th>Select</th>
                  </tr>
              </thead>
       
              <tbody>
              	  <#list individualTableList as row> 
              	  <tr>
              	  	  <td>${row.rowData[0]}</td>
              	  	  <td>${row.rowData[1]}</td>
              	  </tr>
              	  </#list> 
              </tbody>

              <tfoot>
                  <tr>
                      <th>Property</th>
                      <th>Select</th>
                  </tr>
              </tfoot>
         </table>
      </div>     
	      
</fieldset>