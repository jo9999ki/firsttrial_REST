package de.jk.spring.firsttrial.restcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
	 @GetMapping("/")
	    public String index(final Model model) {
	        model.addAttribute("title", "MySpringBootApplication");
	        model.addAttribute("msg", "Welcome to firsttrial!");
	        return "index";
	    }
}
