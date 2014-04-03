package com.yunos.sdk.account;

public class AuthError {
    private int mErrorCode;
    private String mErrorDescription;
    public int getError() {
        return mErrorCode;
    }
    public void setError(int error) {
        this.mErrorCode = error;
    }
    public String getErrorDescription() {
        return mErrorDescription;
    }
    public void setErrorDescription(String error_description) {
        this.mErrorDescription = error_description;
    }
    @Override
    public String toString() {
        return new StringBuilder().append("error:" + mErrorCode)
            .append("; msg:" + mErrorDescription)
            .toString();
    }
}