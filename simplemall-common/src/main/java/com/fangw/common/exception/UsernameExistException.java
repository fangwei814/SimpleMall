package com.fangw.common.exception;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-06-27 16:04
 **/
public class UsernameExistException extends RuntimeException {

    public UsernameExistException() {
        super("存在相同的用户名");
    }
}
