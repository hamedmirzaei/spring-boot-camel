package spring.boot.apachecamel.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spring.boot.apachecamel.model.User;
import spring.boot.apachecamel.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<User>();
        userRepository.findAll().forEach(e -> users.add(e));
        return users;
    }


    public User getUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get();
        } else {
            return new User(0L, "Unknown", "Unknown", "Unknown");
        }
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

}
