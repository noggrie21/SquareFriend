package com.ssafy.userservice.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.userservice.api.request.UserUpdateDto;
import com.ssafy.userservice.api.response.TokenResponseDto;
import com.ssafy.userservice.api.response.UserInfoByCodedEmailDto;
import com.ssafy.userservice.api.response.UserInfoByTokenDto;
import com.ssafy.userservice.api.response.UserInfoResponseDto;
import com.ssafy.userservice.common.exception.UserNotFoundException;
import com.ssafy.userservice.common.util.RedisService;
import com.ssafy.userservice.common.util.TokenProvider;
import com.ssafy.userservice.db.entity.User;
import com.ssafy.userservice.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    @Value("${api-server.url}")
    private String base_url;

    @Override
    public void updateUser(String accessToken, UserUpdateDto userUpdateDto) {
        String uid = tokenProvider.getUserId(accessToken);
        User user = userRepository.findById(Long.parseLong(uid)).orElseThrow(UserNotFoundException::new);

        user.updateUser(userUpdateDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoByCodedEmailDto getUserInfo(String codedEmail) throws IOException {
        User user = userRepository.findByCodedEmail(codedEmail).orElseThrow(UserNotFoundException::new);
        UserInfoByCodedEmailDto userInfoByCodedEmailDto = UserInfoByCodedEmailDto.builder().userId(user.getId())
                .omrList(getOMRList(user.getId())).build();
        return userInfoByCodedEmailDto;
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponseDto reIssue(String accessToken, String refreshToken) {
        tokenProvider.validateToken(refreshToken);
        String uid = tokenProvider.getUserId(refreshToken);
        String findRefreshToken = redisService.getValue(uid);

        if (findRefreshToken == null || !findRefreshToken.equals(refreshToken)) {
            throw new UserNotFoundException("refreshToken을 찾을 수 없습니다.");
        } else {
            return TokenResponseDto.builder()
                    .accessToken(tokenProvider.createAccessToken(uid))
                    .build();
        }
    }

    @Override
    public void logout(String accessToken) {
        String uid = tokenProvider.getUserId(accessToken);

        userRepository.findById(Long.parseLong(uid))
                .orElseThrow(() -> new UserNotFoundException("수험생 정보를 찾을 수 없습니다."));
        redisService.deleteValues(uid);
    }

    @Override
    public UserInfoByTokenDto getUserID(String accessToken) {
        String uid = tokenProvider.getUserId(accessToken);
        UserInfoByTokenDto userInfoResponseDto = UserInfoByTokenDto.builder()
                .id(Long.parseLong(uid))
                .build();

        return userInfoResponseDto;
    }

    @Override
    public UserInfoByTokenDto getUserId(String codedEmail)  {
        User user = userRepository.findByCodedEmail(codedEmail).orElseThrow(UserNotFoundException::new);
        UserInfoByTokenDto userInfoByCodedEmailDto=UserInfoByTokenDto.builder().id(user.getId()).build();
        return userInfoByCodedEmailDto;
    }

    public List<Integer> getOMRList(Long user_id) throws IOException {
        URL url=new URL(base_url+"/api/omr/list/"+user_id);
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type","apllication/json; utf-8");
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(br);
        StringTokenizer st= new StringTokenizer(String.valueOf((root.get("data").get("omridList"))),"[,]");
        List<Integer> omridlist=new LinkedList<Integer>();
        System.out.println((root.get("data").get("omridList")));
        System.out.println((root.get("data").findValuesAsText("omridList")));
        while(st.hasMoreTokens()){
            omridlist.add(Integer.parseInt(st.nextToken()));
        }

        return omridlist;
    }
}
