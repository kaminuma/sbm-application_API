package importApp.importApp.Repository;

import importApp.importApp.Entity.UserEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUsernameAndPassword(String s, @Param("username") String username);
}
