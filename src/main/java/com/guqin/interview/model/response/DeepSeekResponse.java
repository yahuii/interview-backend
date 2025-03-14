package com.guqin.interview.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DeepSeekResponse implements Serializable {

    private String id;

    private String model;

    private List<Choices> choices;

    @Data
    public static class Choices{
        private Integer index;

        private Delta delta;
    }

    @Data
    public static class Delta{
        private String content;
    }



    private static final long serialVersionUID = 1L;

}
