package importApp.importApp.model;
import lombok.Data;

import javax.persistence.*;
@Data
public class loginRequestModel {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String username;
        private String password;

        private boolean enabled;
}
