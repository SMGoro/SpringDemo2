package com.smgoro.springdemo2.Base;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/index")
    public String indexDefault(Model model) {
        return "index";
    }

}

