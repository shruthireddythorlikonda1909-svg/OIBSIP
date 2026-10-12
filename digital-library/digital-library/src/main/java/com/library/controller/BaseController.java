package com.library.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Supplier;

/**
 * Small helper shared by controllers: runs an action, flashes a success message,
 * or flashes the error message if the service rejected the request.
 */
public abstract class BaseController {

    protected String run(Supplier<String> action, RedirectAttributes ra, String redirect) {
        return run(action, ra, redirect, redirect);
    }

    protected String run(Supplier<String> action, RedirectAttributes ra, String okRedirect, String errorRedirect) {
        try {
            ra.addFlashAttribute("success", action.get());
            return "redirect:" + okRedirect;
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:" + errorRedirect;
        }
    }
}
