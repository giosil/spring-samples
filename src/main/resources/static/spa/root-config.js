System.register(["single-spa"], function(_export, _context) {
  var singleSpa;
  return {
    setters: [
      function(module) {
        singleSpa = module;
      }
    ],
    execute: function() {
      console.log('[@dew/root-config] execute...');
      
      let apps = ['app-conf','app-docs','app-info','app-logs'];
      
      for(let app of apps) {
        singleSpa.registerApplication({
          name: '@dew/' + app,
          app: () => System.import('@dew/' + app),
          activeWhen: ['/' + app]
        });
      }
      
      singleSpa.start();
    }
  }
});