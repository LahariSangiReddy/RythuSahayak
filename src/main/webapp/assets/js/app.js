(() => {
    "use strict";

    function todayAsInputValue() {
        const now = new Date();
        const localOffset = now.getTimezoneOffset() * 60000;
        return new Date(now.getTime() - localOffset).toISOString().slice(0, 10);
    }

    function initializeFooterYear() {
        const year = document.getElementById("current-year");
        if (year) year.textContent = String(new Date().getFullYear());
    }

    function initializePasswordToggles() {
        document.querySelectorAll("[data-password-toggle]").forEach((toggle) => {
            const input = document.getElementById(toggle.dataset.passwordToggle);
            if (!input) return;
            toggle.addEventListener("click", () => {
                const visible = input.type === "password";
                input.type = visible ? "text" : "password";
                toggle.setAttribute("aria-pressed", String(visible));
                toggle.setAttribute("aria-label", visible ? "Hide password" : "Show password");
                toggle.innerHTML = `<i class="bi bi-eye${visible ? "-slash" : ""}" aria-hidden="true"></i>`;
            });
        });
    }

    function initializeRegistrationValidation() {
        const form = document.getElementById("register-form");
        if (!form) return;
        const password = document.getElementById("password");
        const confirmation = document.getElementById("confirm-password");
        const validateConfirmation = () => {
            const matches = !confirmation.value || confirmation.value === password.value;
            confirmation.setCustomValidity(matches ? "" : "Passwords do not match.");
            confirmation.setAttribute("aria-invalid", String(!matches));
        };
        form.addEventListener("input", validateConfirmation);
        form.addEventListener("submit", validateConfirmation);
    }

    function initializeReportForm() {
        const form = document.getElementById("report-form");
        if (!form) return;

        const description = document.getElementById("description");
        const characterCount = document.getElementById("description-count");
        const dateInput = document.getElementById("observed-date");
        const submitButton = document.getElementById("report-submit");
        const label = submitButton.querySelector(".button-label");
        const loading = submitButton.querySelector(".button-loading");
        const resetButton = document.getElementById("report-reset");
        const fields = Array.from(form.querySelectorAll("input[required], select[required], textarea[required]"));
        dateInput.max = todayAsInputValue();

        const updateCounter = () => {
            characterCount.textContent = `${description.value.length} / 1000`;
        };
        const validateField = (field) => {
            if (field.id === "observed-date" && field.value && field.value > dateInput.max) {
                field.setCustomValidity("Choose a date that is not in the future.");
            } else {
                field.setCustomValidity("");
            }
            const isValid = field.checkValidity();
            field.classList.toggle("is-invalid", !isValid);
            field.setAttribute("aria-invalid", String(!isValid));
            const message = document.getElementById(`${field.id}-error`);
            if (message) message.classList.toggle("d-block", !isValid);
            return isValid;
        };

        form.addEventListener("input", (event) => {
            if (event.target === description) updateCounter();
            if (fields.includes(event.target)) validateField(event.target);
        });
        form.addEventListener("change", (event) => {
            if (fields.includes(event.target)) validateField(event.target);
        });
        form.addEventListener("submit", (event) => {
            const valid = fields.map(validateField).every(Boolean);
            form.classList.add("was-validated");
            if (!valid) {
                event.preventDefault();
                const firstInvalid = form.querySelector(".is-invalid");
                if (firstInvalid) firstInvalid.focus();
                return;
            }
            submitButton.disabled = true;
            label.classList.add("d-none");
            loading.classList.remove("d-none");
        });
        resetButton.addEventListener("click", (event) => {
            const hasEnteredData = Array.from(form.elements).some((element) => {
                if (element.type === "submit" || element.type === "reset") return false;
                if (element.type === "checkbox") return element.checked;
                return Boolean(element.value);
            });
            if (hasEnteredData && !window.confirm("Clear all entered details?")) event.preventDefault();
        });
        form.addEventListener("reset", () => {
            window.setTimeout(() => {
                form.classList.remove("was-validated");
                fields.forEach((field) => {
                    field.classList.remove("is-invalid");
                    field.removeAttribute("aria-invalid");
                    field.setCustomValidity("");
                    const message = document.getElementById(`${field.id}-error`);
                    if (message) message.classList.remove("d-block");
                });
                updateCounter();
            }, 0);
        });
        updateCounter();
    }

    function initializeTrackingForm() {
        const form = document.getElementById("tracking-form");
        if (!form) return;
        const input = document.getElementById("tracking-reference");
        const errorMessage = document.getElementById("reference-error");
        input.addEventListener("input", () => {
            input.classList.remove("is-invalid");
            input.removeAttribute("aria-invalid");
            errorMessage.classList.remove("d-block");
        });
        form.addEventListener("submit", (event) => {
            const valid = input.checkValidity();
            input.classList.toggle("is-invalid", !valid);
            input.setAttribute("aria-invalid", String(!valid));
            errorMessage.classList.toggle("d-block", !valid);
            if (!valid) {
                event.preventDefault();
                input.focus();
            }
        });
    }

    function filterAdvisories() {
        const searchInput = document.getElementById("advisory-search");
        const cropInput = document.getElementById("advisory-crop");
        const categoryInput = document.getElementById("advisory-category");
        const cards = Array.from(document.querySelectorAll(".advisory-card"));
        const emptyState = document.getElementById("advisory-empty");
        const countLabel = document.getElementById("advisory-count");
        if (!searchInput || !categoryInput || !emptyState || !countLabel) return 0;

        const query = searchInput.value.trim().toLocaleLowerCase();
        const crop = cropInput ? cropInput.value.toLocaleLowerCase() : "";
        const category = categoryInput.value.toLocaleLowerCase();
        let visibleCount = 0;
        cards.forEach((card) => {
            const matchesQuery = card.textContent.toLocaleLowerCase().includes(query)
                || card.dataset.crop.toLocaleLowerCase().includes(query);
            const matchesCrop = !crop || card.dataset.crop.toLocaleLowerCase() === crop;
            const matchesCategory = !category || card.dataset.category.toLocaleLowerCase() === category;
            const visible = matchesQuery && matchesCrop && matchesCategory;
            card.dataset.filterVisible = String(visible);
            if (visible) visibleCount += 1;
        });
        emptyState.hidden = visibleCount !== 0;
        countLabel.textContent = `Showing ${visibleCount} ${visibleCount === 1 ? "sample note" : "sample notes"}`;
        return visibleCount;
    }

    function initializeAdvisoryFallback() {
        if (!document.getElementById("advisory-grid") || window.jQuery) return;
        const grid = document.getElementById("advisory-grid");
        const update = () => {
            filterAdvisories();
            grid.querySelectorAll(".advisory-card").forEach((card) => {
                card.hidden = card.dataset.filterVisible !== "true";
            });
        };
        document.getElementById("advisory-search").addEventListener("input", update);
        const cropInput = document.getElementById("advisory-crop");
        if (cropInput) cropInput.addEventListener("change", update);
        document.getElementById("advisory-category").addEventListener("change", update);
    }

    function initializeFilterReset() {
        const resetButtons = document.querySelectorAll(".clear-advisory-filters");
        if (!resetButtons.length) return;
        resetButtons.forEach((resetButton) => resetButton.addEventListener("click", () => {
            document.getElementById("advisory-search").value = "";
            const cropInput = document.getElementById("advisory-crop");
            if (cropInput) cropInput.value = "";
            document.getElementById("advisory-category").value = "";
            const form = document.getElementById("advisory-filters");
            if (form) form.requestSubmit();
        }));
    }

    window.RythuSahayak = Object.freeze({ filterAdvisories });
    document.addEventListener("DOMContentLoaded", () => {
        initializeFooterYear();
        initializePasswordToggles();
        initializeRegistrationValidation();
        initializeReportForm();
        initializeTrackingForm();
        initializeAdvisoryFallback();
        initializeFilterReset();
    });
})();
