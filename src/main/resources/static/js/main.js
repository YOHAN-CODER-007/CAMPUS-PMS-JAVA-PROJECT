/**
 * Campus Placement Management System - Main Client-Side JavaScript
 * Handles Theme Toggling (Dark/Light), Sidebar Interactions, and UI Enhancements.
 */

(function () {
    'use strict';

    // 1. Dark Mode Initialization & Toggle
    const currentTheme = localStorage.getItem('pms-theme') ||
        (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');

    document.documentElement.setAttribute('data-bs-theme', currentTheme);

    window.addEventListener('DOMContentLoaded', () => {
        updateThemeToggleIcons(currentTheme);

        const themeToggleButtons = document.querySelectorAll('.theme-toggle-btn');
        themeToggleButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const activeTheme = document.documentElement.getAttribute('data-bs-theme');
                const newTheme = activeTheme === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-bs-theme', newTheme);
                localStorage.setItem('pms-theme', newTheme);
                updateThemeToggleIcons(newTheme);
            });
        });

        // 2. Mobile Sidebar Toggle
        const sidebarToggle = document.getElementById('sidebarToggle');
        const sidebar = document.querySelector('.app-sidebar');
        const backdrop = document.querySelector('.sidebar-backdrop');

        if (sidebarToggle && sidebar) {
            sidebarToggle.addEventListener('click', () => {
                sidebar.classList.toggle('show');
                if (backdrop) backdrop.classList.toggle('show');
            });
        }

        if (backdrop) {
            backdrop.addEventListener('click', () => {
                if (sidebar) sidebar.classList.remove('show');
                backdrop.classList.remove('show');
            });
        }

        // 3. Auto dismiss flash alerts after 6 seconds
        const autoDismissAlerts = document.querySelectorAll('.alert-auto-dismiss');
        autoDismissAlerts.forEach(alert => {
            setTimeout(() => {
                const bsAlert = new bootstrap.Alert(alert);
                bsAlert.close();
            }, 6000);
        });
    });

    function updateThemeToggleIcons(theme) {
        const icons = document.querySelectorAll('.theme-toggle-icon');
        icons.forEach(icon => {
            if (theme === 'dark') {
                icon.className = 'bi bi-sun-fill theme-toggle-icon text-warning';
            } else {
                icon.className = 'bi bi-moon-stars-fill theme-toggle-icon text-secondary';
            }
        });
    }
})();
