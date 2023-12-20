package com.homeRepair.step_definitions;

import com.homeRepair.utilities.ConfigurationReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

public class providerCustomerAppointment {

    WebDriver driver;

    @Given("a logged-inn provider is on the dashboard")
    public void provider_on_logged_in() throws InterruptedException {

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


        //a[.="Are you a service provider?"]
        WebElement serviceProviderButton = driver.findElement(By.xpath("//a[.=\"Are you a service provider?\"]"));
        serviceProviderButton.click();

        WebElement signInEmailProButton = driver.findElement(By.xpath("//input[@placeholder=\"name@company.com\"]"));
        signInEmailProButton.sendKeys(ConfigurationReader.get("pusername"));
        Thread.sleep(1000);
        WebElement signInpasswordProButton = driver.findElement(By.xpath("//input[@name=\"password\"]"));
        signInpasswordProButton.sendKeys("12345678");
        Thread.sleep(1000);


        WebElement signInProviderButton = driver.findElement(By.xpath("//button[.='Sign in']"));
        signInProviderButton.click();
        Thread.sleep(2000);
    }

    @When("Provider sees the notification and creates an appointment")
    public void provider_applies_filters() throws InterruptedException {

        WebElement notifyButton = driver.findElement(By.xpath("//span[@class=\"absolute -inset-1.5\"]"));
        notifyButton.click();
        Thread.sleep(1000);



        WebElement exitButton = driver.findElement(By.xpath("(//button[.='Clear All']/../button)[2]"));
        exitButton.click();
        Thread.sleep(1000);

        WebElement locationProButton = driver.findElement(By.xpath("//input[@placeholder=\"Search by location...\"]"));
        locationProButton.sendKeys("Cream Ridge, Upper Freehold, NJ, USA");
        Thread.sleep(2000);
        Actions keyProDown = new Actions(driver);
        keyProDown.sendKeys(Keys.chord(Keys.DOWN, Keys.RETURN)).perform();
        Thread.sleep(2000);

        WebElement priceProButton = driver.findElement(By.xpath("(//div[@class=\" css-13cymwt-control\"])[2]"));
        Thread.sleep(1000);
        priceProButton.click();
        //Thread.sleep(1000);
        //priceProButton.sendKeys("200");
        Thread.sleep(1000);
        Actions keyProDown1 = new Actions(driver);
        keyProDown1.sendKeys(Keys.chord(Keys.DOWN, Keys.RETURN)).perform();
        Thread.sleep(2000);

        WebElement openJobButton = driver.findElement(By.xpath("//button[.='Open Job']"));
        openJobButton.click();
        Thread.sleep(2000);

        WebElement appointmentButton = driver.findElement(By.xpath("//button[.='Create Appointment']"));
        appointmentButton.click();
        Thread.sleep(2000);

        WebElement weekbutton = driver.findElement(By.xpath("//button[.='Week']"));
        weekbutton.click();
        Thread.sleep(1000);


        WebElement calendar = driver.findElement(By.xpath("((//div[@class=\"rbc-day-slot rbc-time-column\"])[3]/div)[15]"));

        Actions calendarget = new Actions(driver);
        calendarget.moveToElement(calendar).perform();
        WebElement chooseDate = driver.findElement(By.xpath("//button[.='Next']"));
        Thread.sleep(1000);
        chooseDate.click();
        Thread.sleep(1000);
        chooseDate.click();
        Thread.sleep(1000);



        calendarget.moveToElement(calendar).clickAndHold().release().perform();
        Thread.sleep(2000);
        Alert successResponse = driver.switchTo().alert();
        successResponse.accept();

        Thread.sleep(1000);

        WebElement createAppointment = driver.findElement(By.xpath("//button[.='Schedule Appointment']"));
        WebElement descriptionAppointment = driver.findElement(By.xpath("//textarea[@maxlength='500']"));

        Actions actions = new Actions(driver);
        actions.moveToElement(createAppointment).perform();
        Thread.sleep(1000);

        descriptionAppointment.sendKeys("Hello there, I hope you are doing well.");
        Thread.sleep(1000);
        descriptionAppointment.click();
        Thread.sleep(1000);

        createAppointment.click();
        Thread.sleep(1000);
        WebElement back = driver.findElement(By.xpath("//button[.='BACK']"));
        back.click();
        Thread.sleep(1000);
        WebElement x = driver.findElement(By.xpath("//button[.='X']"));
        x.click();
        Thread.sleep(3000);


    }

    @When("the customer checks the notification and accept the appointment.")
    public void customer_acceptsApp() throws InterruptedException {

        //a[@href="/provider/myappointments"]


        WebElement myappointments = driver.findElement(By.xpath("//a[@href=\"/provider/myappointments\"]"));
        myappointments.click();
        Thread.sleep(1000);
        WebElement weekBButton = driver.findElement(By.xpath("//button[.='Week']"));
        weekBButton.click();
        Thread.sleep(2000);
        WebElement chooseDate = driver.findElement(By.xpath("//button[.='Next']"));
        Thread.sleep(1000);
        chooseDate.click();
        Thread.sleep(1000);
        chooseDate.click();
        Thread.sleep(1000);

        //(//div[@class="rbc-time-slot"]/span)[21]

        WebElement move = driver.findElement(By.xpath("(//div[@class=\"rbc-time-slot\"]/span)[21]"));

        Actions actions = new Actions(driver);
        actions.moveToElement(move).perform();
        //div[@class="rbc-event-label"][10]

        WebElement appointment = driver.findElement(By.xpath("(//div[@class=\"rbc-event-label\"])[1]"));
        appointment.click();
        Thread.sleep(3000);

        WebElement xbutton = driver.findElement(By.xpath("//button[.='X']"));
        xbutton.click();
        Thread.sleep(1000);

        //button[@id="headlessui-menu-button-:r1:"]

        WebElement profile = driver.findElement(By.xpath("//div[@class=\"flex flex-row \"]/div/button"));
        profile.click();
        Thread.sleep(1000);

        WebElement signout = driver.findElement(By.xpath("//a[.='Sign out']"));
        signout.click();
        Thread.sleep(2000);

        Alert successResponse = driver.switchTo().alert();
        successResponse.accept();

        Thread.sleep(2000);

        WebElement signin = driver.findElement(By.xpath("//a[.='Sign In']"));
        signin.click();

        WebElement signInEmailButton = driver.findElement(By.xpath("//input[@placeholder=\"Email\"]"));
        signInEmailButton.sendKeys(ConfigurationReader.get("cusername"));
        Thread.sleep(1000);
        WebElement signInpasswordButton = driver.findElement(By.xpath("//input[@placeholder=\"Password\"]"));
        signInpasswordButton.sendKeys("12345678");
        Thread.sleep(1000);

        WebElement signinP = driver.findElement(By.xpath("//button[.='Sign in']"));
        signinP.click();

        Thread.sleep(1000);

        WebElement notifyButton = driver.findElement(By.xpath("//span[@class=\"absolute -inset-1.5\"]"));
        notifyButton.click();
        Thread.sleep(2000);


        WebElement exitButton = driver.findElement(By.xpath("(//button[.='Clear All']/../button)[2]"));
        exitButton.click();
        Thread.sleep(1000);


        WebElement myuserappointments = driver.findElement(By.xpath("//a[@href=\"/customer/myappointments\"]"));
        myuserappointments.click();
        Thread.sleep(2000);

        //(//div[@class="rbc-event-content"])[6]

        WebElement myuserappoi = driver.findElement(By.xpath("(//div[@class=\"rbc-event-content\"])[1]"));
        myuserappoi.click();
        Thread.sleep(3000);

        WebElement svgEdit = driver.findElement(By.xpath("(//*[name()='svg'])[5]"));
        svgEdit.click();
        Thread.sleep(2000);

    }
    @When("the appointment on provider calendar should updated as approved.")
    public void customer_accepts() throws InterruptedException {


        WebElement profile = driver.findElement(By.xpath("(//div[@class=\"flex flex-row\"]/button)[2]"));
        profile.click();
        Thread.sleep(1000);

        WebElement signout = driver.findElement(By.xpath("//a[.='Sign out']"));
        signout.click();
        Thread.sleep(2000);

        Alert successResponse = driver.switchTo().alert();
        successResponse.accept();

        Thread.sleep(2000);

        WebElement signin = driver.findElement(By.xpath("//a[.='Sign In']"));
        signin.click();

        WebElement serviceProviderButton = driver.findElement(By.xpath("//a[.=\"Are you a service provider?\"]"));
        serviceProviderButton.click();
        Thread.sleep(2000);


        WebElement signInEmailProButton = driver.findElement(By.xpath("//input[@placeholder=\"name@company.com\"]"));
        signInEmailProButton.sendKeys(ConfigurationReader.get("pusername"));
        Thread.sleep(1000);
        WebElement signInpasswordProButton = driver.findElement(By.xpath("//input[@name=\"password\"]"));
        signInpasswordProButton.sendKeys("12345678");
        Thread.sleep(1000);

        WebElement signinc = driver.findElement(By.xpath("//button[.='Sign in']"));
        signinc.click();

        Thread.sleep(2000);

        WebElement notifyButton = driver.findElement(By.xpath("//span[@class=\"absolute -inset-1.5\"]"));
        notifyButton.click();
        Thread.sleep(2000);


        WebElement exitButton = driver.findElement(By.xpath("(//button[.='Clear All']/../button)[2]"));
        exitButton.click();
        Thread.sleep(1000);

        WebElement myappointments = driver.findElement(By.xpath("//a[@href=\"/provider/updates\"]"));
        myappointments.click();

        Thread.sleep(2000);

    }

        @When("the provider will cancel the appointment and customer will get notified")
        public void cancelappoinment () throws InterruptedException {


            WebElement myappointments = driver.findElement(By.xpath("//a[@href=\"/provider/myappointments\"]"));
            myappointments.click();

            Thread.sleep(2000);

            Thread.sleep(1000);
            WebElement weekBButton = driver.findElement(By.xpath("//button[.='Week']"));
            weekBButton.click();
            Thread.sleep(2000);
            WebElement chooseDate = driver.findElement(By.xpath("//button[.='Next']"));
            Thread.sleep(1000);
            chooseDate.click();
            Thread.sleep(1000);
            chooseDate.click();
            Thread.sleep(1000);

            //(//div[@class="rbc-time-slot"]/span)[21]

            WebElement move = driver.findElement(By.xpath("(//div[@class=\"rbc-time-slot\"]/span)[21]"));

            Actions actions = new Actions(driver);
            actions.moveToElement(move).perform();
            //div[@class="rbc-event-label"][10]

            WebElement appointment = driver.findElement(By.xpath("(//div[@class=\"rbc-event-label\"])[1]"));
            appointment.click();
            Thread.sleep(2000);

            WebElement svgEdit2 = driver.findElement(By.xpath("(//*[name()='svg'])[4]"));
            svgEdit2.click();
            Thread.sleep(2000);

            Alert successResponse = driver.switchTo().alert();
            successResponse.accept();

            Thread.sleep(2000);

            WebElement profile = driver.findElement(By.xpath("//div[@class=\"flex flex-row \"]/div/button"));
            profile.click();
            Thread.sleep(1000);

            WebElement signout = driver.findElement(By.xpath("//a[.='Sign out']"));
            signout.click();
            Thread.sleep(2000);

            Alert successResponse2 = driver.switchTo().alert();
            successResponse2.accept();

            Thread.sleep(2000);

            WebElement signin = driver.findElement(By.xpath("//a[.='Sign In']"));
            signin.click();

            WebElement signInEmailButton = driver.findElement(By.xpath("//input[@placeholder=\"Email\"]"));
            signInEmailButton.sendKeys(ConfigurationReader.get("cusername"));
            Thread.sleep(1000);
            WebElement signInpasswordButton = driver.findElement(By.xpath("//input[@placeholder=\"Password\"]"));
            signInpasswordButton.sendKeys("12345678");
            Thread.sleep(1000);

            WebElement signinP = driver.findElement(By.xpath("//button[.='Sign in']"));
            signinP.click();

            Thread.sleep(1000);

            WebElement notifyButton = driver.findElement(By.xpath("//span[@class=\"absolute -inset-1.5\"]"));
            notifyButton.click();
            Thread.sleep(3000);





        }
    }








