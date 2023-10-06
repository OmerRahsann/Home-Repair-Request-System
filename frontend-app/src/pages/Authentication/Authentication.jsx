import React, { useState } from "react";
import SignInForm from "../../components/Authentication/CustomerAuth/SignIn";
import SignUpForm from "../../components/Authentication/CustomerAuth/SignUp";
import "../../components/Authentication/auth.css"

export default function ControllerLogin() {
  const [type, setType] = useState("signIn");
  const handleOnClick = text => {
    if (text !== type) {
      setType(text);
      return;
    }
  };
  const containerClass =
    "container " + (type === "signUp" ? "right-panel-active" : "");
  return (
    <div>
      <div className="absolute inset-x-0 top-0 h-16 p-2 flex justify-between">
        <h1 class="text-2xl font-semibold">RepairRadar</h1>
        <a href="/provider/auth" className="text-blue-500 hover:underline">Are you a service provider?</a>
      </div>
      <div className="Authentication">
        <div className={containerClass} id="container">
          <SignUpForm />
          <SignInForm />
          <div className="overlay-container">
            <div className="overlay">
              <div className="overlay-panel overlay-left">
                <h1>Welcome Back!</h1>
                <p>
                  To keep connected with us please login with your personal info
                </p>
                <button
                  className="ghost"
                  id="signIn"
                  onClick={() => handleOnClick("signIn")}
                >
                  Sign In
                </button>
              </div>
              <div className="overlay-panel overlay-right">
                <h1>Welcome to RepairRadar!</h1>
                <p>Enter your personal details and start your journey with us</p>
                <button
                  className="ghost "
                  id="signUp"
                  onClick={() => handleOnClick("signUp")}
                >
                  Sign Up
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
