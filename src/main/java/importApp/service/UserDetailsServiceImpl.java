//package importApp.importApp.Service;
//
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    // ユーザーデータベースからユーザー情報を検索
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        // データベースからユーザーを取得するコードを記述
//
//        // 例: ダミーのユーザーを作成
//        UserDetails user = User.withUsername(username)
//                .password("{noop}password") // パスワード（ハッシュ化しない場合は{noop}を前置き）
//                .roles("USER")
//                .build();
//
//        return user;
//    }
//}