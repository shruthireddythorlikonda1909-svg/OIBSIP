package com.library.controller;

import com.library.model.User;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    /** After login everybody lands on "/", which sends them to the right home page. */
    @GetMapping("/")
    public String home(Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return admin ? "redirect:/admin/books" : "redirect:/books";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           RedirectAttributes ra) {
        String cleanEmail = email.trim().toLowerCase();

        if (name.isBlank() || !cleanEmail.contains("@") || password.length() < 6) {
            ra.addFlashAttribute("error", "Enter your name, a valid email and a password of at least 6 characters.");
            return "redirect:/register";
        }
        if (users.existsByEmail(cleanEmail)) {
            ra.addFlashAttribute("error", "An account with this email already exists.");
            return "redirect:/register";
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(cleanEmail);
        user.setPassword(encoder.encode(password));
        user.setRole(User.Role.USER);
        users.save(user);

        ra.addFlashAttribute("success", "Account created. Please log in.");
        return "redirect:/login";
    }
}
