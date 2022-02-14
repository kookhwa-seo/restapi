package jobis.restapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Scrap {
    @Id
    private String userId;

    private Integer totalPayment;

    private Integer totalUseAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "user_id")
    //사용자 정보까지 같이 조회가 필요한 경우  @JsonIgnore 주석처리해서 사용
    @JsonIgnore
    private User user;
}
