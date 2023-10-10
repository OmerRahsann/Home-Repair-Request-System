import React, { useState } from "react";
import SignInForm from "../../components/Authentication/CustomerAuth/SignIn";
import ServiceProviderSignUp from "../../components/Authentication/ProviderAuth/ServiceProviderSignUp";
import "../../components/Authentication/auth.css"
import logo from "../../components/Logos/logo1.png"

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
    <div className="flex justify-center align-middle min-h-full mt-20 mb-50">
      <div className="flex justify-between absolute inset-x-0  top-0 p-3">
        <div>
          <img className = "absolute top-0 h-36 p-3 flex justify-start" src = {logo} alt = 'logo on sign-in page'/>
        </div>
      </div>
      <div className="Authentication">
        <div className={containerClass} id="container">
          <ServiceProviderSignUp />
          <SignInForm />
          <div className="overlay-container">
            <div className="overlay2">
              <div className="overlay2-panel overlay2-left">
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
              <div className="overlay2-panel overlay2-right">
                <h1>Want to Provide Services?</h1>
                <p>Enter your personal details and start your journey with RepairRadar.</p>
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
