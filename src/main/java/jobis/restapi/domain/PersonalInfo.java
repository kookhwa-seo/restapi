package jobis.restapi.domain;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "개인 정보 도메인 객체")
@Entity
public class PersonalInfo {
    @Id
    private String regNo;

    @NotEmpty
    private String name;
}
