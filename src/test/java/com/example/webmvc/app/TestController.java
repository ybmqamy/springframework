package com.example.webmvc.app;

import com.example.Annotation.GetMapping;
import com.example.Annotation.PostMapping;
import com.example.Annotation.RequestBody;
import com.example.Annotation.RequestParam;
import com.example.Annotation.ResponseBody;
import com.example.Annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/hello")
    @ResponseBody
    public String hello() {
        return "Hello WebMvc";
    }

    @GetMapping("/api/sum")
    @ResponseBody
    public String sum(@RequestParam("a") int a, @RequestParam("b") int b) {
        return "sum:" + (a + b);
    }

    @PostMapping("/api/echo")
    @ResponseBody
    public String echo(@RequestBody EchoBody body) {
        return "echo:" + body.getMessage();
    }

    public static class EchoBody {
        private String message;

        public EchoBody() {
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
