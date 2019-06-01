package spring.boot.apachecamel.repository;

import org.springframework.data.repository.CrudRepository;
import spring.boot.apachecamel.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
}
