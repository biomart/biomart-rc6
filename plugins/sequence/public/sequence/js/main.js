(function($) {
$.namespace('biomart.sequence', function(self) {
    var CLIENT = 'webbrowser',
        _urlHash = biomart.url.jsonify(location.href),
        _elements = {},
        _state = { 
            initialized: false
        },
        _delimiterRegex = /[,\s\n]/,

        BLOCK_OPTIONS = {
            message: '<span class="loading" style="margin: 3px auto"></span>',
            css: { padding: '5px', borderColor: '#2e6e9e' },
            overlayCSS:  { backgroundColor: '#fff' }
        },
        
        SEQUENCE_TYPES = [
            {name: 'transcript_exon_intron', displayName: 'Unspliced (Transcript)'},
            {name: 'gene_exon_intron', displayName: 'Unspliced (Gene)'},
            {name: 'transcript_flank', displayName: 'Flank (Transcript)'},
            {name: 'gene_flank', displayName: 'Flank (Gene)'},
            {name: 'coding_transcript_flank', displayName: 'Flank-coding region (Transcript)'},
            {name: '5utr', displayName: '5\' UTR'},
            {name: '3utr', displayName: '3\' UTR'},
            {name: 'gene_exon', displayName: 'Exon Sequences'},
            {name: 'cdna', displayName: 'cDNA Sequences'},
            {name: 'coding', displayName: 'Coding Sequences'},
            {name: 'peptide', displayName: 'Protein'}
        ];

    biomart.renderer.renderInvalid = true;

    self.init = function() {
        initElements();
        initEvents();
        initState();
        _elements.datasetSelect.trigger('change');
        $('#biomart-loading').fadeAndRemove();
    }

    function initElements() {
        _elements.martSelect = $('#mart-list').prettybox();
        _elements.datasetSelect = $('#dataset-list').prettybox();
        _elements.typeContainer = $('#type-list');
        _elements.typeImage = $('#type-image');
        _elements.flankContainer = $('#flank');
        _elements.upstreamFlank = $('#upstream');
        _elements.downstreamFlank = $('#downstream');
        _elements.filterContainer = $('#filter-list');
        _elements.attributeContainer = $('#attribute-list');
        _elements.filterTitle = _elements.filterContainer.siblings('h3').minimizer({state: 'hide'});
        _elements.attributeTitle = _elements.attributeContainer.siblings('h3').minimizer({state: 'hide'});
        _elements.submit = $('#biomart-submit');
        _elements.form = $('#biomart-form');
        _elements.iframe = $('#biomart-streaming');
        _elements.resultsWrapper = $('#biomart-results-wrapper').dialog({
            modal: true,
            resizable: false,
            draggable: false,
            autoOpen: false,
            width: 600,
            height: 500,
            buttons: {
                "Close": function() { $(this).dialog('close') },
                "Download": download
            },
            open: function() {
                _elements.iframe.siblings('.loading').show();
                _elements.form.submit();
            },
            close: function() {
                _elements.iframe.stopIframeLoading();
            }
        });
    }

    function initEvents() {
        _elements.martSelect.bind('change.sequence', martChangedHandler);
        _elements.datasetSelect.bind('change.sequence', datasetChangedHandler);

        _elements.typeContainer
            .delegate('.type-container', 'change.sequence', typeChangedHandler);

        _elements.attributeContainer
            .delegate('.attribute-container', 'addattribute', function() {
                var item = $(this).addClass('active').data('item');
                _state.attributes[item.name] = item;
            })
            .delegate('.attribute-container', 'removeattribute', function() {
                var item = $(this).removeClass('active').data('item');
                delete _state.attributes[item.name];
            })

        _elements.filterContainer
            .delegate('.filter-container', 'addfilter', function(ev, item, value) {
                var $this = $(this),
                    value = biomart.validator.filter($this);
                if (value && (!$.isArray(value) || value[0])) {
                    $this.addClass('active').data('value', value);
                } else {
                    $this.removeClass('active').data('value', null);
                }
                item.value = value;
                _state.filters[item.name] = item;
            })
            .delegate('.filter-container', 'removefilter', function(ev, item) {
                biomart.clearFilter($(this).removeClass('active').data('value', null));
                item.value = null;
                delete _state.filters[item.name];
            });

        _elements.submit
            .delegate('button', 'click', showResults);

        _elements.iframe.bind('load', function() {
            _elements.iframe.siblings('.loading').hide();
        });
    }

    function initState() {
        for (var i=0, mart; mart=window.MARTS[i]; i++) {
            if (mart.name == window.INITIAL_MART_NAME) {
                _state.mart = mart;
            }
            // store mart object
            _elements.martSelect.children('[data-name=' + mart.name + ']').data('item', mart);
        }

        for (var i=0, ds; ds=window.DATASETS[i]; i++) {
            // store dataset object
            _elements.datasetSelect.children('[data-name=' + ds.name + ']').data('item', ds);
        }

        for (var i=0, type; type=SEQUENCE_TYPES[i]; i++) {
            var input = $([
                '<div class="type-container">',
                    '<input type="radio" name="type" id="', type.name, '" value="', type.name, '"',
                        i===0 ? ' checked' : '', '/>',
                    '<label for="', type.name, '">', type.displayName, '</label>',
                '</div>'
            ].join(''))
                .data('item', type)
                .appendTo(_elements.typeContainer);

            if (i===0) {
                _state.type = type;
                input.trigger('change');
            }

        }

        _state.filters = {};
        _state.attributes = {};
    }
        
    function martsLoaded(marts) {
    }

    function datasetsLoaded(datasets) {
    }

    function filterContainerLoaded(root) {
        clearContainer(_elements.filterContainer);
        for (var i=0, container; container=root.containers[i]; i++) {
            biomart.renderer.container({
                headerTagName: 'h4',
                headerClassName: 'container-name',
                item: container,
                mode: biomart.renderer.FILTERS,
                appendTo: _elements.filterContainer
            });
        }
    }

    function attributeContainerLoaded(root) {
        clearContainer(_elements.attributeContainer);
        for (var i=0, container; container=root.containers[i]; i++) {
            biomart.renderer.container({
                headerTagName: 'h4',
                headerClassName: 'container-name',
                item: container,
                mode: biomart.renderer.ATTRIBUTES,
                selectedAttributes: getDefaultAttributes(),
                onAttributeSelect: function(attribute) {
                    _state.attributes[attribute.name] = attribute;
                },
                appendTo: _elements.attributeContainer
            });
        }
    }

    function martChangedHandler() {
        var option = $(this.options[this.selectedIndex]),
            mart = option.data('item')

        // Clear out current dropdown
        _elements.datasetSelect.prettybox('destroy').empty();

        // retrieve new datasets
        biomart.resource.load('datasets', function(datasets) {
            for (var i=0, ds; ds=datasets[i]; i++) {
                var element = $([
                            '<option value="', ds.name, '">', ds.displayName, '</option>'
                        ].join(''))
                    .data('item', ds)
                    .appendTo(_elements.datasetSelect);
            }

            _elements.datasetSelect
                .prettybox()
                .trigger('change');
        }, { mart: mart.name });
    }

    function datasetChangedHandler() {
        var option = $(this.options[this.selectedIndex]),
            ds = option.data('item');

        _state.dataset = ds;

        _elements.filterContainer.block(BLOCK_OPTIONS);
        _elements.attributeContainer.block(BLOCK_OPTIONS);

        biomart.resource.load('containers', filterContainerLoaded, {
            datasets: ds.name,
            config: _state.mart.name,
            withattributes: false
        });

        biomart.resource.load('containers', attributeContainerLoaded, {
            datasets: ds.name,
            config: _state.mart.name,
            withfilters: false
        });
    }

    function typeChangedHandler() {
        var type = $(this).data('item');
        _state.type = type;

        var arr = getDefaultAttributes(),
            checkboxes = _elements.attributeContainer.find('input[type=checkbox]');

        checkboxes.each(function() {
            var that = $(this).closest('.attribute-container'),
                item = that.data('item');
            if ($.inArray(item.name, arr) != -1) {
                this.checked = true;
                that.trigger('addattribute');
            } else if (item.name == 'ensembl_exon_id') {
                this.checked = false;
                that.trigger('removeattribute');
            }
        });

        _elements.attributeTitle.animate({
            color: '#fff'
        }, {
            duration: 200,
            complete: function() {
                _elements.attributeTitle.animate({
                    color: '#897048'
                }, 200);
            }
        });

        _elements.typeImage.html([
            '<img alt="" src="images/', type.name, '.png"/>'
        ].join(''));
    }


    function clearContainer(element) {
        element
            .unblock()
            .html('');
    }

    function showResults() {
        var queryMart = {
                config: _state.mart.config,
                datasets: _state.dataset.name,
                params: [
                    { name: 'type', value: _state.type.name }
                ],
                filters: _state.filters,
                attributes: _state.attributes
            },
            xml,
            upstream = _elements.upstreamFlank.val(),
            downstream = _elements.downstreamFlank.val();

        if (upstream) {
            queryMart.params.push({ name: 'upstreamFlank', value: upstream });
        } else if (downstream) {
            queryMart.params.push({ name: 'downstreamFlank', value: downstream });
        }

        xml = biomart.query.compileSingleMartXML(queryMart, 'sequence', 20, 0, CLIENT);
        _elements.form.children('input').eq(0).val(xml)

        _elements.resultsWrapper.dialog('open')
    }

    function download() {
        var queryMart = {
                config: _state.mart.config,
                datasets: _state.dataset.name,
                params: [
                    { name: 'type', value: _state.type.name }
                ],
                filters: _state.filters,
                attributes: _state.attributes
            },
            xml,
            download = $('#biomart-download'),
            form = $('#biomart-download-form'),
            upstream = _elements.upstreamFlank.val(),
            downstream = _elements.downstreamFlank.val();

        if (upstream) {
            queryMart.params.push({ name: 'upstreamFlank', value: upstream });
        } else if (downstream) {
            queryMart.params.push({ name: 'downstreamFlank', value: downstream });
        }

        xml = biomart.query.compileSingleMartXML(queryMart, 'sequence', -1, 0, CLIENT);

        form.children('input').eq(0).val(xml)
        form.submit();

        _elements.resultsWrapper.dialog('close')

    }

    function getDefaultAttributes() {
        var arr = [];
        arr.push('ensembl_gene_id');
        arr.push('ensembl_transcript_id');
        if (_state.type.name == 'gene_exon') {
            arr.push('ensembl_exon_id');
        }
        return arr;
    }
});

$.subscribe('biomart.init', biomart.sequence, 'init');

})(jQuery);
