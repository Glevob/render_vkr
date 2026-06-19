package ru.accouting.student.controller;

import org.springframework.data.domain.Page;
import ru.accouting.student.model.User;
import ru.accouting.student.model.UserAuthority;
import ru.accouting.student.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // Постраничный список пользователей
    @GetMapping
    public String listUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String authority,
                            Model model) {
        // 10 Пользователей на странице
        int pageSize = 10;
        Page<User> userPage = adminUserService.getAllUsers(page, pageSize, search, authority);

        model.addAttribute("usersPage", userPage);
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("selectedAuthority", authority);
        model.addAttribute("allAuthorities", EnumSet.allOf(UserAuthority.class));

        return "admin-users";
    }

    @GetMapping("/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = adminUserService.getUser(id);
        model.addAttribute("user", user);
        model.addAttribute("allAuthorities", EnumSet.allOf(UserAuthority.class));
        return "admin-user-edit";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String login,
                             @RequestParam(required = false) String password,
                             @RequestParam(name = "authority") String authorityRaw) {

        UserAuthority authority = UserAuthority.valueOf(authorityRaw);

        adminUserService.updateUser(id, login, password, authority);

        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allAuthorities", EnumSet.allOf(UserAuthority.class));
        return "admin-user-create";
    }

    @PostMapping("/new")
    public String createUser(@RequestParam String login,
                             @RequestParam(required = false) String password,
                             @RequestParam(name = "authority") String authorityRaw) {

        UserAuthority authority = UserAuthority.valueOf(authorityRaw);
        adminUserService.createUser(login, password, authority);

        return "redirect:/admin/users";
    }
}
