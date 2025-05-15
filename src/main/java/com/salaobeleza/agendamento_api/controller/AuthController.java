package com.salaobeleza.agendamento_api.controller;


import com.salaobeleza.agendamento_api.dto.AuthRequest;
import com.salaobeleza.agendamento_api.dto.AuthResponse;
import com.salaobeleza.agendamento_api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        String token = authService.authenticate(authRequest.username(), authRequest.senha());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
