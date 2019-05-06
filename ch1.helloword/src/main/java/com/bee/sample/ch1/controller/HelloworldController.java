package com.bee.sample.ch1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bee.sample.ch1.annotation.Function;
/**
 * 访问:http://127.0.0.1:8080/sayhello.html?name=springboot
 * @author xiandafu
 *
 */
@Controller
public class HelloworldController {
	
	
	@RequestMapping("/say.html")
	//@Function()是自定义的一个注解，如果()内为空，则谁都可以访问，设置为user.add后
	//
	@Function("user.add")
	public @ResponseBody String say(String name){
		return "hello "+name;
	}
}
