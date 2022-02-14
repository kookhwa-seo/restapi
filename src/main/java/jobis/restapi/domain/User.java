package jobis.restapi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    private String userId;

    private String name;

    private String password;

    private String regNo;

    public void setRegNo(String regNo) {
        this.regNo = regNo.replace("-", "");
    }
}
