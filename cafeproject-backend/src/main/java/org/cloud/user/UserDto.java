package org.cloud.user;

import lombok.Data;

@Data
public class UserDto {
	
	private String userId;
    private String userName;
    private String userNickname;
    private Integer userLevel;
    private byte[] userImage;
    private String userImageType;

}
