package com.fangw.common.exception;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-06-27 16:04
 **/
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("存在相同的手机号");
    }
}
