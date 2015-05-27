    <script type="text/javascript">
    	//change row color on click
    	$(function(){
    		$("#tripleTable,#individualBody").find("tr").click(function() {
              if(this.style.background == "" || this.style.background =="white") {
                  $(this).css('background', 'green');
              }
              else {
                  $(this).css('background', 'white');
              }
            });
    	});
    </script>
    
    //freemarker get Table
    <table style="width:100%; table-layout:fixed;word-break: break-all; word-wrap: break-word;">

      <#list tableRowList as row>
        <tr>
          <td style = "width:12%">${row.subject}</td>
          <td style = "width:12%">${row.predicate}</td>
          <td style = "width:12%">${row.object}</td>
          <td style = "width:20%">${row.stateA} (${row.revisionA!"  "})</td>
          <td style = "width:20%">${row.stateB} (${row.revisionB!"  "})</td>
          <#if row.conflicting == "1">
            <td style = "width:12%;text-align: center;"><a><img src="/static/images/Conflict.png"/></a></td>
          <#else>
            <td style = "width:12%;text-align: center;"><a><img src="/static/images/Difference.png"/></a></td>
          </#if>
          <td style = "width:12% ;text-align: center;"><input type="checkbox" id ="opt" name="options" value=${row.tripleId}></td>
        </tr>
      </#list>       

      
    </table>