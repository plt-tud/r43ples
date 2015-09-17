<#include "superHeader.ftl">

<script src="//cdn.jsdelivr.net/jquery/2.1.3/jquery.min.js"></script>
<script type="text/javascript">

    $(document).ready(function(){
      $("#btn-add").click(function(){
      $("#tb").append("<tr><th><input type=\"text\" placeholder=\"User\"></th><th><input type=\"text\"   placeholder=\"User\"></th><th><button type=\"button\" class=\"button tiny radius default\" onclick=\"$(this).parent().parent().remove()\">delete</button></th></tr>");
      });  
      //$(".panel-collapse").first().addClass("out");
      // $("#btn-delete").click(function(){$(this).parent().parent().remove()});

    }); 
</script>

<br/>  	    
<div class="container-fluid">
			<div class="row">
				<div class="columns small-8 small-push-2" style="height:616px">        
                 <div class="panel radius" style="background-color:white;">
                  <fieldset>
                    <legend><h4><strong>Properties configuration</strong></h4></legend>
                      <form role="form">
                          <fieldset style="padding-top:16px;margin-top:16px">
                             <legend><em><strong>Initial Configuration</strong></em></legend>

                              <div class="row collapse prefix-radius" >
                                <div class="small-3 columns" >
                                  <span class="prefix"><strong>R43ples revision graph</strong></span>
                                </div>
                                <div class="small-9 columns" >
                                  <select class="radius">
                                     <option value="0" readonly>http://eatld.et.tu-dresden.de/r43ples-revisions</option>
                                  </select>
                                </div>
                              </div>
                              </br>
                              <div class="row collapse prefix-radius" >
                                <div class="small-3 columns" >
                                  <span class="prefix"><strong>R43ples SDD graph</strong></span>
                                </div>
                                <div class="small-9 columns" >
                                  <select class="radius">
                                     <option value="0" readonly>http://eatld.et.tu-dresden.de/r43ples-sdd</option>
                                  </select>
                                </div>
                              </div>
                            
                          </fieldset> 
                          </br>
                          <fieldset style="margin-bottom:16px">
                            <legend><em><strong>Prefix Configuration</strong></em></legend>
                                  <table style="width:86% align:center; margin-top:16px;margin-bottom:16px">
                                    <thead>
                                      <tr>
                                        <th style="width:20%" >prefix</th>
                                        <th style="width:70%" >mapping</th>
                                        <th style="width:30%">delete</th>
                                      </tr>
                                    </thead>
                                    <tbody id="tb">
                                      <tr id ="tr1">
                                        <th><input type="text" placeholder="User"></th>
                                        <th><input type="text" placeholder="User"></th>
                                        <th><button type="button" class="button default radius tiny" onclick="$(this).parent().parent().remove()">delete</button></th>
                                      </tr>
                                    </tbody>
                                  </table>
                                  <div class = "row">
                                    <div class="small-10 small-push-1 columns">
                                      <button type="button" class="button tiny expand default" id="btn-add">Add</button>
                                    </div>
                                  </div>
                                    
                          </fieldset>

                          <div class="row" >
                        	  <div class="small-5 columns small-push-1">
                            	<button type="submit" class="button tiny expand default">Submit</button>
                          	</div>
                          	<div class="small-5 columns small-pull-1">
                            	<button type="button" class="button tiny expand alert">Cancel</button>
                          	</div>
                          </div>
                      
                      </form>
                    </fieldset>
                  </div>                         
            </div>
		 </div>
  </div>

<#include "superFooter.ftl">
