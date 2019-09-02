package com.michaelwang.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author jiuwang.wjw
 */
public class MainClient {

    private static final Logger logger = LoggerFactory.getLogger(MainClient.class);

    public static void main(String[] args) throws Exception {

        //引入远程服务
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("WmRpcDemo-client.xml");

        long count = 10L;

        final HeiheiService heiheiService = (HeiheiService) context.getBean("remoteHeiheiServiceTemp");

        //调用服务并打印结果
        for (int i = 0; i < count; i++) {
            try {
                String result = heiheiService.sayHeihei("wm, i=" + i);
                System.out.println(result);
            } catch (Exception e) {
                System.out.println("ex: " + e);
                logger.warn("--------", e);
            }
        }

        Thread.sleep(3000);

        //获取远程服务
        final HelloService helloService = (HelloService) context.getBean("remoteHelloService");

        //long count = 1000000000000000000L;

        //调用服务并打印结果
        for (int i = 0; i < count; i++) {
            try {
                String result = helloService.sayHelloAnother(" shuaige");
                System.out.println(result);
            } catch (Exception e) {
                System.out.println("--------ex: " + e);
                logger.warn("--------", e);
            }
        }

        //关闭jvm
        System.exit(0);
    }
}
