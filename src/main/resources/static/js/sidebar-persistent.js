/**
 * sidebar-persistent.js (Loaded in <head> with defer)
 */
document.addEventListener("DOMContentLoaded", function () {
    const sidebarWrapper = document.getElementById("sidebarWrapper");
    const toggleButton = document.querySelector('[data-bs-target="#sidebarWrapper"]');

    if (!sidebarWrapper) return;

    // 1. Check saved state on page/tab load
    const sidebarState = localStorage.getItem("sidebarStatus");

    if (sidebarState === "collapsed") {
        // Remove Bootstrap's show class so it stays hidden on load
        sidebarWrapper.classList.remove("show");

        // Correct the aria attribute on the button if it exists on the page
        if (toggleButton) {
            toggleButton.setAttribute("aria-expanded", "false");
            toggleButton.classList.add("collapsed");
        }
    } else {
        // Ensure it has the show class if explicitly set to expanded or empty
        sidebarWrapper.classList.add("show");
    }

    // 2. Listen to Bootstrap's collapse events to track user toggle behavior
    // 'shown.bs.collapse' fires when the sidebar finishes opening
    sidebarWrapper.addEventListener("shown.bs.collapse", function () {
        localStorage.setItem("sidebarStatus", "expanded");
    });

    // 'hidden.bs.collapse' fires when the sidebar finishes closing
    sidebarWrapper.addEventListener("hidden.bs.collapse", function () {
        localStorage.setItem("sidebarStatus", "collapsed");
    });
});