package jobis.restapi.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class Login {
    @NotEmpty
    private String userId;

    @NotEmpty
    private String password;
}
