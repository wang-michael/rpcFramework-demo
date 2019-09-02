package com.michaelwang.serialization.serializer.impl;

import com.michaelwang.serialization.serializer.ISerializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * @author jiuwang.wjw
 */
public class XmlSerializer implements ISerializer {

    private static final XStream xStream = new XStream(new DomDriver());


    @Override
    public <T> byte[] serialize(T obj) {
        return xStream.toXML(obj).getBytes();
    }


    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        String xml = new String(data);
        return (T) xStream.fromXML(xml);
    }


}
