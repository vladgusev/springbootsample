
package com.example.devops.cicd;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class HelloController {
    
    @RequestMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
    
    @RequestMapping(
    		  value = "/add", 
    		  params = { "number1", "number2" }, 
    		  method = RequestMethod.GET)
    		@ResponseBody
    		public String add(@RequestParam("number1") long number1, @RequestParam("number2") long number2)
    		 
    		{
    			long result = number1+number2;
    		    return "Addition Result is = " + result;
    		}
   
    @RequestMapping(
  		  value = "/subtract", 
  		  params = { "number1", "number2" }, 
  		  method = RequestMethod.GET)
  		@ResponseBody
  		public String subtract(@RequestParam("number1") long number1, @RequestParam("number2") long number2)
  		 
  		{
    			long result = number1-number2;
  		    return "Subtraction Result is = " + result;
  		}

    
}

