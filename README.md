1. 요구사항 구현여부
   - 회원가입(/szs/signup) : 구현
   - 로그인(/szs/login) : 구현
   - 내 정보 보기(/szs/me) : 구현
   - 유저 스크랩(/szs/scrap) : 구현
   - 환급액 조회(/szs/refund) : 구현
   
2. 구현방법
   <공통>
   - Java, Spring Boot, JPA, H2, Gradle, git, intelliJ 사용하여 구현
   - 주민등록번호 암호화는 AES256으로 구현, 비밀번호 암호화는 SHA512로 구현
   - 비밀번호와 주민등록번호와 같은 민감정보는 DB에 암호화하여 저장
   - 회원가입 시 가입이 가능한 사용자를 검증하기 위해서 제공받은 주민등록번호와 사용자이름을 초기데이터로 저장함(주민등록번호는 '-'를 제거하고 암호화하여 저장)
   - userId와 password를 사용하여 jwt token을 생성, 생성된 토큰으로 API 접근을 제한하며 토큰의 유효기간은 7일로 설정
   - 회원가입과 로그인 API를 제외한 나머지 API를 호출할 때는 request header의 Authorization에 토큰값을 설정해서 호출할 수 있게 처리 
   - API 호출할 때마다 사용한 토큰이 유효한지 체크하여 유효하지 않을 때는 API를 호출할 수 없게 함 
   <회원가입>
   - userId 파라미터값이 이미 등록된 아이디가 아니고 name과 regNo가 가입 가능한 정보일 때만 회원가입되도록 처리
   - 회원가입 4가지 정보가 모드 필수입력되도록 validation 처리
   <로그인>
   - userId, password 파라미터값이 가입된 회원정보인지 체크해서 가입된 사용자의 정보를 입력했을 때만 token값을 생성
   - 토큰을 만들 때 사용한 userId를 session에 저장 
   <내 정보 보기>
   - session에 저장된 useId를 사용해서 해당 사용자의 회원가입 정보를 조회
   <유저 스크랩>
   - 스크랩 URL 호출 시 필요한 regNo와 name을 파라미터 필드로 지정
   - regNo 파라미터값이 회원 사용자의 주민등록번호와 일치할 때만 스크랩 URL을 호출 
   - 스크랩 URL을 호출 비동기식으로 처리
   - 스크랩 URL 서비스와의 접속실패가 발생하면 접속 재시도를 1초 간격으로 4회 시도하고 4번째 접속할 때도 실패하면 접속실패 message를 출력
   - 스크랩 URL 응답 최대 대기시간을 application.yml 파일에 설정하여 최대 대기시간을 유연하게 변경할 수 있게 처리함  
   - 스크랩 URL 응답결과에서 환급액 계산 시 필요한 총지급액과 총사용금액을 scrap 테이블에 저장 
   <환급액 조회>
   - 사용자의 스크랩 데이터가 존재할 때만 환급액 정보를 조회
   - 스크랩 정보를 가지고 한도, 공제액, 환급액을 계산
   
3. 검증결과
   - UserJpaControllerTest.java에 테스트 코드 작성
   - 유저 스크랩 API에서 스크랩 URL을 비동기로 호출하는 기능 테스트를 위해 .../test/http 폴더 하위에 테스트 실행파일을 추가하였고
     사용자의 스크랩 URL을 0.1초 간격으로 5번 요청했을 때 요청한 순서대로 응답결과가 오지 않고 응답순서가 빠른순서대로 API가 실행됨
   - 사용자의 스크랩 URL 접속 실패가 발생할 때 재시도하는 기능을 테스트 하기 위해
     https://codetest.3o3.co.kr/scrap/ 대신 로컬에 별도 서비스(http://localhost:8088/jpa/users)를 호출하도록 설정 > 
     로컬서비스를 다운 시킨 상태에서 사용자 스크랩 API를 호출하면 접속실패 메시지를 출력하고 접속 재시도 도중 로컬 서비스를 다시 기동시키면 사용자 스크랩 API가 정상동작함  
   
4. 주관식 문제
   1) 외부 의존성이 높은 서비스를 만들 때 고려해야 할 사항이 무엇인지 서술해 주세요.
   - 서술내용 : 외부 서비스와 연계하여 서비스를 개발할 경우 외부 서비스와의 접속이 끊겼을 때 어떤 처리를 할지, 서비스로부터 응답을 받는데 평균적으로 시간이 얼마나 걸리는지
   최대 응답 소요시간이 몇 초인지 등을 고려해야합니다. 접속 실패의 경우 접속 재시도 횟수를 설정하여 일시적인 접속실패라면 접속 재시도를 하여 에러를 발생하지 않게 할 수 있습니다.
   외부 서비스 응답시간이 오래 걸리는 경우는 최대 응답 소요시간을 파악하여 적절한 응답대기 시간을 설정합니다.
   왜냐하면 이런한 예외상황으로 인해서 개발한 서비스에서 에러가 발생하거나 트랜잭션 처리 시 문제가 생길 수 있기 때문입니다.
   그리고 해당 서비스의 요청이 빈번한지도 체크하여 외부서비스 요청을 동기식으로 처리할지 비동기식으로 처리할지도 고려해야 합니다.
   서비스 요청이 빈번함에도 불구하고 외부서비스를 동기식으로 처리한다면 응답속도가 너무 느려져서 사용자에게 불폄함을 초래할 수 있기 때문입니다.   
      
   2) 일정이 촉박한 프로젝트를 진행하게 되었습니다. 이 경우 본인의 평소 습관에 맞춰 개발을 진행할지, 회사의 코드 컨벤션에 맞춰 개발할지 선택해 주세요. 그리고 그 이유를 서
      술해 주세요.
   - 서술내용 : 평소 코딩습관으로 개발하면 속도는 빠르겠지만 기존 프로젝트 멤버들보다 시간을 더 할애해서라도 회사 코드 컨벤션에 맞춰 개발할 것입니다.  
     프로젝트는 저 혼자 진행하는 게 아니고 팀원들과 협업하여 개발을 진행하는 것이기 때문입니다. 또한 다른 팀원이 제가 맡은 개발을 같이하거나 제 코드가 팀원이 개발하는 코드의 관련 코드라서 
     제 코드를 볼 수도 있기 때문입니다. 
     만약 코드리뷰를 진행하게 됐을 때, 리뷰어가 코드컨벤션이 지켜지지 않은 코드를 보면 가독성이 좋지 않아서 리뷰하기 어려울 수도 있습니다. 
     이 외에도 프로젝트 전체 코드의 일관성을 지켜야 나중에 스스로 코드를 관리하기도 쉽고 다른 개발자가 제 코드를 수정할 일이 생길 때도 수정작업이 편리할 수 있습니다.

   3) 민감정보 암호화 알고리즘에는 어떤 것들이 있고, 그중 선택하신다면 어떤 것을 선택하실 건가요? 그 이유는 무엇인가요?
    - 서술내용 : 민간정보 암호화 알고리즘에는 AES, SEED, SHA 알고리즘 등이 있습니다. 
      그 중 주민등록 양방향 암호화를 할 때 선택할 알고리즘은 AES-256이고 비밀번호를 암호화할 때 사용할 알고리즘은 SHA-512입니다.
      먼저 AES-256를 선택한 이유는 SEED와 더불어서 개인정보 보호법에서 안전하다고 얘기하는 암호화 알고리즘이고 암호화 속도가 SEED보다 더 빠르기 때문입니다.
      파일 용량이 크면 클수록 그 속도는 SEED보다 훨씬 우월합니다. 또한 동일한 키 길이를 가지고 있을 때 안정성 기간도 더 깁니다. 
      키 길이가 192bit일 때 AES-256은 안정성 기간은 2030년 이후이지만 SEED는 128bit까지만 안정성 기간은 2030년 이후입니다.   
      SHA-512 알고리즘을 선택한 이유는 비밀번호 암호화를 할 때는 단방향 암호화를 해야 하고 개인정보 보호법에서 안전하다고 인정한 암호화 알고리즘이기 때문에 선택하였습니다.