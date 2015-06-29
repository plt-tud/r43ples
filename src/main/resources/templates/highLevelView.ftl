	    <script type="text/javascript">
	    	// $(function(){
	    	// 	$("#highLevelTable").find("tr").click(function() {
	     //          if((this.style.background == "" || this.style.background =="white")) {
	     //              $(this).css('background', 'green');	              	  
	     //          }
	     //          else {
	     //              $(this).css('background', 'white');
	     //          }
	     //    });
	            
	    	// });

          $("#highLevelTable :button").click(function(){
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
              
              $.post("approveHighLevelProcess",
                  {
                    id: id,
                    isChecked: isChecked
                  }
              );

          });

          $("#allSelectHighLevel").click(function(){
            $("#highLevelTable input[name='options']").each(function(){
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
                $.post("approveHighLevelProcess",
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
              <div id="allSelectHighLevel" class="columns large-2 push-10"><button type="button" class="button tiny radius" onclick="'">Approve All</button></div>
              <hr style="margin:8px;"/>
              <div class = "parentTbl">
                <table  style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;">
                  <tr>
                    <td>
                      <div class = "childTbl">
                        <table  style="width:100%; table-layout:fixed; word-break: break-all; word-wrap: break-word;" class = "childTbl">
                          <tr>
                            <th style = "width:18%">Subject</th>
                            <th style = "width:18%">Predicate</th>
                            <th style = "width:18%">Object</th>
                            <th style = "width:18%">Object will be renamed to</th>
                            <th style = "width:18%">Rename yes or no</th>
                            <th style = "width:10%">Approve</th>
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
                              <td style = "width:18%">${row.subject} </td>
                              <td style = "width:18%">${row.predicate}</td>
                              <td style = "width:18%">${row.objectAlt}</td>
                              <td style = "width:18%">${row.objectNew}</td>

                              <#if row.isRenaming == "yes">
                                <td style = "width:18% ;text-align: center;"><input type="checkbox" id ="opt" name="options" checked value=${row.tripleId}>
                                </td>
                              <#else>
                                 <td style = "width:18% ;text-align: center;"><input type="checkbox" id ="opt" name="options" value=${row.tripleId}>
                                </td>
                              </#if>

                              <#if row.isResolved == "yes">
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
     	  </fieldset>