package com.library.controller;

import com.library.model.Book;
import com.library.repository.*;
import com.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Everything under /admin/** is restricted to ROLE_ADMIN in SecurityConfig. */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController extends BaseController {

    private final BookRepository books;
    private final LoanRepository loans;
    private final UserRepository users;
    private final ContactMessageRepository messages;
    private final LibraryService service;

    // ---------- Books: list / add / edit / delete ----------
    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", books.findAllByOrderByTitleAsc());
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        Book book = new Book();
        book.setTotalQuantity(1);
        model.addAttribute("book", book);
        return "admin/book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBook(@PathVariable Long id, Model model) {
        model.addAttribute("book", books.findById(id).orElseThrow());
        return "admin/book-form";
    }

    @PostMapping("/books")
    public String saveBook(@RequestParam(required = false) Long id,
                           @RequestParam String title,
                           @RequestParam String author,
                           @RequestParam String isbn,
                           @RequestParam String category,
                           @RequestParam int quantity,
                           RedirectAttributes ra) {
        String formUrl = (id == null) ? "/admin/books/new" : "/admin/books/" + id + "/edit";
        return run(() -> {
            service.saveBook(id, title, author, isbn, category, quantity);
            return id == null ? "Book added." : "Book updated.";
        }, ra, "/admin/books", formUrl);
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes ra) {
        return run(() -> {
            service.deleteBook(id);
            return "Book deleted.";
        }, ra, "/admin/books");
    }

    // ---------- Issued books ----------
    @GetMapping("/issued")
    public String issued(Model model) {
        model.addAttribute("loans", loans.findByReturnDateIsNullOrderByDueDateAsc());
        return "admin/issued";
    }

    // ---------- Members ----------
    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", users.findAllByOrderByCreatedAtDesc());
        return "admin/members";
    }

    @PostMapping("/members/{id}/toggle")
    public String toggleMember(@PathVariable Long id, RedirectAttributes ra) {
        return run(() -> service.toggleMember(id) ? "Member unblocked." : "Member blocked.",
                ra, "/admin/members");
    }

    // ---------- Fines ----------
    @GetMapping("/fines")
    public String fines(Model model) {
        model.addAttribute("loans", loans.findByFineAmountGreaterThanOrderByReturnDateDesc(0));
        return "admin/fines";
    }

    @PostMapping("/fines/{id}/pay")
    public String payFine(@PathVariable Long id, RedirectAttributes ra) {
        return run(() -> {
            service.markFinePaid(id);
            return "Fine marked as paid.";
        }, ra, "/admin/fines");
    }

    // ---------- Contact messages ----------
    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("messages", messages.findAllByOrderByCreatedAtDesc());
        return "admin/messages";
    }
}
