/* Educational jQuery selectors and delegated events enhance the advisory filters. */
(function ($) {
    "use strict";

    if (!$ || !window.RythuSahayak) return;

    $(function () {
        const $grid = $("#advisory-grid");
        if (!$grid.length) return;

        const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        const updateCards = function () {
            window.RythuSahayak.filterAdvisories();
            $grid.find(".advisory-card").each(function () {
                const $card = $(this);
                const isVisible = $card.attr("data-filter-visible") === "true";
                $card.attr("aria-hidden", String(!isVisible));
                if (reduceMotion) {
                    $card.toggle(isVisible);
                } else if (isVisible) {
                    $card.stop(true, true).fadeIn(160);
                } else {
                    $card.stop(true, true).fadeOut(120);
                }
            });
        };

        // Delegated jQuery event handlers keep filtering focused on search and category controls.
        $(document).on("input.rythuAdvisory", "#advisory-search", updateCards);
        $(document).on("change.rythuAdvisory", "#advisory-crop", updateCards);
        $(document).on("change.rythuAdvisory", "#advisory-category", updateCards);
        updateCards();
    });
})(window.jQuery);
