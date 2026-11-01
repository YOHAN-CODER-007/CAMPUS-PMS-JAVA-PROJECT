package com.placement.controller;

import com.placement.config.CustomUserDetails;
import com.placement.model.Notification;
import com.placement.model.User;
import com.placement.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller handling user notifications view and mark-as-read actions.
 */
@Controller
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String viewNotifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        List<Notification> notifications = notificationService.getUserNotifications(user);

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotifications", 0); // As they are viewed on this page
        return "notifications";
    }

    @PostMapping("/notifications/mark-all-read")
    public String markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails, RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        notificationService.markAllAsRead(user);
        redirectAttributes.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
