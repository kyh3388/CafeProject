package org.cloud.board;

import java.time.LocalDateTime;

import org.cloud.user.UserDto;

import lombok.Data;

@Data
public class BoardDto {
	
	private Long boardNumber;
    private Integer boardCategory;
    private String boardTitle;
    private String boardWrite;
    private UserDto user;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
