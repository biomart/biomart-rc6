(function($) {
    $.widget('ui.datacontroller', {
        options: {
            // Larger buffer for IE (performance) 
            bufferSize: 20
        },
        _rendererOptions: [
            'grouped',
            'squish',
            'breakAt',
            'lineIndices',
            'heatColumn',
            'extraLabel',
            'xaxisLabel',
            'detailsUrl',
            'displayColumns',
            'fallbackColumn'
        ],
        _create: function() {
            var self = this,
                o  = self.options;

            self._total = 0;
            self._skip = 0;
            self._end = 0;
            self._limit = Infinity;
            self._onDone;
            //self._highlight;
            self._buffer = [];

            // Set options
            if (o.headers) self._headers = true;

            if (o.type) self._renderer = biomart.renderer.get(o.type);
            else self._renderer =  biomart.renderer.get('table');

            if (o.skip) self._skip = o.skip;
            if (o.limit) self._limit = o.limit + self._skip;

            // Set renderer options
            for (var i=0, curr; curr=this._rendererOptions[i]; i++) {
                if (typeof o[curr] != 'undefined') self._renderer.option(curr, o[curr]);
            }

            if (o.preprocess) self._preprocess = o.preprocess;

            self._end = self._skip;

            // prepare writee DOM Element
            self._writee = self._renderer.getElement().addClass('writee');
            self.element
                .addClass(o.type)
                .append(self._writee);
            self._writee.data('results_cache', []);
        },

        _headers: false,
        _writeln: function(s) {
            var self = this;
            if (self._headers) {
                // headers
                if (s) {
                    if (typeof s == 'string') s = s.split('\t');
                    self._writee.data('headers_cache', s);
                    self._renderer.printHeader(s, self._writee);
                }
                self._headers = false;
            } else {
                // normal data
                var self = this;
                if (biomart.errorRegex.test(s)) return;
                if (s) {
                    if (typeof s == 'string') s = s.split('\t');
                    self._writee.data('results_cache').push(s);
                    self._total++;

                    if (self._skip <= self._total && self._end < self._limit) {
                        self._buffer.push(s);
                        self._end++;
                    }
                    if (self._total%self.options.bufferSize == 0) {
                        self._renderer.parse(self._buffer, self._writee);
                        self._buffer = [];
                    }
                }
            }
        },

        error: function(reason) {
            this._hasError = true;
            this._renderer.error.apply(this._renderer, [this._writee, reason]);
        },

        write: function(s) {
            if (s) {
                var lines = s;
                if (!$.isArray(lines)) {
                    lines = lines.split('\n');
                }
                for (var i=0, line; line=lines[i]; i++)  {
                    if (this._preprocess) {
                        line = this._preprocess(line);
                    }
                    if (line) this._writeln.apply(this, [line]);
                }
            }
        },

        write_lines: function(lines) {
            for (var i=0, line; line=lines[i]; i++)  {
                if (this._preprocess)
                    line = this._preprocess(line);

                if (line) this._writeln.apply(this, [line]);
            }
        },

        writee: function() {
              return this._writee;
        },

        paginate: function(skip, limit) {
            var self = this,
                cache = self._writee.data('results_cache'),
                rows = [];

            self._end = skip + limit - 1;

            self._writee.find('tbody').empty();

            for (var i=skip, n=cache.length, curr; i<n; i++) {
                curr = cache[i];
                if (i > self._end) break;
                rows.push(curr);
            } 

            self._renderer.clear();
            self._renderer.parse(rows, self._writee);
            self._renderer.draw();

            return [self._total, skip+1, i];
        },

        // Only valid for renderers with 'setHighlightColumn' defined
        highlight: function(i) {
            if (this._renderer.setHighlightColumn) this._renderer.setHighlightColumn(i);
        },

        done: function() {
            var self = this;
            self._renderer.parse(self._buffer, self._writee);
            self._renderer.draw(self._writee, self._total);
            self._buffer = [];
            if (self.options.done) self.options.done.apply(self, [self._total, self._skip+1, self._end]); // 1-based indexing
        },

        destroy: function() {
            $.Widget.prototype.destroy.apply(this, arguments);
            this._renderer.destroy();
        }
    });

    $.widget('ui.datasource', {
        options: {
            type: 'ajax',
            method: 'POST',
            namespace: null
        },
        _create: function() {
            var self = this,
                o = self.options,
                SUCCESS = 'datasource.success',
                ERROR  = 'datasource.error',
                COMPLETE = 'datasource.complete';

            self._signal = o.namespace ? 'datasource.' + o.namespace : 'datasource';

            if (!self._assert(o)) return;
        },

        success: function(s) {
            this.element.trigger(this._signal + '.success', [s]);
        },

        error: function(reason) {
            this.element.trigger(this._signal + '.error', [reason]);
        },

        /*
         * This is always called AFTER success and error
         */
        complete: function() {
            this.element.trigger(this._signal + '.complete');
        },

        exec: function() {
            this._handlers[this.options.type].apply(this);
        },

        xhr_abort: function() {
            try {
                if (this._xhr && this._xhr.abort) this._xhr.abort();
                else return false;
            } catch (e) {
                // There is a bug in jQuery 1.4.2 where abort is overloaded, but IE6/7 doesn't allow overloading of that function
                // Has been fixed in jQUery 1.4.4 so we should upgrade to that or 1.5
            }
            return true;
        },

        _handlers: {
            ajax: function() {
                var self = this,
                    options = self.options,
                    ajaxOptions = {
                        timeout: options.timeout || 30000,
                        url: options.url,
                        data: options.data,
                        type: options.method,
                        complete: function() {
                            self.complete();
                        },
                        success: function(data) {
                            self.success(data);
                        },
                        error: function(xhr, reason, data) {
                            self.error.apply(self, [xhr, reason, data]);
                        }
                    };
                if (options.dataFilter) {
                    ajaxOptions.dataFilter = options.dataFilter;
                }
                self._xhr = $.ajax(ajaxOptions);
            },
            streaming: function() {
                var self = this,
                    options = self.options,
                    url = options.url,
                    data;

                self._iframe = options.iframe || $(['<iframe class="streaming" name="', biomart.uuid(), '"/>'].join('')).appendTo(document.body);

                // Allow iframe access to this object for streaming
                self._uuid = self._iframe.attr('name');
                biomart.datasource.streamers[self._uuid] = self;

                data = $.extend({stream: true, iframe: true, uuid: self._uuid, scope: 'biomart.datasource'}, options.data);

                if (options.method == 'POST') {
                    self._form = $(['<form class="streaming" method="POST" action="', url, '" target="', self._uuid, '"/>'].join('')).appendTo(document.body);
                    for (var k in data) {
                        $(['<input type="hidden" name="', k, '" />'].join('')).val(data[k]).appendTo(self._form);
                    }
                    self._form.submit();
                } else {
                    url = [url, '?', $.param(data)].join('');
                    if ($.browser.msie) {
                        self._iframe.contents()[0].location.replace(url);
                    } else {
                        self._iframe[0].contentWindow.location.replace(url);
                    }
                }
            },

            local: function() {
                this.success(this.options.data);
                this.complete();
            }
        },

        // Verify options are correct
        _assert: function(o) {
            if (!(o.type in this._handlers)) {
                alert('(DataSource) Type is invalid: ' + o.type);
                return false;
            }
            return true;
        },

        uuid: function() { return this._uuid },

        destroy: function() {
            $.Widget.prototype.destroy.apply(this, arguments);
            this.xhr_abort();
            if (this._iframe) {
                this._iframe.stopIframeLoading();
                if (!this.options.iframe) {
                    this._iframe.remove();
                }
                this._form.remove();
            }
            this.element.unbind('datasource');
        }
    });

    $.namespace('biomart.datasource', function(exports) {
        /*
         * Streaming functions for iframes to call to parent
         */
        exports.streamers = {};
        exports.write = function(uuid, s) {
            exports.streamers[uuid].success(s);
        };
        exports.done = function(uuid) {
            exports.streamers[uuid].complete();
        }
    });

})(jQuery);

