(function($) {
$.namespace('biomart.martview', function(self) {
    var $results = $('#biomart-results'),

        CONFIG = 'config',
        QUERY_RESULTS_OPTIONS = {
            header: true,
            showProgress: false,
            animationTime: 200,
            iframe: false,
            showLoading: true,
            footer: false
        },
        LIMIT = 200,
        DATASETS = 'datasets',
        ATTRIBUTES = 'attributes',

        _mart = {};

    self.init = function() {
        var params = biomart.url.simpleQueryParams(location.hash.substr(1)),
            xml,
            config,
            datasets,
            attributes,
            filters = {};

        config = params[CONFIG];
        datasets = params[DATASETS].split(',');
        attributes = list2hash(params[ATTRIBUTES].split(','));

        delete params[CONFIG];
        delete params[DATASETS];
        delete params[ATTRIBUTES];
            
        for (var p in params) {
            filters[p] = {
                name: p,
                value: params[p]
            };
        }

        _mart = {
            datasets: datasets,
            attributes: attributes,
            filters: filters
        };

        if (config) _mart.config = config;

        xml = biomart.query.compile('XML', _mart, 'TSVX', 100, true, 'webbrowser');

        $results.queryResults($.extend({
            queries: xml,
            paginateBy: 20,
            independentQuery: false,
            loading: $results.find('.loading'),
            display: 'html'
        }, QUERY_RESULTS_OPTIONS));
    };

    function list2hash(list) {
         var hash = {};
        for (var i=0, item; item=list[i]; i++) hash[item] = {name: item};
        return hash;
    }
});

$.subscribe('biomart.init', biomart.martview, 'init');
})(jQuery);

