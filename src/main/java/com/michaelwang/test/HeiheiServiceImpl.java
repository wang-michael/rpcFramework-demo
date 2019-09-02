package com.michaelwang.test;

/**
 * @author jiuwang.wjw
 */
public class HeiheiServiceImpl implements HeiheiService {

    @Override
    public String sayHeihei(String somebody) {
        return "----------- hei " + somebody;
    }
}
