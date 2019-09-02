package com.michaelwang.test;

/**
 * @author jiuwang.wjw
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String somebody) {
        return "hello " + somebody + "!";
    }

    @Override
    public String sayHelloAnother(String somebody) {
        return "1111 " + somebody + "!";
    }

}
