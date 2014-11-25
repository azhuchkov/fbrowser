(function ($) {
    $(document).ready(function () {
        $('#tree').fbrowser({
            /**
             * Override browsing path
             */
            browseUrl: '/fs/browse',

            /**
             * Define expandable types
             */
            containerTypes: ["DIRECTORY", "FILE_ZIP", "FILE_JAR", "FILE_RAR"],

            /**
             * Add new CSS class 'archive' for archive files
             */
            typeClassesExt: {
                "archive": ["FILE_ZIP", "FILE_JAR", "FILE_RAR"]
            }
        });
    });
})(jQuery);
