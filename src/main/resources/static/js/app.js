(function ($) {
    $(document).ready(function () {
        $('#tree').fbrowser({
            browseUrl: '/fs/browse',
            formatName: function(name) {
                return name.substring(name.lastIndexOf('/') + 1, name.length)
            }
        });
    });
})(jQuery);
