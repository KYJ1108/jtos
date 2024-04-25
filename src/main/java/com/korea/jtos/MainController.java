package com.korea.jtos;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {
    @GetMapping("/sbb")
    @ResponseBody
    public String index(){
        return "안녕하세요 sbb 에 오신 것을 환영합니다";
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/")
    public String root(){
        return "redirect:/question/list";
    }
}
