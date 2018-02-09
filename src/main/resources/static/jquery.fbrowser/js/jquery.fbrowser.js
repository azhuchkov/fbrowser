/*
 * fBrowser jQuery plugin.
 */
(function ($, storage, window) {

    // Instance counter
    var instanceCount = 0;

    /* Plugin default settings */
    var defaults = {
        browseUrl: '/browse',
        pathParam: 'path',
        splitter: '/',

        containerTypes: ["DIRECTORY"],

        typeClasses: {
            "image": ["FILE_JPG", "FILE_TIFF", "FILE_BMP", "FILE_PNG", "FILE_ICO"],
            "directory": ["DIRECTORY"]
        },

        typeClassesExt: {},

        sort: function (file1, file2) {
            var isDirectory = function (file) {
                return file.type.toUpperCase() === "DIRECTORY";
            };

            if (isDirectory(file1))
                return isDirectory(file2) ? file1.name.localeCompare(file2.name) : -1;
            else
                return isDirectory(file2) ? 1 : file1.name.localeCompare(file2.name);
        },

        errorHandler: function (response) {
            if (response.status === 501)
                alert('Unsupported operation!');
            else
                alert('Failed to load data!');
        },

        formatName: function (name) {
            return name;
        }
    };

    /* Plugin methods */
    var methods = {
        init: function (options) {
            var settings = $.extend({}, defaults, options);

            settings.typeClassMap = {};

            $.each($.extend({}, settings.typeClasses, settings.typeClassesExt), function (cls, types) {
                $.each(types, function (i, type) {
                    settings.typeClassMap[type] = cls;
                });
            });

            return this.each(function () {
                var $this = $(this),
                    data = $this.data('fbrowser');

                if (data)
                    return false;

                var instanceId = window.location.href + 'fbrowser' + instanceCount++,
                    state = methods._restoreState(instanceId) || {selected: false, expanded: {}},
                    $root = $('<ul/>', {
                        class: 'root container'
                    });

                data = {
                    target: $this,
                    settings: settings,
                    instanceId: instanceId,
                    state: state
                };

                $this
                    .data('fbrowser', data)
                    .addClass('fbrowser')
                    .html($root);

                var $spinner = $('<div/>', {
                    class: 'spinner'
                }).appendTo($this);

                methods._load($root, settings.splitter, data).done(function (files) {
                    if (!files.length) $root.text("No files");

                    var expand = function ($container, prefix, expanded) {
                        $.each(expanded, function (name, children) {
                            var path = prefix + settings.splitter + name,
                                $expander = $('[data-path="' + path + '"]', $container);

                            if ($expander.length) {
                                methods
                                    ._toggle($expander, data)
                                    .done(function () {
                                        expand($expander.siblings('.container'), path, children);
                                    });
                            } else {
                                delete expanded[name];
                                methods._saveState(instanceId, state);
                            }
                        });
                    };

                    expand($root, "", state.expanded);
                }).always(function () {
                    $spinner.remove();
                });
            });
        },

        _saveState: function (instanceId, state) {
            storage.setItem(instanceId, JSON.stringify(state));
        },

        _restoreState: function (instanceId) {
            var data = storage.getItem(instanceId);
            return data !== undefined && data != null ? JSON.parse(data) : null;
        },

        _toggle: function ($expander, data) {
            var $children = $expander.siblings('.container'),
                path = $expander.data('path'),
                state = data.state,
                settings = data.settings,
                instanceId = data.instanceId;

            var addPathToState = function () {
                var subtree = state.expanded;

                $.each(path.split(settings.splitter).slice(1), function (i, name) {
                    if (subtree[name] === undefined)
                        subtree[name] = {};
                    subtree = subtree[name];
                });
            };

            var removePathFromState = function () {
                var subtree = state.expanded,
                    names = path.split(settings.splitter).slice(1);

                for (var i = 0; i < names.length - 1; i++) {
                    if (subtree[names[i]] === undefined)
                        return false;
                    subtree = subtree[names[i]];
                }

                delete subtree[names.pop()];
            };

            if ($children.length) {
                if ($expander.hasClass("expanded"))
                    $children.children("li").children(".expanded").trigger("click.fbrowser");

                $children.slideToggle();

                $expander.toggleClass("expanded");

                if ($expander.hasClass("expanded"))
                    addPathToState();
                else
                    removePathFromState();

                methods._saveState(instanceId, state);
            } else {
                var $container = $('<ul/>', {
                    css: {display: 'none'},
                    class: 'container'
                }).appendTo($expander.parent());

                var $spinner = $('<div/>', {
                    class: 'spinner'
                }).appendTo($expander.siblings('.item'));

                return methods
                    ._load($container, path, data)
                    .done(function () {
                        methods._toggle($expander, data);
                    })
                    .fail(function () {
                        $container.remove();
                    })
                    .always(function () {
                        $spinner.fadeOut(function () {
                            $spinner.remove();
                        });
                    });
            }
        },

        _select: function ($item, data) {
            $('.selected', data.target).removeClass('selected');
            $item.addClass('selected');

            data.state.selected = $item.attr('title');
            methods._saveState(data.instanceId, data.state);
        },

        _load: function (parent, path, data) {
            var settings = data.settings,
                state = data.state;

            return $.getJSON(settings.browseUrl, {
                path: path
            }).done(function (files) {
                $.each(files.sort(settings.sort), function (index, file) {
                    var $li = $('<li/>');

                    var filepath =
                        (path + settings.splitter + file.name).replace("//", "/");

                    if (settings.containerTypes.indexOf(file.type) > -1) {
                        $('<a/>', {
                            class: 'expander',
                            'data-path': filepath
                        }).on('click.fbrowser', function () {
                            methods._toggle($(this), data);
                        }).appendTo($li);
                    }

                    var classes =
                        'item ' + settings.typeClassMap[file.type] + (filepath === state.selected ? " selected" : "");

                    $('<a/>', {
                        text: settings.formatName(file.name),
                        title: filepath,
                        class: classes
                    }).bind('click.fbrowser', function () {
                        methods._select($(this), data);
                    }).appendTo($li);

                    parent.append($li);
                });
            }).fail(settings.errorHandler);
        },

        destroy: function () {
            return this.each(function () {
                $(this)
                    .unbind('.fbrowser')
                    .removeClass('fbrowser')
                    .removeData('fbrowser')
                    .empty();
            });
        }
    };

    /* Add plugin to jQuery namespace */
    $.fn.fbrowser = function (method) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist for jQuery.fbrowser');
        }
    }

})(jQuery, localStorage, window);