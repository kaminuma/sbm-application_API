//package importApp.importApp.Controller;
//
//import importApp.importApp.Entity.UserEntity;
//import importApp.importApp.Security.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import javax.servlet.http.HttpSession;
//
//@Controller
//public class LoginContloller {
//
//    private final UserService userService;
//
//    @Autowired
//    public LoginContloller(UserService userService) {
//        this.userService = userService;
//    }
//
//    @GetMapping("/login")
//    public String login() {
//        return "login"; // ログインページのビュー名
//    }
//    @PostMapping("/login")
//    public String loginPost(
//            @RequestParam("username") String username,
//            @RequestParam("password") String password) {
//
//        boolean isValidUser = userService.isUserValid(username, password);
//
//            // ユーザー認証の処理を実行
//
//        if (isValidUser) {
//            // ログイン成功時の処理
//            return "redirect:/"; // ログイン成功後にリダイレクトするURL
//        } else {
//            // ログイン失敗時の処理
//            return "redirect:/login"; // ログイン失敗後にリダイレクトするURL
//        }
//    }
//}