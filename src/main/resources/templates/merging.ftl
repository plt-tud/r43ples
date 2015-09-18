<#include "superHeader.ftl">
<style>
  fieldset {
    border:solid 3px black;
  }

  .setBorder{
    border:solid 3px black;
  }
</style>

<script type="text/javascript">
      //local 
    //localStorage.lastname = document.getElementById("user").innerHTML;
    // var x = document.getElementById("user").innerHTML;
    // alert(x);
   // alert(localStorage.lastname);
   
   function setLocalClient(){
     localStorage.clientName = document.getElementById("user").value;
   } 

    
    //Ajax graph->branch
      function showBranchList(str){
        var xmlhttp;
        if (window.XMLHttpRequest){// code for IE7+, Firefox, Chrome, Opera, Safari
          xmlhttp=new XMLHttpRequest();
        }
        else{// code for IE6, IE5
          xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
        }
        xmlhttp.onreadystatechange=function(){
          if (xmlhttp.readyState==4 && xmlhttp.status==200){
            document.getElementById("Branch1").innerHTML=xmlhttp.responseText;
            document.getElementById("Branch2").innerHTML=xmlhttp.responseText;
          }
        }
        xmlhttp.open("GET","merging?getBranch=1&graph="+str,true);
        xmlhttp.send();
       }; 

        //js Stragegie->illustration with Graph
        
        function showIllustration(num){
          if(num == 0){
            document.getElementById("illustration").innerHTML="";
          }
          if(num == 1){
            document.getElementById("illustration").innerHTML="<img class=\"pull-left\" src=\"/static/images/fast-forward-strategie.png\" alt=\"fast-forward\" style=\"width:416px;height:466px;margin-left:6px\">";
          }
          if(num == 2){
            document.getElementById("illustration").innerHTML="<img class=\"pull-left\" src=\"/static/images/three-way-strategie.png\" alt=\"three-way\" style=\"width:416px;height:466px;margin-left:6px\">";
          }
          if(num == 3){ 
            document.getElementById("illustration").innerHTML="<img class=\"pull-left\" src=\"/static/images/rebase-strategie.png\" alt=\"rebase\"     style=\"width:416px;height:466px;margin-left:6px\">";
          }

          // by fast forward hide the common,auto fieldset and sdd selector
          if(num == 1){
            $('#methodField').hide();
            $('#sddField').hide();
          } else{

            $('#methodField').show();
            $('#sddField').show();
          }

        }; 

        // branch onchange check : ob mit fast forward erreichbar
        function checkFastForward (){
          var graph = $('#graph').val();
          var branch1 = $('#Branch1').val();
          var branch2 = $('#Branch2').val();


          $.get("fastForwardCheckProcess", 
            {
              graph : graph,
              branch1 : branch1,
              branch2 : branch2
            },
            function(data,stauts){
              // get data and check
              $('#strategie').val('0');
              if(data == "false"){
                $('#strategie').children('option').each(function(){
                  if($(this).val() === "1"){
                    $(this).attr('disabled', true);
                  }
                });
              }else{
                $('#strategie').children('option').each(function(){
                  if($(this).val() === "1"){
                    $(this).attr('disabled', false);
                  }
                });
              }

            });
        }



</script>

<div class="container">
			<div class="row" >
				<div class="small-7 columns">
                <div class="panel radius" style="background-color:white;">
                  <fieldset>
                    <legend><h4><strong>Start Merging</strong></h4></legend>
                    </br>
                    <form action="mergingProcess" method="post" role="form">
                      <fieldset id="methodField">
                        <legend><em>Fieldset</em></legend>
                        <div class="row">
                          <div class="small-6 columns">
                              <input type="radio" name="optradio" value="common" checked><label><strong>Common</strong></label>
                          </div>
                          <div class=" small-6 columns">
                              <input type="radio" name="optradio" value="auto"><label><strong>AUTO</strong></label>
                          </div>
                        </div>
                      </fieldset>
                      </br>
                      <div class="row collapse prefix-radius setBorder" >
                        <div class="small-2 columns" >
                          <span class="prefix"><strong>GRAPH:</strong></span>
                        </div>
                        <div class="small-10 columns" >
                          <select class="radius" id="graph" name="graph" onchange="showBranchList(this.value)">
                             <option value="no">None</option>
							               <#list graphList as graphName>							     
                             <option value="${graphName}">${graphName}</option>                        
                             </#list>
                          </select>
                        </div>
                      </div>
                      </br>
                      <div class="row collapse prefix-radius setBorder" >
                        <div class="small-2 columns" >
                          <span class="prefix"><strong>Strategy:</strong></span>
                        </div>
                        <div class="small-10 columns" >
                          <select class="radius" id="strategie" name="strategie" onchange="showIllustration(this.value)">
                             <option value="0"> &lt; Select Strategy &gt;</option>
                             <option value="2">Three-Way Merge Strategy</option>
                             <option value="3">Rebase Merge Strategy</option>
                             <option value="1">Fast-Forward Merge Strategy</option>
                          </select>
                        </div>
                      </div>
                      </br>
                      <div id = "sddField" class="row collapse prefix-radius setBorder" >
                          <div class="small-2 columns" >
                            <span class="prefix"><strong>SDD:</strong></span>
                          </div>
                          <div class="small-10 columns">          
                            <select class="radius" id="sdd" name="sdd">
                             <option value="http://eatld.et.tu-dresden.de/sdd#defaultSDD">sdd:defaultSDD</option>
                            </select>
                          </div>
                      </div>
                      </br>
                      <div class="row collapse prefix-radius ">
                        <div class="small-2 columns setBorder">
                          <span class="prefix"><strong>MERGE:</strong></span>
                        </div>
                        <div class="small-3 columns setBorder">
                          <select class="radius" id="Branch1" name="Branch1" onchange="checkFastForward()">    
                             <option value="no"></option>
                          </select>          
                        </div>
                        <div class="small-2 small-offset-2 columns setBorder">
                          <span class="prefix"><strong>INTO:</strong></span>
                        </div>
                        <div class="small-3 columns setBorder">
                          <select class="radius" id="Branch2" name="Branch2" onchange="checkFastForward()">
                             <option value="no"></option>
                          </select>          
                        </div>
                      </div>
                      </br>  
                      <div class="row collapse prefix-radius setBorder">
                          <div class="small-2 columns">
                            <span class="prefix"><strong>USER:</strong></span>
                          </div>
                          <div class="small-10 columns">
                            <input type="text" style="margin:0px" placeholder="User" id="user" name="user">
                          </div>
                       </div>    
                      </br>
                      <div class="row">
                           <label class="small-2 columns"><div align=center><strong>MESSAGE:</strong></div></label>
                           <div class="small-10 columns ">
                           <textarea class="form-control setBorder" rows="6" id="message" name="message"></textarea>
                           </div>
                      </div>
                      </br>
                      <div class="row">
                          <div class="small-6 columns">
                            <button id="sub" type="submit" class="button tiny expand default" onclick="setLocalClient()">Submit</button>
                          </div>
                          <div class="small-6 columns">
                            <button id="can" type="button" class="button tiny expand alert ">Cancel</button>
                          </div>
                      </div>
                    </form>
       
                  </fieldset>
                </div>
			 </div>

       <div class= "small-5 columns">
          <div class="panel radius" style="height:566px; width:486;margin-top:56px;background-color:white;">
          <fieldset style="background-color:white">
            <legend><h4><strong>Strategy Illustration</strong></h4></legend>
            <div class="panel-body" id="illustration" style="height:486px; width:406px ;">
            </div>
          </fieldset>
          </div>
       </div>
		</div>
   </div>
   
   
<#include "superFooter.ftl">
	
		
