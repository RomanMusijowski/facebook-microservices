package com.mic.auth.util;

import com.mic.auth.domain.User;
import com.mic.auth.payload.UserInfo;
import org.mapstruct.Mapper;

@Mapper
public interface UserInfoMapper {

    User userInfoToUser(UserInfo userInfo);

    UserInfo userToUserInfo(User user);
}
