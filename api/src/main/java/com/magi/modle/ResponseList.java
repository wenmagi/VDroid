package com.magi.modle;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author zhangzhaowen @ Zhihu Inc.
 * @since 06-29-2017
 */

public class ResponseList<T extends Response> {

    @SerializedName("data")
    public List<T> data;

    @SerializedName("paging")
    @Expose
    public Paging paging;

}
