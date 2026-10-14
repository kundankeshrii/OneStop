package com.project5.OneStop.auth.service;
import com.project5.OneStop.auth.dto.AuthResponse;
import com.project5.OneStop.auth.dto.LoginRequest;
import com.project5.OneStop.auth.dto.SignupRequest;
import com.project5.OneStop.auth.entity.AuthProvider;
import com.project5.OneStop.auth.entity.User;
import com.project5.OneStop.auth.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.project5.OneStop.auth.security.JwtService jwtService;


    public User signup(SignupRequest request){

        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email is already registered");
        }
        User user=new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setProvider(AuthProvider.LOCAL);
        return userRepository.save(user);
    }

    public AuthResponse login(@Valid LoginRequest request) {

        User user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                new RuntimeException("Invalid email or password")
                );
        if(user.getPassword()==null || !passwordEncoder.matches(
                request.getPassword(), user.getPassword()
        )
        ){
            throw new RuntimeException("Invalid email or password");
        }
        String token=jwtService.generateToken(user);
        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
