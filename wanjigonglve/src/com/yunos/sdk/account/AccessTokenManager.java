package com.yunos.sdk.account;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Context;
import android.os.Bundle;

public class AccessTokenManager {
    private static final String ACCESS_TOKEN_FILE_NAME = "yunos_access_token";
    public static void saveAccessToken(Context context, AccessToken token) {
        if (token == null) {
            throw new IllegalArgumentException("token must not null.");
        }
        FileOutputStream output = null;
        ObjectOutputStream objOutput = null;
        try {
            output = context.openFileOutput(ACCESS_TOKEN_FILE_NAME,
                Context.MODE_PRIVATE);
            objOutput = new ObjectOutputStream(output);
            objOutput.writeObject(token);
            objOutput.flush();
        } catch(Exception e){
        } finally {
            if (output != null) {
                try {
                output.close();
                } catch (IOException e) {
                e.printStackTrace();
                }
            }
            if (objOutput != null) {
                try {
                    objOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static AccessToken readAccessToken(Context context) {
        FileInputStream in = null;
        ObjectInputStream objIn = null;
        AccessToken token = null;
        try {
            in = context.openFileInput(ACCESS_TOKEN_FILE_NAME);
            objIn = new ObjectInputStream(in);
            token = (AccessToken) objIn.readObject();
        } catch (Exception e) {
        } finally {
            if (objIn != null) {
                try {
                    objIn.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return token;
    }
    
    public static boolean removeAccessToken(Context context) {
        File token = new File(context.getFilesDir(), ACCESS_TOKEN_FILE_NAME);
        return token.delete();
    }
    
    public static AccessToken convertToAccessToken(Bundle values) {
        AccessToken token = new AccessToken();
        token.setAccessToken(values.getString("accessToken"));
        token.setUserInfo(values.getString("loginId"), 
            values.getString("kp"));
        return token;
    }
}