package jobis.restapi.controller;

import com.google.gson.internal.LinkedTreeMap;
import io.swagger.annotations.ApiOperation;
import jobis.restapi.domain.Login;
import jobis.restapi.domain.PersonalInfo;
import jobis.restapi.domain.Scrap;
import jobis.restapi.domain.User;
import jobis.restapi.exception.PersonalInfoNotFoundException;
import jobis.restapi.exception.ScrapNotFoundException;
import jobis.restapi.exception.UserNotFoundException;
import jobis.restapi.jpa.repository.PersonalInfoRepository;
import jobis.restapi.jpa.repository.ScrapRepository;
import jobis.restapi.jpa.repository.UserRepository;
import jobis.restapi.util.ConvertFormat;
import jobis.restapi.util.CryptoUtil;
import jobis.restapi.util.JsonUtil;
import jobis.restapi.util.Sha512Cipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.AsyncRestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/szs")
public class UserJpaController {
    final private String scrapURL = "https://codetest.3o3.co.kr/scrap/";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalInfoRepository personalInfoRepository;

    @Autowired
    private ScrapRepository scrapRepository;

    @Autowired
    private Environment environment;

    @PostMapping("/signup")
    @ApiOperation(value = "회원가입", notes = "신규 사용자를 등록합니다.")
    public User createUser(@Valid @RequestBody User user) throws Exception {
        //개인정보 테이블에서 이름과 주민등록번호가 존재하는지 체크
        String encRegNo = CryptoUtil.encrypt(user.getRegNo());
        Optional<PersonalInfo> personalInfo = personalInfoRepository.findById(encRegNo);

        if (!personalInfo.isPresent() || (personalInfo.isPresent() && !user.getName().equals(personalInfo.get().getName()))){
            throw new PersonalInfoNotFoundException(String.format("regNo{%s} or name{%s} not exist", encRegNo, user.getName()));
        }

        //존재하는 아이디일 때
        if (userRepository.existsById(user.getUserId())){
            throw new UserNotFoundException(String.format("userId{%s} exist", user.getUserId()));
        }

        //민감정보 암호화
        user.setPassword(Sha512Cipher.encrypt(user.getPassword()));
        user.setRegNo(encRegNo);

        return userRepository.save(user);
    }

    @PostMapping("/login")
    @ApiOperation(value = "로그인", notes = "로그인 정보를 이용하여 JWT Token을 생성합니다.")
    public HashMap<String, String> createToken(HttpServletRequest request, @Valid @RequestBody Login login) throws Exception {
        HashMap<String, String> userMap = new HashMap<String, String>();

        String userId = login.getUserId();
        String password = login.getPassword();
        Optional<User> user = userRepository.findById(userId);

        //존재하는 userId인지 확인
        if (!user.isPresent()){
            throw new UserNotFoundException(String.format("userId{%s} is not exist", userId));
        }else{
            if(!user.get().getPassword().equals(Sha512Cipher.encrypt(password))){
                throw new UserNotFoundException(String.format("password{%s} is incorrect", password));
            }
        }

        String userJson = JsonUtil.toJson(userMap);
        request.getSession().setAttribute("loginId", userId);

        userMap.put("userId", userId);
        userMap.put("password", password);
        String token = CryptoUtil.createJWT(userId, userJson);
        HashMap<String, String> result = new HashMap<>();
        result.put("token", token);
        return result;
    }

    @GetMapping("/me")
    @ApiOperation(value = "내 정보 보기", notes = "token을 이용하여 자신의 회원가입 정보를 조회합니다.")
    public User retrieveUser(HttpServletRequest request) {
        String loginId = (String) request.getSession().getAttribute("loginId");
        Optional<User> user = userRepository.findById(loginId);

        if (!user.isPresent()) {
            throw new UserNotFoundException(String.format("userId{%s} not found", loginId));
        }
        return user.get();
    }

    @PostMapping("/scrap")
    @ApiOperation(value = "사용자 스크랩", notes = "제공된 스크랩 URL을 통해 환급액 계산에 필요한 사용자의 스크랩 정보를 저장합니다.")
    public Scrap saveScrap(HttpServletRequest request, @Valid @RequestBody PersonalInfo personalInfo) throws Exception {
        Scrap scrapEntity = new Scrap();

        String loginId = (String) request.getSession().getAttribute("loginId");
        Optional<User> user = userRepository.findById(loginId);

        if (!user.isPresent()) {
            throw new UserNotFoundException(String.format("userId{%s} not found", loginId));
        }

        scrapEntity.setUserId(user.get().getUserId());
        scrapEntity.setUser(user.get());

        String encRegNo = CryptoUtil.encrypt(personalInfo.getRegNo().replace("-", ""));

        if (user.get().getRegNo().equals(encRegNo)){
            AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
            asyncRestTemplate.setAsyncRequestFactory(getClientHttpRequestFactory());
            HttpEntity<PersonalInfo> httpRequest = new HttpEntity<>(personalInfo);
            //scrap URL 호출
            ListenableFuture<ResponseEntity<String>> entity = asyncRestTemplate.postForEntity(scrapURL, httpRequest, String.class);
            ResponseEntity<String> responseEntity = entity.get();

            entity.addCallback(new SuccessCallback<ResponseEntity<String>>() {
                @Override
                public void onSuccess(ResponseEntity<String> result) {
                    String scrapResult = responseEntity.getBody();

                    HashMap<String, Object> scrapMap = JsonUtil.fromJson(scrapResult, HashMap.class);
                    if (scrapMap.size() != 0){
                        LinkedTreeMap<String, Object> jsonList = (LinkedTreeMap<String, Object>) scrapMap.get("jsonList");

                        if (!CollectionUtils.isEmpty(jsonList) && jsonList.get("errMsg").equals("")){
                            for (Map.Entry<String, Object> entry : jsonList.entrySet()) {
                                if (entry.getKey().startsWith("scrap")){
                                    List<Map<String, String>> scrap = (List<Map<String, String>>) jsonList.get(entry.getKey());
                                    for(Map<String, String> temp : scrap){
                                        for (Map.Entry<String, String> tempEntry : temp.entrySet()) {
                                            if (tempEntry.getKey().equals("총지급액")){
                                                scrapEntity.setTotalPayment(Integer.valueOf(tempEntry.getValue()));
                                            }
                                            if (tempEntry.getKey().equals("총사용금액")){
                                                scrapEntity.setTotalUseAmount(Integer.valueOf(tempEntry.getValue()));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }, new FailureCallback() {
                @Override
                public void onFailure(Throwable ex) {
                    ex.printStackTrace();
                }
            });

            return scrapRepository.save(scrapEntity);
        }else{
            throw new UserNotFoundException(String.format("regNo not found"));
        }
    }

    @GetMapping("/refund")
    @ApiOperation(value = "환급액", notes = "사용자의 환급액 관련 정보를 조회합니다.")
    public HashMap<String, String> retrieveRefund(HttpServletRequest request) {
        HashMap<String, String> refundMap = new HashMap<>();;

        String loginId = (String) request.getSession().getAttribute("loginId");
        Optional<User> user = userRepository.findById(loginId);

        //스크랩정보 조회
        Optional<Scrap> scrap = scrapRepository.findById(loginId);

        if (!scrap.isPresent()){
            throw new ScrapNotFoundException(String.format("userId{%s}'s scrap data not found", loginId));
        }else{
            refundMap.put("이름", user.get().getName());

            //한도
            int limit = 0;
            int totalPayment = scrap.get().getTotalPayment();
            if (totalPayment <= 33000000){
                limit = 740000;
            } else if(totalPayment > 33000000 && totalPayment <= 70000000){
                limit = (int) (740000 - (totalPayment-33000000)*0.008);
                limit = limit<660000 ? 660000 : limit;
            } else if(totalPayment > 70000000){
                limit = (int) (660000 - (totalPayment-70000000)*0.5);
                limit = limit<500000 ? 500000 : limit;
            }
            refundMap.put("한도", ConvertFormat.convertMoney(limit));

            //공제액
            int deductible = 0;
            int totalUseAmount = scrap.get().getTotalUseAmount();
            if (totalUseAmount <= 1300000){
                deductible = (int) (totalUseAmount*0.55);
            } else if(totalUseAmount > 1300000){
                deductible = (int) (715000 + (totalUseAmount - 1300000)*0.3);
            }
            refundMap.put("공제액", ConvertFormat.convertMoney(deductible));

            //환급액
            int refund = Math.min(limit, deductible);
            refundMap.put("환급액", ConvertFormat.convertMoney(refund));
        }
        return refundMap;
    }

    private AsyncClientHttpRequestFactory getClientHttpRequestFactory() {
        String timout = environment.getProperty("config.read-timeout");
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setTaskExecutor(new SimpleAsyncTaskExecutor());
        requestFactory.setReadTimeout(Integer.parseInt(timout));
        return requestFactory;
    }
}
