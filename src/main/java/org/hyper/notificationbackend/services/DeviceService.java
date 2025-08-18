package org.hyper.notificationbackend.services;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeviceService {

    private static WebDriver driver;
    private static boolean isAuthenticated = false;
    private static long lastAuthTime = 0;
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    private static final Object authLock = new Object(); // Synchronization lock

    public Map<String, Object> getDeviceData() {
        synchronized (authLock) {
            Map<String, Object> result = new HashMap<>();

            try {
                // Check if we need to authenticate or re-authenticate
                boolean needsAuth = driver == null || !isAuthenticated || 
                                  (System.currentTimeMillis() - lastAuthTime) > SESSION_TIMEOUT;

                if (needsAuth) {
                    System.out.println("=== AUTHENTICATION NEEDED ===");
                    authenticateUser();
                } else {
                    System.out.println("=== USING EXISTING SESSION ===");
                }

                // Navigate directly to the data page (session should be maintained)
                System.out.println("=== FETCHING LATEST DATA ===");
                driver.get("http://10.41.15.7:8080/ord/file:%5EPx%20files/Affichage.px");
                Thread.sleep(2000);

                String currentUrl = driver.getCurrentUrl();
                System.out.println("Current URL: " + currentUrl);

                // Check if we were redirected to login (session expired)
                if (currentUrl.contains("login") || currentUrl.contains("prelogin")) {
                System.out.println("Session expired, re-authenticating...");
                isAuthenticated = false;
                authenticateUser();
                
                // Try again after re-authentication
                driver.get("http://10.41.15.7:8080/ord/file:%5EPx%20files/Affichage.px");
                Thread.sleep(2000);
            }

            String pxContent = driver.getPageSource();

            // Extract data from iframe
            System.out.println("=== EXTRACTING SENSOR DATA ===");
            try {
                // Force page refresh to get latest data
                System.out.println("Refreshing page to get latest sensor data...");
                driver.navigate().refresh();
                Thread.sleep(3000);
                
                // Wait for iframe to be present and switch to it
                WebDriverWait iframeWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement iframe = iframeWait.until(ExpectedConditions.presenceOfElementLocated(By.id("servletViewWidget")));
                
                // Switch to iframe and get its content
                driver.switchTo().frame(iframe);
                Thread.sleep(2000);
                
                String iframeContent = driver.getPageSource();
                
                // Parse sensor data
                Map<String, String> sensorData = parseSensorData(iframeContent);
                
                // Switch back to main content
                driver.switchTo().defaultContent();
                
                // Structure the response for the frontend
                result.put("success", true);
                result.put("timestamp", System.currentTimeMillis());
                result.put("temperature", sensorData.getOrDefault("temperature", "N/A"));
                result.put("temperature_unit", sensorData.getOrDefault("temperature_unit", "°C"));
                result.put("pressure", sensorData.getOrDefault("pressure", "N/A"));
                result.put("pressure_unit", sensorData.getOrDefault("pressure_unit", "Pa"));
                result.put("humidity", sensorData.getOrDefault("humidity", "N/A"));
                result.put("humidity_unit", sensorData.getOrDefault("humidity_unit", "%RH"));
                result.put("sensor_data", sensorData);
                result.put("current_url", currentUrl);
                
                System.out.println("=== DATA EXTRACTION COMPLETE ===");
                System.out.println("Temperature: " + result.get("temperature") + " " + result.get("temperature_unit"));
                System.out.println("Pressure: " + result.get("pressure") + " " + result.get("pressure_unit"));
                System.out.println("Humidity: " + result.get("humidity") + " " + result.get("humidity_unit"));
                
            } catch (Exception e) {
                System.err.println("Error extracting data from iframe: " + e.getMessage());
                
                // Try fallback: parse data directly from main page without iframe
                System.out.println("Trying fallback: parsing data from main page...");
                try {
                    String mainPageContent = driver.getPageSource();
                    Map<String, String> sensorData = parseSensorData(mainPageContent);
                    
                    if (!sensorData.isEmpty()) {
                        result.put("success", true);
                        result.put("timestamp", System.currentTimeMillis());
                        result.put("temperature", sensorData.getOrDefault("temperature", "N/A"));
                        result.put("temperature_unit", sensorData.getOrDefault("temperature_unit", "°C"));
                        result.put("pressure", sensorData.getOrDefault("pressure", "N/A"));
                        result.put("pressure_unit", sensorData.getOrDefault("pressure_unit", "Pa"));
                        result.put("humidity", sensorData.getOrDefault("humidity", "N/A"));
                        result.put("humidity_unit", sensorData.getOrDefault("humidity_unit", "%RH"));
                        result.put("sensor_data", sensorData);
                        result.put("current_url", currentUrl);
                        result.put("data_source", "main_page_fallback");
                        
                        System.out.println("=== FALLBACK DATA EXTRACTION COMPLETE ===");
                        System.out.println("Temperature: " + result.get("temperature") + " " + result.get("temperature_unit"));
                        System.out.println("Pressure: " + result.get("pressure") + " " + result.get("pressure_unit"));
                        System.out.println("Humidity: " + result.get("humidity") + " " + result.get("humidity_unit"));
                    } else {
                        result.put("iframe_error", e.getMessage());
                        result.put("success", false);
                    }
                } catch (Exception fallbackException) {
                    System.err.println("Fallback parsing also failed: " + fallbackException.getMessage());
                    result.put("iframe_error", e.getMessage());
                    result.put("fallback_error", fallbackException.getMessage());
                    result.put("success", false);
                }
            }

        } catch (Exception e) {
            System.err.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            result.put("error", e.getMessage());
            result.put("success", false);
            
            // Reset authentication state on error
            isAuthenticated = false;
        }

        return result;
        } // End synchronized block
    }

    private void authenticateUser() throws Exception {
        System.out.println("=== STARTING AUTHENTICATION ===");
        
        // Close existing driver if it exists
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.out.println("Error closing existing driver: " + e.getMessage());
            }
            driver = null; // Set to null after quitting
        }

        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            // Step 1: Navigate to prelogin page and inspect form
            System.out.println("Step 1: Navigating to prelogin page");
            driver.get("http://10.41.15.7:8080/prelogin");
            Thread.sleep(3000);
            
            // Debug: Print page source to understand the form structure
            String pageSource = driver.getPageSource();
            System.out.println("=== PRELOGIN PAGE SOURCE (first 2000 chars) ===");
            System.out.println(pageSource.substring(0, Math.min(2000, pageSource.length())));
            System.out.println("=== END PAGE SOURCE ===");
            
            System.out.println("Step 2: Finding and filling username field");
            WebElement usernameInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("j_username")));
            
            // Debug: Check if there are any other required fields
            try {
                java.util.List<WebElement> allInputs = driver.findElements(By.tagName("input"));
                System.out.println("Found " + allInputs.size() + " input elements on prelogin page:");
                for (WebElement input : allInputs) {
                    String name = input.getAttribute("name");
                    String type = input.getAttribute("type");
                    String id = input.getAttribute("id");
                    System.out.println("  - Input: name='" + name + "', type='" + type + "', id='" + id + "'");
                }
            } catch (Exception e) {
                System.err.println("Could not inspect form inputs: " + e.getMessage());
            }
            
            // Use JavaScript to clear and set the value to avoid stale element issues
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", usernameInput);
            ((JavascriptExecutor) driver).executeScript("arguments[0].value = 'Affichage';", usernameInput);
            System.out.println("Username 'Affichage' entered successfully using JavaScript");
            
            System.out.println("Step 3: Clicking submit button for username");
            WebElement submitButton = driver.findElement(By.id("login-submit"));
            submitButton.click();
            
            // Wait for page to navigate after username submission
            Thread.sleep(5000);
            
            String currentUrl = driver.getCurrentUrl();
            System.out.println("After username submission, current URL: " + currentUrl);
            
            // If we get auth=fail immediately, let's try different approaches
            if (currentUrl.contains("auth=fail")) {
                System.err.println("===== IMMEDIATE AUTH FAILURE DETECTED =====");
                System.err.println("The username 'Affichage' is being rejected immediately.");
                System.err.println("Let's try accessing the direct login page instead...");
                
                // Try accessing the main login page directly
                driver.get("http://10.41.15.7:8080/login");
                Thread.sleep(3000);
                
                String loginPageSource = driver.getPageSource();
                System.out.println("=== LOGIN PAGE SOURCE (first 2000 chars) ===");
                System.out.println(loginPageSource.substring(0, Math.min(2000, loginPageSource.length())));
                System.out.println("=== END LOGIN PAGE SOURCE ===");
                
                // Try to find both username and password fields on the same page
                try {
                    WebElement directUsernameInput = driver.findElement(By.name("j_username"));
                    WebElement directPasswordInput = driver.findElement(By.name("j_password"));
                    
                    System.out.println("Found both username and password fields on login page - trying direct login");
                    
                    directUsernameInput.clear();
                    directUsernameInput.sendKeys("Affichage");
                    
                    directPasswordInput.clear();
                    directPasswordInput.sendKeys("Azerty123$");
                    
                    WebElement directLoginButton = driver.findElement(By.id("login-submit"));
                    directLoginButton.click();
                    
                    Thread.sleep(5000);
                    String directLoginUrl = driver.getCurrentUrl();
                    System.out.println("After direct login attempt, URL: " + directLoginUrl);
                    
                    if (!directLoginUrl.contains("auth=fail") && !directLoginUrl.contains("login")) {
                        System.out.println("Direct login appears successful! Continuing...");
                        // Update the current URL for the rest of the method
                        currentUrl = directLoginUrl;
                    } else {
                        throw new Exception("Both prelogin and direct login failed - credentials may be incorrect");
                    }
                    
                } catch (Exception directLoginEx) {
                    System.err.println("Direct login also failed: " + directLoginEx.getMessage());
                    throw new Exception("Authentication failed: both prelogin flow and direct login failed");
                }
            }

            // Step 4: Handle password entry on the new page (re-find all elements to avoid stale references)
            System.out.println("Step 4: Looking for password field on current page");
            
            // Wait for password field to be available on the current page
            WebElement passwordInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("j_password")));
            System.out.println("Found password field");
            
            // Clear and enter password
            passwordInput.clear();
            Thread.sleep(500);
            passwordInput.sendKeys("Azerty123$");
            System.out.println("Password entered successfully");
            
            // Step 5: Submit login form (re-find button to avoid stale reference)
            System.out.println("Step 5: Finding login button for password submission");
            Thread.sleep(1000);
            
            // Find and click the login button for password submission
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-submit")));
            loginButton.click();
            System.out.println("Login button clicked for password submission");
            
            // Wait for authentication to complete
            Thread.sleep(8000);
            
            String finalUrl = driver.getCurrentUrl();
            System.out.println("Authentication completed, final URL: " + finalUrl);
            
            // Check authentication results
            if (finalUrl.contains("auth=fail")) {
                System.err.println("Authentication explicitly failed - auth=fail in URL");
                throw new Exception("Authentication failed: incorrect credentials");
            }
            
            if (finalUrl.contains("login") || finalUrl.contains("prelogin")) {
                System.err.println("Authentication failed - still on login page: " + finalUrl);
                throw new Exception("Authentication failed: redirected back to login page");
            }
            
            // Test navigation to verify authentication works
            System.out.println("Step 6: Testing access to protected content");
            driver.get("http://10.41.15.7:8080/ord/file:%5EPx%20files/Affichage.px");
            Thread.sleep(3000);
            
            String testUrl = driver.getCurrentUrl();
            System.out.println("Test navigation URL: " + testUrl);
            
            if (testUrl.contains("login") || testUrl.contains("prelogin")) {
                System.err.println("Authentication verification failed - redirected to login when accessing protected content");
                throw new Exception("Authentication verification failed");
            }
            
            // Mark as authenticated
            isAuthenticated = true;
            lastAuthTime = System.currentTimeMillis();
            
            System.out.println("=== AUTHENTICATION SUCCESSFUL ===");
            
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            isAuthenticated = false;
            
            // Clean up on failure
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception quitException) {
                    System.err.println("Error quitting driver: " + quitException.getMessage());
                }
                driver = null; // Set to null after quitting
            }
            throw new Exception("Authentication failed: " + e.getMessage(), e);
        }
    }

    private Map<String, String> parseSensorData(String htmlContent) {
        Map<String, String> sensorData = new HashMap<>();
        
        try {
            System.out.println("=== PARSING SENSOR DATA ===");
            
            // Look for title attributes which contain the precise sensor readings
            // Pattern: title="T° moyenne CTA 1 = = 28.7 °C {ok} @ def"
            java.util.regex.Pattern titlePattern = java.util.regex.Pattern.compile(
                "title=\"[^\"]*=\\s*=?\\s*([\\d\\.-]+)\\s*([^\\s{}]+)[^\"]*\"", 
                java.util.regex.Pattern.CASE_INSENSITIVE
            );
            java.util.regex.Matcher titleMatcher = titlePattern.matcher(htmlContent);
            
            while (titleMatcher.find()) {
                String value = titleMatcher.group(1);
                String unit = titleMatcher.group(2);
                String fullTitle = titleMatcher.group(0);
                
                System.out.println("Found title: " + fullTitle);
                System.out.println("Extracted - Value: " + value + ", Unit: " + unit);
                
                // Determine if this is interior (CTA/moyenne) or exterior (extérieure) data
                boolean isInteriorData = fullTitle.contains("CTA") || fullTitle.contains("moyenne") || 
                                       (!fullTitle.contains("extérieure") && !fullTitle.contains("extΘrieure"));
                boolean isExteriorData = fullTitle.contains("extérieure") || fullTitle.contains("extΘrieure");
                
                if (unit.contains("C") || unit.contains("°")) {
                    // Prioritize interior temperature over exterior
                    if (isInteriorData || !sensorData.containsKey("temperature")) {
                        sensorData.put("temperature", value);
                        sensorData.put("temperature_unit", "°C");
                        String location = isInteriorData ? " (Interior)" : (isExteriorData ? " (Exterior)" : "");
                        System.out.println("✓ Temperature" + location + ": " + value + "°C");
                    }
                } else if (unit.equals("Pa")) {
                    if (!sensorData.containsKey("pressure")) {
                        sensorData.put("pressure", value);
                        sensorData.put("pressure_unit", "Pa");
                        System.out.println("✓ Pressure: " + value + " Pa");
                    }
                } else if (unit.contains("%") || unit.contains("RH")) {
                    // Prioritize interior humidity over exterior
                    if (isInteriorData || !sensorData.containsKey("humidity")) {
                        sensorData.put("humidity", value);
                        sensorData.put("humidity_unit", "%RH");
                        String location = isInteriorData ? " (Interior)" : (isExteriorData ? " (Exterior)" : "");
                        System.out.println("✓ Humidity" + location + ": " + value + "%RH");
                    }
                }
            }

            // Fallback: Parse from display text if title attributes didn't work
            if (sensorData.size() < 3) {
                System.out.println("Using fallback parsing from display text...");
                
                // Temperature pattern
                java.util.regex.Pattern tempPattern = java.util.regex.Pattern.compile("([\\d\\.-]+)\\s*[░°]C");
                java.util.regex.Matcher tempMatcher = tempPattern.matcher(htmlContent);
                if (tempMatcher.find() && !sensorData.containsKey("temperature")) {
                    sensorData.put("temperature", tempMatcher.group(1));
                    sensorData.put("temperature_unit", "°C");
                    System.out.println("✓ Temperature (fallback): " + tempMatcher.group(1) + "°C");
                }
                
                // Pressure pattern
                java.util.regex.Pattern pressurePattern = java.util.regex.Pattern.compile("([\\d\\.-]+)\\s*Pa");
                java.util.regex.Matcher pressureMatcher = pressurePattern.matcher(htmlContent);
                if (pressureMatcher.find() && !sensorData.containsKey("pressure")) {
                    sensorData.put("pressure", pressureMatcher.group(1));
                    sensorData.put("pressure_unit", "Pa");
                    System.out.println("✓ Pressure (fallback): " + pressureMatcher.group(1) + " Pa");
                }
                
                // Humidity pattern
                java.util.regex.Pattern humidityPattern = java.util.regex.Pattern.compile("([\\d\\.-]+)\\s*%RH");
                java.util.regex.Matcher humidityMatcher = humidityPattern.matcher(htmlContent);
                if (humidityMatcher.find() && !sensorData.containsKey("humidity")) {
                    sensorData.put("humidity", humidityMatcher.group(1));
                    sensorData.put("humidity_unit", "%RH");
                    System.out.println("✓ Humidity (fallback): " + humidityMatcher.group(1) + "%RH");
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing sensor data: " + e.getMessage());
            e.printStackTrace();
            sensorData.put("parse_error", e.getMessage());
        }
        
        return sensorData;
    }

    // Optional: Add a method to force re-authentication
    public Map<String, Object> forceReauth() {
        isAuthenticated = false;
        return getDeviceData();
    }

    // Optional: Add a method to close the browser session
    public void closeBrowserSession() {
        if (driver != null) {
            try {
                driver.quit();
                driver = null;
                isAuthenticated = false;
                System.out.println("Browser session closed");
            } catch (Exception e) {
                System.err.println("Error closing browser session: " + e.getMessage());
            }
        }
    }
}
