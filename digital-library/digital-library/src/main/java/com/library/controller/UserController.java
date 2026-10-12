package com.library.controller;

import com.library.model.Book;
import com.library.model.ContactMessage;
import com.library.model.Loan;
import com.library.model.User;
import com.library.repository.*;
import com.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final BookRepository books;
    private final LoanRepository loans;
    private final ReservationRepository reservations;
    private final UserRepository users;
    private final ContactMessageRepository messages;
    private final LibraryService service;

    private User current(Principal principal) {
        return users.findByEmail(principal.getName()).orElseThrow();
    }

    // ---------- Browse & search ----------
    @GetMapping("/books")
    public String catalogue(@RequestParam(required = false) String q,
                            @RequestParam(required = false) String category,
                            Model model) {
        List<Book> result = (q != null && !q.isBlank())
                ? books.search(q.trim())
                : books.findAllByOrderByTitleAsc();
        if (category != null && !category.isBlank()) {
            result = result.stream().filter(b -> b.getCategory().equalsIgnoreCase(category)).toList();
        }
        model.addAttribute("books", result);
        model.addAttribute("categories", books.findCategories());
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        return "books";
    }

    // ---------- Issue / reserve ----------
    @PostMapping("/books/{id}/issue")
    public String issue(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        return run(() -> {
            service.issueBook(current(principal).getId(), id);
            return "Book issued. Please return it within " + Loan.LOAN_DAYS + " days.";
        }, ra, "/books");
    }

    @PostMapping("/books/{id}/reserve")
    public String reserve(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        return run(() -> {
            service.reserveBook(current(principal).getId(), id);
            return "Book reserved. It will be held for you when a copy is returned.";
        }, ra, "/books");
    }

    // ---------- My books ----------
    @GetMapping("/my-loans")
    public String myLoans(Principal principal, Model model) {
        User user = current(principal);
        List<Loan> all = loans.findByUserIdOrderByIssueDateDescIdDesc(user.getId());
        model.addAttribute("active", all.stream().filter(l -> l.getReturnDate() == null).toList());
        model.addAttribute("history", all.stream().filter(l -> l.getReturnDate() != null).toList());
        model.addAttribute("reservations", reservations.findByUserIdOrderByReservedOnDesc(user.getId()));
        return "my-loans";
    }

    @PostMapping("/loans/{id}/return")
    public String returnBook(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        return run(() -> {
            Loan loan = service.returnBook(id, current(principal).getId());
            return loan.getFineAmount() > 0
                    ? "Book returned " + loan.getOverdueDays() + " day(s) late. Fine: \u20B9" + loan.getFineAmount()
                      + " - please pay it at the library desk."
                    : "Book returned. Thank you!";
        }, ra, "/my-loans");
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        return run(() -> {
            service.cancelReservation(id, current(principal).getId());
            return "Reservation cancelled.";
        }, ra, "/my-loans");
    }

    // ---------- Contact form ----------
    @GetMapping("/contact")
    public String contactForm() {
        return "contact";
    }

    @PostMapping("/contact")
    public String sendContact(@RequestParam String subject,
                              @RequestParam String message,
                              Principal principal,
                              RedirectAttributes ra) {
        if (subject.isBlank() || message.isBlank()) {
            ra.addFlashAttribute("error", "Please fill in both the subject and the message.");
            return "redirect:/contact";
        }
        ContactMessage m = new ContactMessage();
        m.setUser(current(principal));
        m.setSubject(subject.trim());
        m.setMessage(message.trim());
        messages.save(m);
        ra.addFlashAttribute("success", "Your message has been sent to the library admin.");
        return "redirect:/contact";
    }
}
