package jobis.restapi.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "사용자 정보 도메인 객체")
@Entity
public class User {
    @Id
    @NotEmpty
    private String userId;

    @Size(min=2, message = "name은 2글자 이상 입력해 주세요.")
    @ApiModelProperty(notes = "사용자 이름을 입력해 주세요.")
    @NotEmpty
    private String name;

    @ApiModelProperty(notes = "사용자의 패스워드를 입력해 주세요.")
    @NotEmpty
    private String password;

    @ApiModelProperty(notes = "사용자의 주민번호를 입력해 주세요.")
    @NotEmpty
    private String regNo;

    public void setRegNo(String regNo) {
        this.regNo = regNo.replace("-", "");
    }
}
