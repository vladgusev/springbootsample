package com.examples.devops.cicd;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.example.devops.cicd.Application;
import com.example.devops.cicd.HelloController;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={Application.class})
@AutoConfigureMockMvc
public class HelloControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getHello() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Greetings from Spring Boot!")));
    }
    
    @Test
    public void getAdd() throws Exception {
    	      String number1= "6";
    	      String number2= "4";
        mvc.perform(MockMvcRequestBuilders.get("/add").accept(MediaType.APPLICATION_JSON)
        		.param("number1", number1)
        		.param("number2", number2))
        	    .andExpect(status().isOk())
            .andExpect(content().string(equalTo("Addition Result is = 10")));
    }
    
    
    @Test
    public void getSubtract() throws Exception {
    	      String number1= "10";
    	      String number2= "4";
        mvc.perform(MockMvcRequestBuilders.get("/subtract").accept(MediaType.APPLICATION_JSON)
        		.param("number1", number1)
        		.param("number2", number2))
        	    .andExpect(status().isOk())
            .andExpect(content().string(equalTo("Subtraction Result is = 6")));
    }
}
