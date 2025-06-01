package br.ifsp.vvts.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173/")
@RestController
@RequestMapping(path = "/api/v1")
@AllArgsConstructor
@Tag(name = "Registration/Authentication API")
public class UserController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user.",
            description = "Returns the new user id."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201", description = "Successful operation.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterUserRequest.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication fails.",
                    content = @Content(schema = @Schema(hidden = true))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User email is already registered.",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest request) {
        final RegisterUserResponse response = authenticationService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Authenticates the user using email and password.",
            description = "Returns a JWT credential to be used in future requests."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200", description = "Successful operation.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication fails.",
                    content = @Content(schema = @Schema(hidden = true))
            )
    })
    @PostMapping("/authenticate")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        final AuthResponse response = authenticationService.authenticate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
