package com.example.Hospital.Service;

import com.example.Hospital.model.enums.PushResult;

public interface PushSender {


    PushResult send(String deviceToken, String title, String body);
}
