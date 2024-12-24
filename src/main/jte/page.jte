@import gg.jte.Content

@param Content menu
@param String sources
@param String version

<!doctype html>
<html lang="it">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <title>Spring-samples</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/main.css?${version}" rel="stylesheet">
    
    <script type="systemjs-importmap">
      {
        "imports": {
          "single-spa":       "https://cdn.jsdelivr.net/npm/single-spa@5.9.0/lib/system/single-spa.min.js",
          "@dew/root-config": "/spa/root-config.js",
          "@dew/app-conf":    "/spa/app-conf.js",
          "@dew/app-docs":    "/spa/app-docs.js",
          "@dew/app-info":    "/spa/app-info.js",
          "@dew/app-logs":    "/spa/app-logs.js"
        }
      }
    </script>
    <script src="https://cdn.jsdelivr.net/npm/systemjs@6.8.3/dist/system.min.js"></script>
  </head>
  <body>
    <header>
      <h1>Spring-samples</h1>
    </header>
    <hr>
    ${menu}
    <hr>
    <div class="container" id="single-spa-application">
      <div id="view-root"></div>
    </div>
    
    <div id="notification-center"></div>
    <div id="wux-waitpls" class="waitpls"></div>
    
    <script src="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/js/all.min.js"></script>
    
    <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.4/dist/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js"></script>
    
    <script src="js/custom.js?${version}"></script>
    <script src="js/portal-config.js?${version}"></script>
    
    <script src="app/wux${sources}.js?${version}"></script>
    <script src="app/app${sources}.js?${version}"></script>
    
    <script>
      System.import('@dew/root-config');
    </script>
  </body>
</html>