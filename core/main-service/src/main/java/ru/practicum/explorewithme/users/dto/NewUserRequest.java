package ru.practicum.explorewithme.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {

    @NotBlank
    @Size(min = 2, max = 250)
    private String name;

    @NotBlank
    @Email
    @Size(min = 6, max = 254)
    private String email;
}