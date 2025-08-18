package org.hyper.notificationbackend.services;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class DeviceService {

    public Map<String, Object> getDeviceData() {
        Map<String, Object> result = new HashMap<>();

        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in background
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Step 1: Open prelogin page and inspect its structure
            System.out.println("=== Navigating to prelogin page ===");
            driver.get("http://10.41.15.7:8080/prelogin");
            
            // Wait for page to load
            Thread.sleep(2000);
            
            // Debug: Print page source to understand structure
            String preloginSource = driver.getPageSource();
            System.out.println("=== PRELOGIN PAGE SOURCE ===");
            System.out.println(preloginSource);
            System.out.println("=== END PRELOGIN SOURCE ===");
            
            // Try to find input fields by different methods
            System.out.println("=== Looking for input fields ===");
            List<WebElement> allInputs = driver.findElements(By.tagName("input"));
            for (int i = 0; i < allInputs.size(); i++) {
                WebElement input = allInputs.get(i);
                String type = input.getAttribute("type");
                String name = input.getAttribute("name");
                String id = input.getAttribute("id");
                String className = input.getAttribute("class");
                System.out.println("Input " + i + ": type=" + type + ", name=" + name + ", id=" + id + ", class=" + className);
            }

            // Try to find forms
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            System.out.println("Found " + forms.size() + " form(s)");
            for (int i = 0; i < forms.size(); i++) {
                WebElement form = forms.get(i);
                String action = form.getAttribute("action");
                String method = form.getAttribute("method");
                System.out.println("Form " + i + ": action=" + action + ", method=" + method);
            }

            // Try different approaches to find username field
            WebElement usernameInput = null;
            try {
                usernameInput = driver.findElement(By.name("j_username"));
                System.out.println("Found username field by name 'j_username'");
            } catch (Exception e1) {
                try {
                    usernameInput = driver.findElement(By.name("username"));
                    System.out.println("Found username field by name 'username'");
                } catch (Exception e2) {
                    try {
                        usernameInput = driver.findElement(By.id("username"));
                        System.out.println("Found username field by id 'username'");
                    } catch (Exception e3) {
                        try {
                            usernameInput = driver.findElement(By.xpath("//input[@type='text']"));
                            System.out.println("Found username field by xpath text input");
                        } catch (Exception e4) {
                            System.out.println("Could not find username field");
                        }
                    }
                }
            }

            if (usernameInput != null) {
                usernameInput.sendKeys("Affichage");
                System.out.println("Entered username");
                
                // Submit the username form first
                WebElement submitButton = driver.findElement(By.id("login-submit"));
                submitButton.click();
                System.out.println("Submitted username");
                
                // Wait for the page to process and potentially redirect
                Thread.sleep(3000);
                
                // Check current URL to see where we ended up
                String currentUrl = driver.getCurrentUrl();
                System.out.println("Current URL after username submission: " + currentUrl);
                
                // Print the page source after username submission
                String afterUsernameSource = driver.getPageSource();
                System.out.println("=== PAGE SOURCE AFTER USERNAME SUBMISSION ===");
                System.out.println(afterUsernameSource);
                System.out.println("=== END AFTER USERNAME SOURCE ===");
                
                // Debug: Print login page source
                String loginSource = driver.getPageSource();
                System.out.println("=== LOGIN PAGE SOURCE ===");
                System.out.println(loginSource);
                System.out.println("=== END LOGIN SOURCE ===");
                
                // Look for password field on login page
                List<WebElement> loginInputs = driver.findElements(By.tagName("input"));
                for (int i = 0; i < loginInputs.size(); i++) {
                    WebElement input = loginInputs.get(i);
                    String type = input.getAttribute("type");
                    String name = input.getAttribute("name");
                    String id = input.getAttribute("id");
                    String className = input.getAttribute("class");
                    System.out.println("Login Input " + i + ": type=" + type + ", name=" + name + ", id=" + id + ", class=" + className);
                }

                // Try different approaches to find password field
                WebElement passwordInput = null;
                try {
                    passwordInput = driver.findElement(By.name("j_password"));
                    System.out.println("Found password field by name 'j_password'");
                } catch (Exception e1) {
                    try {
                        passwordInput = driver.findElement(By.name("password"));
                        System.out.println("Found password field by name 'password'");
                    } catch (Exception e2) {
                        try {
                            passwordInput = driver.findElement(By.id("password"));
                            System.out.println("Found password field by id 'password'");
                        } catch (Exception e3) {
                            try {
                                passwordInput = driver.findElement(By.xpath("//input[@type='password']"));
                                System.out.println("Found password field by xpath password input");
                            } catch (Exception e4) {
                                System.out.println("Could not find password field");
                            }
                        }
                    }
                }

                if (passwordInput != null) {
                    passwordInput.sendKeys("Azerty123$");
                    System.out.println("Entered password");

                    // Try to find and click login button
                    WebElement loginButton = null;
                    try {
                        loginButton = driver.findElement(By.id("login-submit"));
                    } catch (Exception e1) {
                        try {
                            loginButton = driver.findElement(By.xpath("//input[@type='submit']"));
                        } catch (Exception e2) {
                            try {
                                loginButton = driver.findElement(By.xpath("//button[@type='submit']"));
                            } catch (Exception e3) {
                                System.out.println("Could not find login button");
                            }
                        }
                    }

                    if (loginButton != null) {
                        loginButton.click();
                        System.out.println("Clicked login button");
                        
                        // Wait for redirect
                        Thread.sleep(3000);
                        
                        // Navigate to the data page
                        System.out.println("=== Navigating to data page ===");
                        driver.get("http://10.41.15.7:8080/ord/file:%5EPx%20files/Affichage.px");
                        Thread.sleep(2000);
                        
                        String pxContent = driver.getPageSource();
                        result.put("px_content", pxContent);
                        result.put("success", true);
                    } else {
                        result.put("error", "Could not find login button");
                    }
                } else {
                    result.put("error", "Could not find password field");
                }
            } else {
                result.put("error", "Could not find username field");
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", e.getMessage());
            result.put("stackTrace", e.getStackTrace());
        } finally {
            driver.quit();
        }

        return result;
    }
}
