<html class="no-js" lang="en">
<head>
<title>R43ples - Revision Management for the Semantic Web</title>
<meta name="author" content="Markus Graube">
<meta name="description" content="R43ples web application">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="icon" type="image/png" href="/static/images/r43ples-logo.v04.png"> 

<!-- Bootstrap core CSS -->
<!--
<link rel="stylesheet" href="//cdn.jsdelivr.net/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="//cdn.jsdelivr.net/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="//cdn.jsdelivr.net/fontawesome/4.3.0/css/font-awesome.css">
-->

<!-- Foundation 5 core CSS -->
<link href="/static/css/foundation.css" rel="stylesheet" />
<link href="/static/css/normalize.css" rel="stylesheet" />
<link rel="stylesheet" href="//cdn.jsdelivr.net/fontawesome/4.3.0/css/font-awesome.css">

<!-- Custom styles for this template -->
<link href="/static/css/r43ples.css" rel="stylesheet" />

<style>
.wsmall{
      -webkit-transform: scale(0.8,0.8); /* Safari and Chrome */
      }
</style>

</head>
<body style="background-color:WhiteSmoke;">

<!-- Navigation -->
  <div class="fixed">
    <nav class = "top-bar" data-topbar role = "navigation">
      <ul class = "title-area">
        <li class = "name">
          <h1><a href="."><img class="pull-left" src="/static/images/r43ples-r-logo.v04.png" alt="R43ples logo" style="height:42px;width:32px"><span>R43ples</span></a></h1>
        </li>
        <li class="toggle-topbar menu-icon"><a href="#"><span>Menu</span></a></li>
      </ul>
      <section class="top-bar-section">

          <!-- Right Nav Section -->
          <ul class="left">
            <#if endpoint_active??>
              <li class="active" ><a href="sparql"><i class="fa fa-edit"></i> Endpoint</a></li>
            <#else>
              <li ><a href="sparql"><i class="fa fa-edit"></i> Endpoint</a></li>
            </#if>

            <#if debug_active?? >
              <li class="active"><a href="debug"><i class="fa fa-stethoscope"></i> Debug</a></li>
            <#else>
              <li ><a href="debug"><i class="fa fa-stethoscope"></i> Debug</a></li>
            </#if>

            <#if merging_active??>
              <li class="active"><a href="merging"><i class="fa fa-magic"></i> Merging</a></li>
            <#else>
              <li ><a href="merging"><i class="fa fa-magic"></i> Merging</a></li>
            </#if>

            <#if mergingConfiguration_active??>
              <li class="active"><a href="mergingConfiguration"><i class="fa fa-cog fa-fw"></i> Configuration</a></li>
            <#else>
              <li ><a href="mergingConfiguration"><i class="fa fa-cog fa-fw"></i> Configuration</a></li>
            </#if>

            <li class="divider"></li>
          </ul>

          <!-- Left Nav Section -->
          <ul class="right">
            <li><div style="margin:6px; padding:3px;" ><em style="margin:16px; padding:8px; color:white;"> <#if version??>Version: ${version!"  "} 
              <#else>Git: ${git.commitIdAbbrev!" "} - ${git.branch!" "}</#if> </em></div></li>
            <li class="divider"></li>
            <li><a href="http://plt-tud.github.io/r43ples/"><i class="fa fa-globe"></i> Website</a></li>
            <li><a href="https://github.com/plt-tud/r43ples"><i class="fa fa-github"></i> GitHub</a></li>
          </ul>
        </section>
      </nav>

  </div>

    <div class="container wsmall">