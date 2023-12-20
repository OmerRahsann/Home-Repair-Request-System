package com.homeRepair.step_definitions;

import com.homeRepair.utilities.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class customerAcceptsMailRequest {

    //span[@class="absolute -inset-1.5"]

     WebDriver driver;

    @Given("a loggedin customer is on the dashboard")
    public void user_logged_in() throws InterruptedException {

        // Optional. If not specified, WebDriver searches the PATH for chromedriver.
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\omerr\\IdeaProjects\\Home-Repair-Request-System\\homerepair_automation\\chromedriver.exe");
        ChromeOptions option = new ChromeOptions();
        //String allowedOrigins = "http://52.90.184.85:3000/";
        option.addArguments("--remote-allow-origins=*");


        driver = new ChromeDriver(option);

        driver.get(ConfigurationReader.get("url"));

        driver.manage().window().maximize();

        Thread.sleep(2000);
        WebElement signInButton = driver.findElement(By.xpath("//a[.='Sign In']"));
        signInButton.click();

        Thread.sleep(2000);

        WebElement signInEmailButton = driver.findElement(By.xpath("//input[@placeholder=\"Email\"]"));
        signInEmailButton.sendKeys(ConfigurationReader.get("cusername"));
        Thread.sleep(1000);
        WebElement signInpasswordButton = driver.findElement(By.xpath("//input[@placeholder=\"Password\"]"));
        signInpasswordButton.sendKeys("12345678");
        Thread.sleep(1000);


        WebElement createRequestButton = driver.findElement(By.xpath("//button[@type='submit']"));
        createRequestButton.click();

        Thread.sleep(1000);

    }
    @When("a customer clicks and open the  email request notification from a service provider")
    public void user_notify() throws InterruptedException {


        WebElement notifyButton = driver.findElement(By.xpath("//span[@class=\"absolute -inset-1.5\"]"));
        notifyButton.click();
        Thread.sleep(1000);

        WebElement clickRequest = driver.findElement(By.xpath("//a[.=\"Click Here\"]"));
        clickRequest.click();
        Thread.sleep(1000);

        WebElement svgEdit = driver.findElement(By.xpath("(//*[name()='svg'])[4]"));
        svgEdit.click();
        Thread.sleep(2000);



    }
    @Then("the customer should able to accepts or decline the request")
    public void user_request_accept() throws InterruptedException {




        WebElement svgEdit = driver.findElement(By.xpath("(//*[name()='svg'])[6]"));
        svgEdit.click();
        Thread.sleep(2000);



    }
}
