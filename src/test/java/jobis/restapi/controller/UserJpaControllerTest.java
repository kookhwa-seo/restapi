package jobis.restapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jobis.restapi.domain.Login;
import jobis.restapi.domain.PersonalInfo;
import jobis.restapi.domain.User;
import jobis.restapi.jpa.repository.UserRepository;
import jobis.restapi.util.CryptoUtil;
import jobis.restapi.util.Sha512Cipher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserJpaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Environment environment;

    public String token;
    public MockHttpSession session;

    String userId = "test2";
    String password = Sha512Cipher.encrypt("2222");

    @Before
    public void setup() throws Exception {
        User user = new User();
        user.setUserId(userId);
        user.setPassword(password);
        user.setRegNo(CryptoUtil.encrypt("9211081582816"));
        user.setName("김둘리");
        userRepository.save(user);

        session = new MockHttpSession();
        session.setAttribute("loginId", userId);

        token = CryptoUtil.createJWT(userId, password);
    }

    @After
    public void clean(){
        session.clearAttributes();
    }

    @Test
    public void signup_sucess() throws Exception {
        User user = new User();
        user.setUserId("test1");
        user.setPassword("2222");
        user.setRegNo("860824-1655068");
        user.setName("홍길동");

        String object = objectMapper.writeValueAsString(user);
        ResultActions actions = mockMvc.perform(post("/szs/signup")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void login_success() throws Exception {
        Login login = new Login();
        login.setUserId("test2");
        login.setPassword("2222");

        String object = objectMapper.writeValueAsString(login);
        ResultActions actions = mockMvc.perform(post("/szs/login")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void signup_fail_exist_user() throws Exception {
        User user = new User();
        user.setUserId("test2");
        user.setPassword("2222");
        user.setRegNo("921108-1582816");
        user.setName("김둘리");

        String object = objectMapper.writeValueAsString(user);
        ResultActions actions = mockMvc.perform(post("/szs/signup")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void signup_fail_null() throws Exception {
        User user = new User();
        user.setUserId("test2");
        user.setPassword("2222");
        user.setRegNo("921108-1582816");

        String object = objectMapper.writeValueAsString(user);
        ResultActions actions = mockMvc.perform(post("/szs/signup")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void signup_fail_regNo_invalid() throws Exception {
        User user = new User();
        user.setUserId("test2");
        user.setPassword("2222");
        user.setRegNo("921108-9999999");
        user.setName("김둘리");

        String object = objectMapper.writeValueAsString(user);
        ResultActions actions = mockMvc.perform(post("/szs/signup")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void signup_fail_name_invalid() throws Exception {
        User user = new User();
        user.setUserId("test2");
        user.setPassword("2222");
        user.setRegNo("921108-1582816");
        user.setName("김퐁키");

        String object = objectMapper.writeValueAsString(user);
        ResultActions actions = mockMvc.perform(post("/szs/signup")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    public void login_fail_not_exist_user() throws Exception {
        Login login = new Login();
        login.setUserId("9999");
        login.setPassword("2222");

        String object = objectMapper.writeValueAsString(login);
        ResultActions actions = mockMvc.perform(post("/szs/login")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void login_fail_incorrect_password() throws Exception {
        Login login = new Login();
        login.setUserId("test2");
        login.setPassword("8888");

        String object = objectMapper.writeValueAsString(login);
        ResultActions actions = mockMvc.perform(post("/szs/login")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isNotFound());
    }


    @Test
    public void my_info_success() throws Exception {
        ResultActions actions = mockMvc.perform(get("/szs/me")
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void my_info_token_null() throws Exception {
        ResultActions actions = mockMvc.perform(get("/szs/me")
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void my_info_token_invalid() throws Exception {
        ResultActions actions = mockMvc.perform(get("/szs/me")
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", "aaaa")
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void my_info_token_expired() throws Exception {
        CryptoUtil.jwtValidate = 3;
        String expiredToken = CryptoUtil.createJWT(userId, password);

        ResultActions actions = mockMvc.perform(get("/szs/me")
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", expiredToken)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isUnauthorized());

        String validate = environment.getProperty("jwt.validate");
        CryptoUtil.jwtValidate = Long.valueOf(validate);
    }

    @Test
    public void my_info_method_not_allow() throws Exception {
        ResultActions actions = mockMvc.perform(post("/szs/me")
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isMethodNotAllowed());
    }


    @Test
    public void scrap_fail_invalid_user() throws Exception {
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setName("김둘리");
        personalInfo.setRegNo("921108-1582816");
        MockHttpSession tempSession = new MockHttpSession();
        tempSession.setAttribute("loginId", "1111");

        String object = objectMapper.writeValueAsString(personalInfo);
        ResultActions actions = mockMvc.perform(post("/szs/scrap")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .session(tempSession)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void scrap_fail_invalid_regNo() throws Exception {
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setName("김둘리");
        personalInfo.setRegNo("921108-9999999");

        String object = objectMapper.writeValueAsString(personalInfo);
        ResultActions actions = mockMvc.perform(post("/szs/scrap")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void scrap_success() throws Exception {
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setName("김둘리");
        personalInfo.setRegNo("921108-1582816");

        String object = objectMapper.writeValueAsString(personalInfo);

        ResultActions actions1 = mockMvc.perform(post("/szs/scrap")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        ResultActions actions2 = mockMvc.perform(post("/szs/scrap")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        ResultActions actions3 = mockMvc.perform(post("/szs/scrap")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions1.andDo(print())
                .andExpect(status().isOk());
        actions2.andDo(print())
                .andExpect(status().isOk());
        actions3.andDo(print())
                .andExpect(status().isOk());
    }
    
    @Test
    public void refund_success() throws Exception {
        PersonalInfo personalInfo = new PersonalInfo();
        personalInfo.setName("김둘리");
        personalInfo.setRegNo("921108-1582816");

        String object = objectMapper.writeValueAsString(personalInfo);
        ResultActions scrapActions = mockMvc.perform(post("/szs/scrap")
                .content(object)
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        scrapActions.andDo(print())
                .andExpect(status().isOk());

        ResultActions refundActions = mockMvc.perform(get("/szs/refund")
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        refundActions.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void refund_fail_no_scrap() throws Exception {
        ResultActions actions = mockMvc.perform(get("/szs/refund")
                .accept(MediaType.APPLICATION_JSON)
                .session(session)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON_UTF8));

        actions.andDo(print())
                .andExpect(status().isNotFound());
    }
}
