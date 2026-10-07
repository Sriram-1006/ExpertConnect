package ExpertConnect.service;

import ExpertConnect.dto.AuthRegisterRequest;
import ExpertConnect.dto.AuthResponse;
import ExpertConnect.dto.LoginRequest;
import ExpertConnect.dto.RegisterRequest;
import ExpertConnect.dto.UserResponse;
import ExpertConnect.entity.User;
import ExpertConnect.exception.InvalidCredentialsException;
import ExpertConnect.repository.UserRepository;
import ExpertConnect.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserService userService,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponse register(AuthRegisterRequest request) {
        // Public registration always creates a USER; role is not taken from input.
        RegisterRequest base = new RegisterRequest(request.name(), request.email(), request.password(), null);
        return userService.register(base);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, "Bearer", user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
