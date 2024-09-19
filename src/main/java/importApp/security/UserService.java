//package importApp.importApp.Security;
//
//import importApp.Entity.UserEntity;
//import importApp.Mapper.UserMapper;
//import importApp.Repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class UserService {
//        private final UserRepository userRepository;
//
//        @Autowired
//        public UserService(UserRepository userRepository) {
//            this.userRepository = userRepository;
//        }
//
//
//    public boolean isUserValid(String username, String password) {
//            UserEntity user = userRepository.findByUsernameAndPassword(username, password);
//            return user != null;
//        }
//}