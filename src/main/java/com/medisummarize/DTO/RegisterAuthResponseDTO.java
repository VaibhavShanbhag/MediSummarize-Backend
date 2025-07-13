package com.medisummarize.DTO;

import com.medisummarize.enums.AuthStatus;
import com.medisummarize.model.User;

public record RegisterAuthResponseDTO(User user, AuthStatus authStatus) {
}
