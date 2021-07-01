package com.sunshinemine;

public interface HttpCallBack {
    void onPreExecute(String result,String caller);
    void onPostExecute(String result,String caller);
}
