package com.intuit.be_a_friend.utils;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static List<String> allowedEndpoints = Arrays.asList("/api/v1/user/signin", "/api/v1/user/signup","swagger-ui.html","/v3/api-docs.*","/swagger-ui.html","/swagger-ui/.*");
}
