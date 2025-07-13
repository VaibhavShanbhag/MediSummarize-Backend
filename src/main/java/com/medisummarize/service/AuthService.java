package com.medisummarize.service;

import com.medisummarize.enums.Role;
import com.medisummarize.model.User;
import com.medisummarize.repository.UserRepository;
import com.medisummarize.utils.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UserRepository userRepository;

    public String login (String email, String password){
        var authToken = new UsernamePasswordAuthenticationToken(email,password);
        var authenticate = authenticationManager.authenticate(authToken);
        if(authenticate == null)
            throw new RuntimeException("Bad Credentials");

        UserDetails userDetails = (UserDetails) authenticate.getPrincipal();

        // Fetch full User from DB to get role
        User user = userRepository.findByEmail(userDetails.getUsername());

        return JwtUtils.generateToken(user);
    }

    public User register(String name, String email, String password, String role) {
        if (userService.existsByEmail(email)) {
            throw new RuntimeException("User already exists with this email");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRole(Role.valueOf(role.toUpperCase()));

        return userService.createUser(user);
    }
}
